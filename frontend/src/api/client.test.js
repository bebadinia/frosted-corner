import { afterEach, describe, expect, it, vi } from "vitest";
import {
  chatWithAssistant,
  createOrder,
  getAnalyticsSummary, getCustomerOrderHistory, getCustomerProfile,
  getCustomerRecommendations, getNearestStore, getProductAvailability, getStores,
  getCurrentUser,
  login,
  logout,
  register,
} from "./client";

describe("getAnalyticsSummary", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("requests owner-wide analytics without a store scope", async () => {
    const summary = { totalOrders: 7 };
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue(summary),
    });
    vi.stubGlobal("fetch", fetchMock);

    await expect(getAnalyticsSummary()).resolves.toEqual(summary);
    expect(fetchMock).toHaveBeenCalledWith("/api/analytics/summary", { credentials: "include" });
  });

  it("encodes manager store scope without sending role information", async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue({ totalOrders: 2 }),
    });
    vi.stubGlobal("fetch", fetchMock);

    await getAnalyticsSummary({
      storeId: "store/one & two",
      role: "MANAGER",
    });

    expect(fetchMock).toHaveBeenCalledWith(
      "/api/analytics/summary?storeId=store%2Fone%20%26%20two",
      { credentials: "include" },
    );
    expect(JSON.stringify(fetchMock.mock.calls)).not.toContain("MANAGER");
  });

  it("throws a useful error for an unsuccessful response", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue({ ok: false, status: 503 }));

    await expect(getAnalyticsSummary()).rejects.toThrow(
      "Analytics request failed with status 503.",
    );
  });
});

describe("customer account requests", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("loads the signed-in customer's profile with session credentials", async () => {
    const profile = { id: "c1", name: "Alex Carter" };
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue(profile),
    });
    vi.stubGlobal("fetch", fetchMock);

    await expect(getCustomerProfile("c1")).resolves.toEqual(profile);
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/customers/c1",
      { credentials: "include" },
    );
  });

  it("loads personalized suggestions for the linked customer", async () => {
    const recommendations = [{ id: "P005", name: "Chocolate Fudge Cupcake" }];
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue(recommendations),
    });
    vi.stubGlobal("fetch", fetchMock);

    await expect(getCustomerRecommendations("c1")).resolves.toEqual(recommendations);
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/customers/c1/recommendations",
      { credentials: "include" },
    );
  });

  it("loads order history for the linked customer", async () => {
    const history = { orders: [{ id: "demo-order-001" }], favoriteItems: [] };
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue(history),
    });
    vi.stubGlobal("fetch", fetchMock);

    await expect(getCustomerOrderHistory("c1")).resolves.toEqual(history);
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/customers/c1/orders",
      { credentials: "include" },
    );
  });
});

describe("createOrder", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("posts the documented order request without a frontend total", async () => {
    const order = {
      customerId: "c1",
      fulfillmentOption: "TAKEOUT",
      storeId: "store1",
      customer: {
        name: "Alex Carter",
        email: "alex@example.com",
        phone: "555-0100",
      },
      items: [{ productId: "p1", quantity: 2 }],
    };
    const responseOrder = { id: "o100", status: "CONFIRMED", total: 9 };
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue(responseOrder),
    });
    vi.stubGlobal("fetch", fetchMock);

    await expect(createOrder(order)).resolves.toEqual(responseOrder);
    expect(fetchMock).toHaveBeenCalledWith("/api/orders", {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(order),
    });
  });

  it("uses a backend error message when checkout is rejected", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue({
      ok: false,
      json: vi.fn().mockResolvedValue({ message: "Insufficient inventory." }),
    }));

    await expect(createOrder({ items: [] }, "user1")).rejects.toThrow(
      "Insufficient inventory.",
    );
  });
});

describe("getProductAvailability", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("loads read-only inventory for the selected store", async () => {
    const availability = [{ productId: "P001", quantity: 4, lowStockThreshold: 10 }];
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue(availability),
    });
    vi.stubGlobal("fetch", fetchMock);

    await expect(getProductAvailability("store24")).resolves.toEqual(availability);
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/products/availability?storeId=store24",
      { credentials: "include" },
    );
  });
});

describe("getStores", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("loads the selectable seeded store list", async () => {
    const stores = [{ id: "store1" }, { id: "store24" }];
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue(stores),
    });
    vi.stubGlobal("fetch", fetchMock);

    await expect(getStores()).resolves.toEqual(stores);
    expect(fetchMock).toHaveBeenCalledWith("/api/locations");
  });

  it("throws a useful error when the store list request fails", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue({ ok: false, status: 500 }));

    await expect(getStores()).rejects.toThrow(
      "Store list request failed with status 500.",
    );
  });
});

describe("getNearestStore", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("requests the new location ranking endpoint with the selected demo value", async () => {
    const nearestStore = { nearestStore: { id: "store24" } };
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue(nearestStore),
    });
    vi.stubGlobal("fetch", fetchMock);

    await expect(getNearestStore("97205")).resolves.toEqual(nearestStore);
    expect(fetchMock).toHaveBeenCalledWith("/api/locations/nearest?demoAddressOrZip=97205");
  });

  it("uses backend validation text when the selected demo input is unsupported", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue({
      ok: false,
      json: vi.fn().mockResolvedValue({ message: "Unsupported demo address or ZIP: 99999" }),
    }));

    await expect(getNearestStore("99999")).rejects.toThrow(
      "Unsupported demo address or ZIP: 99999",
    );
  });
});

describe("chatWithAssistant", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("posts conversational messages to the backend assistant endpoint", async () => {
    const assistantResponse = { message: "Here is a chocolate option." };
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue(assistantResponse),
    });
    vi.stubGlobal("fetch", fetchMock);

    await expect(chatWithAssistant({
      customerId: "c1",
      storeId: "store1",
      message: "Show me something chocolate.",
      recommendedProductIds: ["P005"],
    })).resolves.toEqual(assistantResponse);

    expect(fetchMock).toHaveBeenCalledWith("/api/assistant/chat", {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        customerId: "c1",
        storeId: "store1",
        message: "Show me something chocolate.",
        recommendedProductIds: ["P005"],
      }),
    });
  });

  it("uses a backend error message when the assistant rejects a request", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue({
      ok: false,
      json: vi.fn().mockResolvedValue({ message: "message is required" }),
    }));

    await expect(chatWithAssistant({ customerId: "c1", message: "" })).rejects.toThrow(
      "message is required",
    );
  });
});

describe("session authentication", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("submits credentials and reads the session user with cookies included", async () => {
    const user = { id: "user-manager", role: "MANAGER", storeId: "store1" };
    const fetchMock = vi.fn()
      .mockResolvedValueOnce({ ok: true, json: vi.fn().mockResolvedValue(user) })
      .mockResolvedValueOnce({ ok: true, json: vi.fn().mockResolvedValue(user) });
    vi.stubGlobal("fetch", fetchMock);

    await expect(login("manager@frostedcorner.demo", "demo-password")).resolves.toEqual(user);
    await expect(getCurrentUser()).resolves.toEqual(user);

    expect(fetchMock).toHaveBeenNthCalledWith(1, "/api/auth/login", {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email: "manager@frostedcorner.demo", password: "demo-password" }),
    });
    expect(fetchMock).toHaveBeenNthCalledWith(2, "/api/auth/me", { credentials: "include" });
  });

  it("treats unauthenticated session lookups as a guest and logs out with cookies", async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce({ ok: false, status: 401 })
      .mockResolvedValueOnce({ ok: true });
    vi.stubGlobal("fetch", fetchMock);

    await expect(getCurrentUser()).resolves.toBeNull();
    await expect(logout()).resolves.toBeUndefined();
    expect(fetchMock).toHaveBeenLastCalledWith("/api/auth/logout", {
      method: "POST",
      credentials: "include",
    });
  });

  it("registers a new customer account with credentials included", async () => {
    const user = { id: "user-1", role: "CUSTOMER", customerId: "customer-1" };
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue(user),
    });
    vi.stubGlobal("fetch", fetchMock);

    await expect(register("New Customer", "new@example.com", "safe-password"))
      .resolves.toEqual(user);
    expect(fetchMock).toHaveBeenCalledWith("/api/auth/register", {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        name: "New Customer",
        email: "new@example.com",
        password: "safe-password",
      }),
    });
  });
});