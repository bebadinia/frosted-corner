import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { OrderPage } from "./OrderPage";

const { getProducts, getNearestStore, getProductAvailability } = vi.hoisted(() => ({
  getProducts: vi.fn(),
  getNearestStore: vi.fn(),
  getProductAvailability: vi.fn(),
}));

vi.mock("../api/client", () => ({
  getProducts,
  getNearestStore,
  getProductAvailability,
}));

vi.mock("../components/ProductCatalog", () => ({
  ProductCatalog: ({ products, loading, error, availabilityByProductId, availabilityLoading }) => (
    <div data-testid="product-catalog">
      <span>{loading ? "loading" : "loaded"}</span>
      <span>{error || "no-error"}</span>
      <span>{products.map((product) => product.name).join(",") || "no-products"}</span>
      <span>{availabilityLoading ? "inventory-loading" : "inventory-loaded"}</span>
      <span>{availabilityByProductId.p1?.quantity ?? "no-stock"}</span>
    </div>
  ),
}));

vi.mock("../components/ConversationalAssistant", () => ({
  ConversationalAssistant: () => <div data-testid="assistant-products">assistant-ready</div>,
}));

describe("OrderPage", () => {
  beforeEach(() => {
    getProducts.mockReset();
    getNearestStore.mockReset();
    getProductAvailability.mockReset();

    getNearestStore.mockResolvedValue({
      nearestStore: {
        id: "store24",
        storeName: "Frosted Corner - Portland",
      },
    });
    getProductAvailability.mockResolvedValue([
      { productId: "p1", quantity: 4, lowStockThreshold: 10 },
    ]);
  });

  afterEach(() => {
    cleanup();
  });

  it("requests products on load and shows the loading state before the request resolves", () => {
    getProducts.mockReturnValue(new Promise(() => {}));

    render(<OrderPage />);

    expect(getProducts).toHaveBeenCalledTimes(1);
    expect(screen.getByTestId("product-catalog")).toHaveTextContent("loading");
    expect(screen.getByTestId("product-catalog")).toHaveTextContent("no-products");
  });

  it("renders dessert products and store inventory after successful requests", async () => {
    getProducts.mockResolvedValue([
      {
        id: "p1",
        name: "Chocolate Cupcake",
        description: "Chocolate cupcake with frosting",
        price: 4.5,
        category: "Cupcakes",
        imageFileName: "chocolate-cupcake.jpg",
        active: true,
      },
      {
        id: "p2",
        name: "Lemon Tart",
        description: "Lemon tart with toasted meringue",
        price: 5.25,
        category: "Tarts",
        imageFileName: "lemon-tart.jpg",
        active: true,
      },
      {
        id: "P021",
        name: "The Starter Subscription",
        description: "Monthly dessert plan",
        price: 25,
        category: "Subscriptions",
        imageFileName: "placeholder.svg",
        active: true,
      },
    ]);

    render(<OrderPage />);

    await waitFor(() => {
      expect(screen.getByTestId("product-catalog")).toHaveTextContent("inventory-loaded");
    });

    expect(screen.getByTestId("product-catalog")).toHaveTextContent("Chocolate Cupcake,Lemon Tart");
    expect(screen.getByTestId("product-catalog")).not.toHaveTextContent("The Starter Subscription");
    expect(screen.getByTestId("product-catalog")).toHaveTextContent("4");
    expect(getNearestStore).toHaveBeenCalledWith("97205");
    expect(getProductAvailability).toHaveBeenCalledWith("store24");
  });

  it("reloads inventory when the demo inventory location changes", async () => {
    getProducts.mockResolvedValue([]);
    getNearestStore.mockImplementation((zip) => Promise.resolve({
      nearestStore: zip === "98101"
        ? { id: "store17", storeName: "Frosted Corner - Seattle" }
        : { id: "store24", storeName: "Frosted Corner - Portland" },
    }));
    getProductAvailability.mockResolvedValue([]);

    render(<OrderPage />);

    await waitFor(() => expect(getProductAvailability).toHaveBeenCalledWith("store24"));

    fireEvent.change(screen.getByLabelText("Inventory location"), {
      target: { value: "98101" },
    });

    await waitFor(() => {
      expect(getNearestStore).toHaveBeenCalledWith("98101");
      expect(getProductAvailability).toHaveBeenCalledWith("store17");
    });
  });

  it("shows a user-friendly error state when the product request fails", async () => {
    getProducts.mockRejectedValue(new Error("Product request failed with status 500."));

    const consoleErrorSpy = vi.spyOn(console, "error").mockImplementation(() => {});

    render(<OrderPage />);

    await waitFor(() => {
      expect(screen.getByTestId("product-catalog")).toHaveTextContent("loaded");
    });

    expect(screen.getByTestId("product-catalog")).toHaveTextContent(
      "We couldn't load the menu right now. Please try again in a moment.",
    );
    expect(screen.getByTestId("product-catalog")).toHaveTextContent("no-products");

    consoleErrorSpy.mockRestore();
  });
});
