import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { StoreFinderPanel } from "./StoreFinderPanel";

const { getNearestStore } = vi.hoisted(() => ({
  getNearestStore: vi.fn(),
}));

vi.mock("../api/client", () => ({
  getNearestStore,
}));

describe("StoreFinderPanel", () => {
  afterEach(() => {
    getNearestStore.mockReset();
  });

  it("loads the nearest store and sorted preview for an approved demo ZIP", async () => {
    getNearestStore.mockResolvedValue({
      nearestStore: {
        id: "store24",
        storeName: "Frosted Corner - Portland",
        street: "2490 Burnside Street",
        city: "Portland",
        state: "OR",
        zipCode: "97205",
        managerName: "Sage Peterson",
        distanceMiles: 0,
      },
      stores: [
        {
          id: "store24",
          storeName: "Frosted Corner - Portland",
          city: "Portland",
          state: "OR",
          distanceMiles: 0,
        },
        {
          id: "store17",
          storeName: "Frosted Corner - Seattle",
          city: "Seattle",
          state: "WA",
          distanceMiles: 145.42,
        },
        {
          id: "store37",
          storeName: "Frosted Corner - Miami",
          city: "Miami",
          state: "FL",
          distanceMiles: 2726.18,
        },
      ],
    });

    render(<StoreFinderPanel />);

    fireEvent.click(screen.getByRole("button", { name: "Find nearest store" }));

    await screen.findByRole("heading", { name: "Frosted Corner - Portland" });
    expect(getNearestStore).toHaveBeenCalledWith("97205");
    expect(screen.getByText("2490 Burnside Street, Portland, OR 97205")).toBeInTheDocument();
    expect(screen.getByText("0.00 miles")).toBeInTheDocument();
    expect(screen.getByText("145.42 mi")).toBeInTheDocument();
    expect(screen.getByText("2726.18 mi")).toBeInTheDocument();
  });

  it("shows backend validation errors to the user", async () => {
    getNearestStore.mockRejectedValue(new Error("Unsupported demo address or ZIP: 99999"));

    render(<StoreFinderPanel />);

    fireEvent.click(screen.getByRole("button", { name: "Find nearest store" }));

    await waitFor(() => expect(screen.getByRole("alert")).toHaveTextContent(
      "Unsupported demo address or ZIP: 99999",
    ));
  });

  it("sends the user-selected approved demo ZIP", async () => {
    getNearestStore.mockResolvedValue({
      nearestStore: {
        id: "store17",
        storeName: "Frosted Corner - Seattle",
        street: "1755 Pine Street",
        city: "Seattle",
        state: "WA",
        zipCode: "98101",
        managerName: "Harper Adams",
        distanceMiles: 0,
      },
      stores: [
        {
          id: "store17",
          storeName: "Frosted Corner - Seattle",
          city: "Seattle",
          state: "WA",
          distanceMiles: 0,
        },
      ],
    });

    render(<StoreFinderPanel />);

    fireEvent.change(screen.getByLabelText("Demo customer location"), {
      target: { value: "98101" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Find nearest store" }));

    await screen.findByRole("heading", { name: "Frosted Corner - Seattle" });
    expect(getNearestStore).toHaveBeenCalledWith("98101");
  });
});