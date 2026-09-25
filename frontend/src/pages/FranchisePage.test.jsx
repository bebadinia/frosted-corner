import { cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { FranchisePage } from "./FranchisePage";

const { getAnalyticsSummary } = vi.hoisted(() => ({
  getAnalyticsSummary: vi.fn(),
}));

vi.mock("../api/client", () => ({
  getAnalyticsSummary,
}));

const manager = { id: "demo-manager", role: "MANAGER", storeId: "store1" };
const owner = { id: "demo-owner", role: "OWNER" };
const summary = {
  totalOrders: 7,
  revenue: 272.22,
  averageOrderValue: 38.89,
  topSellingProducts: [
    { productId: "P008", name: "Chocolate Chip Cookie", quantitySold: 19 },
    { productId: "P002", name: "Vanilla Cupcake", quantitySold: 8 },
  ],
};

describe("FranchisePage analytics", () => {
  beforeEach(() => {
    getAnalyticsSummary.mockReset();
  });

  afterEach(() => {
    cleanup();
    vi.restoreAllMocks();
  });

  it("shows loading and requests only the manager's assigned store", () => {
    getAnalyticsSummary.mockReturnValue(new Promise(() => {}));

    render(<FranchisePage currentUser={manager} />);

    expect(screen.getByRole("status")).toHaveTextContent("Loading sales analytics");
    expect(screen.getByText("Store: store1")).toBeInTheDocument();
    expect(getAnalyticsSummary).toHaveBeenCalledWith({ storeId: "store1" });
    expect(screen.queryByRole("combobox")).not.toBeInTheDocument();
    expect(screen.queryByText(/all stores/i)).not.toBeInTheDocument();
    expect(screen.getByRole("button", { name: /refresh/i })).toBeDisabled();
  });

  it("requests owner-wide analytics without a store ID", async () => {
    getAnalyticsSummary.mockResolvedValue(summary);

    render(<FranchisePage currentUser={owner} />);

    expect(screen.getByText("All stores")).toBeInTheDocument();
    await waitFor(() => expect(getAnalyticsSummary).toHaveBeenCalledWith({}));
  });

  it.each([
    ["missing user", undefined],
    ["unsupported role", { id: "employee-1", role: "EMPLOYEE", storeId: "store1" }],
  ])("denies access for a %s without making a request", (_label, currentUser) => {
    render(<FranchisePage currentUser={currentUser} />);

    expect(screen.getByRole("alert")).toHaveTextContent("Access denied");
    expect(getAnalyticsSummary).not.toHaveBeenCalled();
    expect(screen.queryByText("Total Orders")).not.toBeInTheDocument();
  });

  it.each(["", "   ", undefined])("rejects a manager with invalid store ID %s", (storeId) => {
    render(<FranchisePage currentUser={{ id: "manager-1", role: "MANAGER", storeId }} />);

    expect(screen.getByRole("alert")).toHaveTextContent("no assigned store");
    expect(getAnalyticsSummary).not.toHaveBeenCalled();
  });

  it("renders backend metrics, chart values in backend order, and an accessible data table", async () => {
    getAnalyticsSummary.mockResolvedValue(summary);

    render(<FranchisePage currentUser={manager} />);

    expect(await screen.findByText("Total Orders")).toBeInTheDocument();
    expect(screen.getByText("7")).toBeInTheDocument();
    expect(screen.getByText("$272.22")).toBeInTheDocument();
    expect(screen.getByText("$38.89")).toBeInTheDocument();

    const chart = screen.getByRole("region", { name: "Top-Selling Products Chart" });
    const chartNames = within(chart).getAllByText(/Chocolate Chip Cookie|Vanilla Cupcake/);
    expect(chartNames.map((node) => node.textContent)).toEqual([
      "Chocolate Chip Cookie",
      "Vanilla Cupcake",
    ]);
    expect(within(chart).getByText("19 sold")).toBeInTheDocument();
    expect(within(chart).getByText("8 sold")).toBeInTheDocument();

    const table = screen.getByRole("table", { name: "Top-selling products" });
    expect(within(table).getByRole("columnheader", { name: "Product" })).toBeInTheDocument();
    expect(within(table).getByRole("columnheader", { name: "Product ID" })).toBeInTheDocument();
    expect(within(table).getByRole("columnheader", { name: "Quantity Sold" })).toBeInTheDocument();
    expect(within(table).getByRole("rowheader", { name: "Chocolate Chip Cookie" })).toBeInTheDocument();
    expect(within(table).getByText("P008")).toBeInTheDocument();
    expect(within(table).getByText("19")).toBeInTheDocument();
  });

  it("renders zero-order metrics and clear empty chart and table states", async () => {
    getAnalyticsSummary.mockResolvedValue({
      totalOrders: 0,
      revenue: 0,
      averageOrderValue: 0,
      topSellingProducts: [],
    });

    render(<FranchisePage currentUser={manager} />);

    expect(await screen.findByText("No sales have been recorded for this scope yet.")).toBeInTheDocument();
    expect(screen.getByText("0")).toBeInTheDocument();
    expect(screen.getAllByText("$0.00")).toHaveLength(2);
    expect(screen.getByText("No products to chart yet.")).toBeInTheDocument();
    expect(screen.getByRole("cell", { name: "No sales data available." })).toBeInTheDocument();
  });

  it("shows a request error and retries with the same manager scope", async () => {
    vi.spyOn(console, "error").mockImplementation(() => {});
    getAnalyticsSummary
      .mockRejectedValueOnce(new Error("Unavailable"))
      .mockResolvedValueOnce(summary);

    render(<FranchisePage currentUser={manager} />);

    expect(await screen.findByRole("alert")).toHaveTextContent("couldn't load sales analytics");
    fireEvent.click(screen.getByRole("button", { name: "Retry analytics request" }));

    await screen.findByText("Total Orders");
    expect(getAnalyticsSummary).toHaveBeenCalledTimes(2);
    expect(getAnalyticsSummary).toHaveBeenLastCalledWith({ storeId: "store1" });
  });

  it("refreshes the same owner-wide scope", async () => {
    getAnalyticsSummary.mockResolvedValue(summary);

    render(<FranchisePage currentUser={owner} />);

    await screen.findByText("Total Orders");
    fireEvent.click(screen.getByRole("button", { name: "Refresh analytics" }));

    await waitFor(() => expect(getAnalyticsSummary).toHaveBeenCalledTimes(2));
    expect(getAnalyticsSummary).toHaveBeenLastCalledWith({});
  });
});