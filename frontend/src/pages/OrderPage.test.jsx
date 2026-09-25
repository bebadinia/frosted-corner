import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { OrderPage } from "./OrderPage";

const {
  authState,
  addItem,
  getCustomerProfile,
  getCustomerRecommendations,
  getProducts,
  getNearestStore,
  getProductAvailability,
} = vi.hoisted(() => ({
  authState: { currentUser: null },
  addItem: vi.fn(),
  getCustomerProfile: vi.fn(),
  getCustomerRecommendations: vi.fn(),
  getProducts: vi.fn(),
  getNearestStore: vi.fn(),
  getProductAvailability: vi.fn(),
}));

vi.mock("../context/AuthContext", () => ({
  useAuth: () => authState,
}));

vi.mock("../context/CartContext", () => ({
  useCart: () => ({ addItem }),
}));

vi.mock("../api/client", () => ({
  getCustomerProfile,
  getCustomerRecommendations,
  getProducts,
  getNearestStore,
  getProductAvailability,
  getProductImageUrl: (imageFileName) => "/images/products/" + imageFileName,
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
    authState.currentUser = null;
    addItem.mockReset();
    getCustomerProfile.mockReset();
    getCustomerRecommendations.mockReset();
    getProducts.mockReset();
    getNearestStore.mockReset();
    getProductAvailability.mockReset();

    getCustomerProfile.mockResolvedValue({ id: "c1", zipCode: "10001" });
    getCustomerRecommendations.mockResolvedValue([]);
    getNearestStore.mockResolvedValue({
      nearestStore: {
        id: "store24",
        storeName: "Frosted Corner - Portland",
        zipCode: "97205",
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
    expect(getNearestStore).toHaveBeenCalledWith("97205");
    expect(getProductAvailability).toHaveBeenCalledWith("store24");
  });

  it("uses signed-in customer history and saved ZIP for personalized ordering", async () => {
    authState.currentUser = {
      id: "user-customer",
      role: "CUSTOMER",
      customerId: "c1",
    };
    getProducts.mockResolvedValue([]);
    getCustomerProfile.mockResolvedValue({
      id: "c1",
      zipCode: "10001",
    });
    getCustomerRecommendations.mockResolvedValue([
      {
        id: "P005",
        name: "Chocolate Fudge Cupcake",
        description: "Chocolate cupcake",
        price: 4.49,
        category: "Cupcakes",
        imageFileName: "cupcake.jpg",
        active: true,
      },
    ]);
    getNearestStore.mockImplementation((zip) => Promise.resolve({
      nearestStore: zip === "10001"
        ? { id: "store1", storeName: "Frosted Corner - New York", zipCode: "10001" }
        : { id: "store24", storeName: "Frosted Corner - Portland", zipCode: "97205" },
    }));
    getProductAvailability.mockResolvedValue([
      { productId: "P005", quantity: 9, lowStockThreshold: 3 },
    ]);

    render(<OrderPage />);

    await waitFor(() => {
      expect(screen.getByLabelText("Inventory location")).toHaveValue("10001");
    });
    await waitFor(() => expect(getProductAvailability).toHaveBeenCalledWith("store1"));

    expect(screen.getByText("Based on your order history")).toBeInTheDocument();
    expect(screen.getByText("Chocolate Fudge Cupcake")).toBeInTheDocument();
    expect(screen.getByText(/selected from your saved customer ZIP/)).toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: "Add to cart" }));
    expect(addItem).toHaveBeenCalledWith(expect.objectContaining({ id: "P005" }));
  });

  it("reloads inventory when the demo inventory location changes", async () => {
    getProducts.mockResolvedValue([]);
    getNearestStore.mockImplementation((zip) => Promise.resolve({
      nearestStore: zip === "98101"
        ? { id: "store17", storeName: "Frosted Corner - Seattle", zipCode: "98101" }
        : { id: "store24", storeName: "Frosted Corner - Portland", zipCode: "97205" },
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

    consoleErrorSpy.mockRestore();
  });
});
