import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { CartProvider, useCart } from "../context/CartContext";
import { CartDrawer } from "./CartDrawer";

const { authState } = vi.hoisted(() => ({
  authState: { currentUser: null },
}));

const { createOrder, getCustomerProfile, getNearestStore, getStores, quoteOrder } = vi.hoisted(() => ({
  createOrder: vi.fn(),
  getCustomerProfile: vi.fn(),
  getNearestStore: vi.fn(),
  getStores: vi.fn(),
  quoteOrder: vi.fn(),
}));

vi.mock("../context/AuthContext", () => ({
  useAuth: () => authState,
}));

vi.mock("../api/client", () => ({
  createOrder,
  getCustomerProfile,
  getNearestStore,
  getProductImageUrl: (imageFileName) => `/images/products/${imageFileName}`,
  getStores,
  quoteOrder,
}));

const cupcake = {
  id: "p1",
  name: "Chocolate Cupcake",
  price: 4.5,
  imageFileName: "chocolate-cupcake.jpg",
};

const tart = {
  id: "p2",
  name: "Lemon Tart",
  price: 5.25,
  imageFileName: "lemon-tart.jpg",
};

function CartHarness() {
  const { addItem, openCart } = useCart();

  return (
    <>
      <button onClick={() => addItem(cupcake)} type="button">Add cupcake</button>
      <button onClick={() => addItem(tart)} type="button">Add tart</button>
      <button onClick={openCart} type="button">Open cart</button>
      <CartDrawer />
    </>
  );
}

function renderCart() {
  return render(
    <CartProvider>
      <CartHarness />
    </CartProvider>,
  );
}

describe("CartDrawer", () => {
  beforeEach(() => {
    authState.currentUser = null;
    createOrder.mockReset();
    getCustomerProfile.mockReset();
    getNearestStore.mockReset();
    getStores.mockReset();
    quoteOrder.mockReset();
    quoteOrder.mockResolvedValue({
      fulfillmentType: "LOCAL_DELIVERY",
      storeId: "store24",
      subtotal: 9,
      standardFulfillmentFee: 2.99,
      fulfillmentFee: 2.99,
      promotionSavings: 0,
      promotionApplied: false,
      estimatedTotal: 11.99,
    });
    getCustomerProfile.mockResolvedValue({
      id: "c1",
      name: "Alex Carter",
      email: "customer@frostedcorner.demo",
      phone: "555-0100",
      street: "101 Broadway",
      city: "New York",
      state: "NY",
      zipCode: "10001",
    });
    getStores.mockResolvedValue([
      {
        id: "store1",
        storeName: "Frosted Corner - New York",
        city: "New York",
        state: "NY",
      },
      {
        id: "store24",
        storeName: "Frosted Corner - Portland",
        city: "Portland",
        state: "OR",
      },
    ]);
    getNearestStore.mockResolvedValue({
      nearestStore: { id: "store24" },
      stores: [
        { id: "store24", storeName: "Frosted Corner - Portland", city: "Portland", state: "OR" },
        { id: "store1", storeName: "Frosted Corner - New York", city: "New York", state: "NY" },
      ],
    });
    localStorage.clear();
  });

  afterEach(() => {
    cleanup();
  });

  it("updates quantities and removes products", () => {
    renderCart();

    fireEvent.click(screen.getByRole("button", { name: "Add cupcake" }));
    fireEvent.click(screen.getByRole("button", { name: "Add tart" }));
    fireEvent.click(screen.getByRole("button", { name: "Open cart" }));
    fireEvent.click(screen.getByRole("button", { name: "Increase quantity of Chocolate Cupcake" }));

    expect(screen.getByText("$14.25")).toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: "Remove Lemon Tart from cart" }));

    expect(screen.queryByText("Lemon Tart")).not.toBeInTheDocument();
    expect(screen.getByText("$9.00")).toBeInTheDocument();
  });

  it("shows fulfillment fee and free-shipping promotion before checkout", async () => {
    quoteOrder.mockResolvedValue({
      fulfillmentType: "SHIPPING",
      storeId: "store17",
      subtotal: 31.99,
      standardFulfillmentFee: 4.99,
      fulfillmentFee: 0,
      promotionSavings: 4.99,
      promotionApplied: true,
      estimatedTotal: 31.99,
    });

    renderCart();

    fireEvent.click(screen.getByRole("button", { name: "Add cupcake" }));
    fireEvent.click(screen.getByRole("button", { name: "Increase quantity of Chocolate Cupcake" }));
    fireEvent.click(screen.getByRole("button", { name: "Increase quantity of Chocolate Cupcake" }));
    fireEvent.click(screen.getByRole("button", { name: "Increase quantity of Chocolate Cupcake" }));
    fireEvent.click(screen.getByRole("button", { name: "Increase quantity of Chocolate Cupcake" }));
    fireEvent.click(screen.getByRole("button", { name: "Increase quantity of Chocolate Cupcake" }));
    fireEvent.click(screen.getByRole("button", { name: "Increase quantity of Chocolate Cupcake" }));
    fireEvent.change(screen.getByLabelText("Name"), { target: { value: "Alex Carter" } });
    fireEvent.change(screen.getByLabelText("Email"), { target: { value: "alex@example.com" } });
    fireEvent.change(screen.getByLabelText("Phone"), { target: { value: "555-0100" } });
    fireEvent.change(screen.getByLabelText("Street"), { target: { value: "2490 Burnside Street" } });
    fireEvent.change(screen.getByLabelText("City"), { target: { value: "Portland" } });
    fireEvent.change(screen.getByLabelText("State"), { target: { value: "OR" } });
    fireEvent.change(screen.getByLabelText("ZIP code"), { target: { value: "97205" } });

    await waitFor(() => expect(quoteOrder).toHaveBeenCalled());
    expect(await screen.findByText("Shipping fee")).toBeInTheDocument();
    expect(screen.getByText("$4.99")).toBeInTheDocument();
    expect(screen.getByText("$0.00")).toBeInTheDocument();
    expect(screen.getByText("Free shipping promotion applied — saved $4.99.")).toBeInTheDocument();
    expect(screen.getAllByText("$31.99").length).toBeGreaterThan(0);
  });

  it("submits product IDs and quantities and displays the backend confirmation total", async () => {
    let resolveOrder;
    createOrder.mockReturnValue(new Promise((resolve) => {
      resolveOrder = resolve;
    }));
    renderCart();

    fireEvent.click(screen.getByRole("button", { name: "Add cupcake" }));
    fireEvent.click(screen.getByRole("button", { name: "Increase quantity of Chocolate Cupcake" }));
    fireEvent.change(screen.getByLabelText("Name"), { target: { value: "Alex Carter" } });
    fireEvent.change(screen.getByLabelText("Email"), { target: { value: "alex@example.com" } });
    fireEvent.change(screen.getByLabelText("Phone"), { target: { value: "555-0100" } });
    fireEvent.change(screen.getByLabelText("Street"), { target: { value: "2490 Burnside Street" } });
    fireEvent.change(screen.getByLabelText("City"), { target: { value: "Portland" } });
    fireEvent.change(screen.getByLabelText("State"), { target: { value: "OR" } });
    fireEvent.change(screen.getByLabelText("ZIP code"), { target: { value: "97205" } });
    fireEvent.click(screen.getByRole("button", { name: "PLACE ORDER" }));

    expect(screen.getByRole("button", { name: "PLACING ORDER..." })).toBeDisabled();
    expect(createOrder).toHaveBeenCalledWith(
      {
        customerId: "c1",
        fulfillmentOption: "DELIVERY",
        customer: {
          name: "Alex Carter",
          email: "alex@example.com",
          phone: "555-0100",
          street: "2490 Burnside Street",
          city: "Portland",
          state: "OR",
          zipCode: "97205",
        },
        items: [{ productId: "p1", quantity: 2 }],
      },
    );

    resolveOrder({
      id: "o100",
      status: "CONFIRMED",
      total: 12.34,
      fulfillmentType: "LOCAL_DELIVERY",
      fulfillmentProvider: "DoorDash",
    });

    expect(await screen.findByText("Order confirmed")).toBeInTheDocument();
    expect(screen.getByText("Order o100 is confirmed.")).toBeInTheDocument();
    expect(screen.getByText("Local delivery (DoorDash)")).toBeInTheDocument();
    expect(screen.getByText("$12.34")).toBeInTheDocument();
  });

  it("submits takeout orders with a selected store and no address fields", async () => {
    createOrder.mockResolvedValue({
      id: "o101",
      status: "CONFIRMED",
      total: 4.5,
      fulfillmentType: "TAKEOUT",
      fulfillmentFee: 0,
    });
    renderCart();

    fireEvent.click(screen.getByRole("button", { name: "Add cupcake" }));
    fireEvent.click(screen.getByRole("button", { name: "Open cart" }));
    fireEvent.change(screen.getByLabelText("Name"), { target: { value: "Alex Carter" } });
    fireEvent.change(screen.getByLabelText("Email"), { target: { value: "alex@example.com" } });
    fireEvent.change(screen.getByLabelText("Phone"), { target: { value: "555-0100" } });
    fireEvent.click(screen.getByLabelText("Takeout"));

    await waitFor(() => expect(getStores).toHaveBeenCalled());
    fireEvent.change(screen.getByLabelText("Pick up store"), { target: { value: "store24" } });
    fireEvent.click(screen.getByRole("button", { name: "PLACE ORDER" }));

    expect(createOrder).toHaveBeenCalledWith(
      {
        customerId: "c1",
        fulfillmentOption: "TAKEOUT",
        storeId: "store24",
        customer: {
          name: "Alex Carter",
          email: "alex@example.com",
          phone: "555-0100",
        },
        items: [{ productId: "p1", quantity: 1 }],
      },
    );
    expect(screen.queryByLabelText("Street")).not.toBeInTheDocument();
  });

  it("prefills signed-in customer details and nearest takeout store", async () => {
    authState.currentUser = {
      id: "user-customer-1",
      role: "CUSTOMER",
      customerId: "c1",
    };
    getNearestStore.mockResolvedValue({
      nearestStore: { id: "store1" },
      stores: [
        { id: "store1", storeName: "Frosted Corner - New York", city: "New York", state: "NY" },
        { id: "store24", storeName: "Frosted Corner - Portland", city: "Portland", state: "OR" },
      ],
    });
    createOrder.mockResolvedValue({
      id: "o102",
      status: "CONFIRMED",
      total: 4.5,
      fulfillmentType: "TAKEOUT",
      fulfillmentFee: 0,
    });
    renderCart();

    fireEvent.click(screen.getByRole("button", { name: "Add cupcake" }));
    fireEvent.click(screen.getByRole("button", { name: "Open cart" }));

    await waitFor(() => expect(getCustomerProfile).toHaveBeenCalledWith("c1"));
    await waitFor(() => {
      expect(screen.getByLabelText("Name")).toHaveValue("Alex Carter");
      expect(screen.getByLabelText("Email")).toHaveValue("customer@frostedcorner.demo");
      expect(screen.getByLabelText("Phone")).toHaveValue("555-0100");
      expect(screen.getByLabelText("ZIP code")).toHaveValue("10001");
    });

    fireEvent.click(screen.getByLabelText("Takeout"));

    await waitFor(() => expect(getNearestStore).toHaveBeenCalledWith("10001"));
    await waitFor(() => expect(screen.getByLabelText("Pick up store")).toHaveValue("store1"));
    fireEvent.click(screen.getByRole("button", { name: "PLACE ORDER" }));

    expect(createOrder).toHaveBeenCalledWith(
      {
        customerId: "c1",
        fulfillmentOption: "TAKEOUT",
        storeId: "store1",
        customer: {
          name: "Alex Carter",
          email: "customer@frostedcorner.demo",
          phone: "555-0100",
        },
        items: [{ productId: "p1", quantity: 1 }],
      },
    );
  });

  it("clears the confirmation view after completing an order and reopening the cart", async () => {
    createOrder.mockResolvedValue({
      id: "o103",
      status: "CONFIRMED",
      total: 4.5,
      fulfillmentType: "TAKEOUT",
      fulfillmentFee: 0,
    });
    renderCart();

    fireEvent.click(screen.getByRole("button", { name: "Add cupcake" }));
    fireEvent.click(screen.getByRole("button", { name: "Open cart" }));
    fireEvent.change(screen.getByLabelText("Name"), { target: { value: "Alex Carter" } });
    fireEvent.change(screen.getByLabelText("Email"), { target: { value: "alex@example.com" } });
    fireEvent.change(screen.getByLabelText("Phone"), { target: { value: "555-0100" } });
    fireEvent.click(screen.getByRole("button", { name: "PLACE ORDER" }));

    expect(await screen.findByText("Order confirmed")).toBeInTheDocument();
    fireEvent.click(screen.getByRole("button", { name: "CONTINUE SHOPPING" }));
    fireEvent.click(screen.getByRole("button", { name: "Open cart" }));

    expect(screen.queryByText("Order confirmed")).not.toBeInTheDocument();
    expect(screen.getByText("Your cart is empty.")).toBeInTheDocument();
  });

  it("sorts takeout stores closest-to-farthest when coordinates are available", async () => {
    getNearestStore.mockResolvedValue({
      nearestStore: { id: "store24" },
      stores: [
        {
          id: "store24",
          storeName: "Frosted Corner - Portland",
          city: "Portland",
          state: "OR",
        },
        {
          id: "store1",
          storeName: "Frosted Corner - New York",
          city: "New York",
          state: "NY",
        },
      ],
    });
    renderCart();

    fireEvent.click(screen.getByRole("button", { name: "Add cupcake" }));
    fireEvent.click(screen.getByRole("button", { name: "Open cart" }));
    fireEvent.click(screen.getByLabelText("Takeout"));
    await waitFor(() => expect(getStores).toHaveBeenCalled());
    fireEvent.change(screen.getByLabelText("ZIP code for nearby stores"), {
      target: { value: "97205" },
    });

    await waitFor(() => expect(getNearestStore).toHaveBeenCalledWith("97205"));
    expect(screen.getByLabelText("Pick up store")).toHaveValue("store24");
    const options = screen.getAllByRole("option");
    expect(options.some((option) => option.textContent?.includes("Frosted Corner - Portland"))).toBe(true);
    expect(screen.getByText("Showing stores closest-to-farthest for ZIP 97205.")).toBeInTheDocument();
  });

  it("shows a checkout error and keeps the cart available for retry", async () => {
    createOrder.mockRejectedValue(new Error("Not enough inventory for Chocolate Cupcake."));
    renderCart();

    fireEvent.click(screen.getByRole("button", { name: "Add cupcake" }));
    fireEvent.change(screen.getByLabelText("Name"), { target: { value: "Alex Carter" } });
    fireEvent.change(screen.getByLabelText("Email"), { target: { value: "alex@example.com" } });
    fireEvent.change(screen.getByLabelText("Phone"), { target: { value: "555-0100" } });
    fireEvent.click(screen.getByRole("button", { name: "PLACE ORDER" }));

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "Not enough inventory for Chocolate Cupcake.",
    );
    expect(screen.getByText("Chocolate Cupcake")).toBeInTheDocument();
    await waitFor(() => expect(screen.getByRole("button", { name: "PLACE ORDER" })).toBeEnabled());
  });
});