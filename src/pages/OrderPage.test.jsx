import { cleanup, render, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { OrderPage } from "./OrderPage";

const { getProducts } = vi.hoisted(() => ({
  getProducts: vi.fn(),
}));

vi.mock("../api/client", () => ({
  getProducts,
}));

vi.mock("../components/ProductCatalog", () => ({
  ProductCatalog: ({ products, loading, error }) => (
    <div data-testid="product-catalog">
      <span>{loading ? "loading" : "loaded"}</span>
      <span>{error || "no-error"}</span>
      <span>{products.map((product) => product.name).join(",") || "no-products"}</span>
    </div>
  ),
}));

vi.mock("../components/ConversationalAssistant", () => ({
  ConversationalAssistant: () => <div data-testid="assistant-products">assistant-ready</div>,
}));

describe("OrderPage", () => {
  beforeEach(() => {
    getProducts.mockReset();
  });

  afterEach(() => {
    cleanup();
  });

  it("requests products on load and shows the loading state before the request resolves", async () => {
    getProducts.mockReturnValue(new Promise(() => {}));

    render(<OrderPage />);

    expect(getProducts).toHaveBeenCalledTimes(1);
    expect(screen.getByTestId("product-catalog")).toHaveTextContent("loading");
    expect(screen.getByTestId("product-catalog")).toHaveTextContent("no-products");
  });

  it("renders backend products after a successful request", async () => {
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
    ]);

    render(<OrderPage />);

    await waitFor(() => {
      expect(screen.getByTestId("product-catalog")).toHaveTextContent("loaded");
    });

    expect(screen.getByTestId("product-catalog")).toHaveTextContent("Chocolate Cupcake,Lemon Tart");
    expect(screen.getByTestId("assistant-products")).toHaveTextContent("assistant-ready");
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