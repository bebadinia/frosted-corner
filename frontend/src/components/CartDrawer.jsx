import { CheckCircle2, LoaderCircle, Minus, Plus, ShoppingBag, X } from "lucide-react";
import { useEffect, useState } from "react";
import {
  createOrder,
  getCustomerProfile,
  getNearestStore,
  getProductImageUrl,
  getStores,
} from "../api/client";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";

const guestCustomerId = import.meta.env.VITE_CUSTOMER_ID || "c1";
function createInitialCheckoutForm() {
  return {
    fulfillmentOption: "DELIVERY",
    name: "",
    email: "",
    phone: "",
    street: "",
    city: "",
    state: "",
    zipCode: "",
    takeoutSortLocation: "",
    storeId: "",
  };
}

function formatFulfillmentLabel(order) {
  if (order.fulfillmentType === "LOCAL_DELIVERY") {
    return `Local delivery (${order.fulfillmentProvider})`;
  }

  if (order.fulfillmentType === "SHIPPING") {
    return "Shipping";
  }

  return "Takeout";
}

export function CartDrawer() {
  const { currentUser } = useAuth();
  const {
    items,
    updateQuantity,
    removeItem,
    clearCart,
    totalItems,
    totalPrice,
    isOpen,
    closeCart,
  } = useCart();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [checkoutError, setCheckoutError] = useState("");
  const [confirmedOrder, setConfirmedOrder] = useState(null);
  const [checkoutForm, setCheckoutForm] = useState(createInitialCheckoutForm);
  const [baseStores, setBaseStores] = useState([]);
  const [storeOptions, setStoreOptions] = useState([]);
  const [storesError, setStoresError] = useState("");
  const [isLoadingStores, setIsLoadingStores] = useState(false);
  const customerId = currentUser?.role === "CUSTOMER" && currentUser.customerId
    ? currentUser.customerId
    : guestCustomerId;
  const takeoutSortZip = checkoutForm.takeoutSortLocation.trim();

  const handleCloseCart = () => {
    closeCart();

    if (confirmedOrder) {
      setConfirmedOrder(null);
      setCheckoutError("");
      setCheckoutForm(createInitialCheckoutForm());
    }
  };

  useEffect(() => {
    if (items.length > 0 && confirmedOrder) {
      setConfirmedOrder(null);
    }
  }, [confirmedOrder, items.length]);

  useEffect(() => {
    if (!isOpen || currentUser?.role !== "CUSTOMER" || !currentUser.customerId) {
      return undefined;
    }

    let isCurrent = true;

    getCustomerProfile(currentUser.customerId)
      .then((profile) => {
        if (!isCurrent) {
          return;
        }

        setCheckoutForm((currentForm) => ({
          ...currentForm,
          name: profile.name || "",
          email: profile.email || "",
          phone: profile.phone || "",
          street: profile.street || "",
          city: profile.city || "",
          state: profile.state || "",
          zipCode: profile.zipCode || "",
          takeoutSortLocation: profile.zipCode || "",
        }));
      })
      .catch((requestError) => {
        console.error("Unable to prefill checkout from customer profile.", requestError);
      });

    return () => {
      isCurrent = false;
    };
  }, [currentUser?.customerId, currentUser?.role, isOpen]);

  useEffect(() => {
    if (!isOpen || baseStores.length > 0) {
      return;
    }

    let isCurrent = true;
    setIsLoadingStores(true);
    setStoresError("");

    getStores()
      .then((stores) => {
        if (!isCurrent) {
          return;
        }

        setBaseStores(stores);
        setStoreOptions(stores);
        setCheckoutForm((currentForm) => ({
          ...currentForm,
          storeId: currentForm.storeId || stores[0]?.id || "",
        }));
      })
      .catch((requestError) => {
        if (!isCurrent) {
          return;
        }

        setStoresError(requestError instanceof Error ? requestError.message : "Unable to load stores.");
      })
      .finally(() => {
        if (isCurrent) {
          setIsLoadingStores(false);
        }
      });

    return () => {
      isCurrent = false;
    };
  }, [baseStores.length, isOpen]);

  useEffect(() => {
    if (!isOpen || !/^\d{5}$/.test(takeoutSortZip)) {
      setStoreOptions(baseStores);
      return;
    }

    let isCurrent = true;
    setIsLoadingStores(true);
    setStoresError("");

    getNearestStore(takeoutSortZip)
      .then((result) => {
        if (!isCurrent) {
          return;
        }

        setStoreOptions(result.stores);
        setCheckoutForm((currentForm) => ({
          ...currentForm,
          storeId: result.nearestStore?.id || result.stores[0]?.id || "",
        }));
      })
      .catch((requestError) => {
        if (!isCurrent) {
          return;
        }

        setStoresError(requestError instanceof Error ? requestError.message : "Unable to sort stores.");
      })
      .finally(() => {
        if (isCurrent) {
          setIsLoadingStores(false);
        }
      });

    return () => {
      isCurrent = false;
    };
  }, [baseStores, isOpen, takeoutSortZip]);

  const handleFieldChange = (event) => {
    const { name, value } = event.target;
    setCheckoutForm((currentForm) => ({
      ...currentForm,
      [name]: value,
    }));
  };

  const handleCheckout = async () => {
    if (items.length === 0 || isSubmitting) {
      return;
    }

    setIsSubmitting(true);
    setCheckoutError("");

    try {
      const isTakeout = checkoutForm.fulfillmentOption === "TAKEOUT";
      const order = await createOrder(
        {
          customerId,
          fulfillmentOption: checkoutForm.fulfillmentOption,
          ...(isTakeout ? { storeId: checkoutForm.storeId } : {}),
          customer: {
            name: checkoutForm.name,
            email: checkoutForm.email,
            phone: checkoutForm.phone,
            ...(isTakeout
              ? {}
              : {
                  street: checkoutForm.street,
                  city: checkoutForm.city,
                  state: checkoutForm.state,
                  zipCode: checkoutForm.zipCode,
                }),
          },
          items: items.map(({ product, quantity }) => ({
            productId: product.id,
            quantity,
          })),
        },
      );

      setConfirmedOrder(order);
      clearCart();
      setCheckoutForm(createInitialCheckoutForm());
    } catch (requestError) {
      setCheckoutError(
        requestError instanceof Error
          ? requestError.message
          : "Checkout failed. Please review your cart and try again.",
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <>
      <div
        aria-hidden="true"
        className={`fixed inset-0 z-40 bg-black/40 transition-opacity ${
          isOpen ? "pointer-events-auto opacity-100" : "pointer-events-none opacity-0"
        }`}
        onClick={handleCloseCart}
      />
      <aside
        aria-label="Shopping cart"
        className={`fixed right-0 top-0 z-50 flex h-full w-full max-w-sm flex-col overflow-hidden bg-white shadow-2xl transition-transform duration-300 ${
          isOpen ? "translate-x-0" : "translate-x-full"
        }`}
        aria-modal="true"
        role="dialog"
      >
        <div className="flex items-center justify-between border-b border-border p-6">
          <h2 className="flex items-center gap-2 font-serif text-2xl font-bold">
            <ShoppingBag className="h-5 w-5 text-primary" />
            Your Cart
          </h2>
          <button aria-label="Close cart" className="text-muted-foreground hover:text-primary" onClick={handleCloseCart} type="button">
            <X className="h-5 w-5" />
          </button>
        </div>

        <div className="flex min-h-0 flex-1 flex-col overflow-y-auto p-6">
          {confirmedOrder ? (
            <div className="my-auto text-center" role="status">
              <CheckCircle2 className="mx-auto h-12 w-12 text-primary" />
              <h3 className="mt-4 font-serif text-2xl font-bold">Order confirmed</h3>
              <p className="mt-2 text-sm text-muted-foreground">
                Order {confirmedOrder.id} is {confirmedOrder.status.toLowerCase()}.
              </p>
              <p className="mt-2 text-sm text-muted-foreground">
                {formatFulfillmentLabel(confirmedOrder)}
              </p>
              <p className="mt-6 text-xs font-bold uppercase tracking-wider text-muted-foreground">
                Confirmed total
              </p>
              <p className="mt-1 font-serif text-3xl font-bold">
                ${Number(confirmedOrder.total).toFixed(2)}
              </p>
            </div>
          ) : null}
          {items.length === 0 && !confirmedOrder ? (
            <p className="mt-12 text-center text-sm text-muted-foreground">Your cart is empty.</p>
          ) : null}
          {!confirmedOrder ? items.map(({ product, quantity }) => (
            <div key={product.id} className="mb-4 flex items-center gap-3 border-b border-border pb-4 last:mb-0">
              <img
                alt={product.name}
                className="h-16 w-16 shrink-0 rounded-md bg-muted object-cover"
                src={getProductImageUrl(product.imageFileName)}
              />
              <div className="min-w-0 flex-1">
                <h4 className="truncate text-sm font-bold text-foreground">{product.name}</h4>
                <p className="text-xs text-muted-foreground">${product.price.toFixed(2)} each</p>
                <div className="mt-2 flex items-center gap-2">
                  <button
                    aria-label={`Decrease quantity of ${product.name}`}
                    className="flex h-6 w-6 items-center justify-center rounded-full bg-secondary text-primary transition-colors hover:bg-primary hover:text-white"
                    onClick={() => updateQuantity(product.id, quantity - 1)}
                    type="button"
                  >
                    <Minus className="h-3 w-3" />
                  </button>
                  <span className="w-4 text-center text-sm font-semibold">{quantity}</span>
                  <button
                    aria-label={`Increase quantity of ${product.name}`}
                    className="flex h-6 w-6 items-center justify-center rounded-full bg-secondary text-primary transition-colors hover:bg-primary hover:text-white"
                    onClick={() => updateQuantity(product.id, quantity + 1)}
                    type="button"
                  >
                    <Plus className="h-3 w-3" />
                  </button>
                </div>
              </div>
              <button
                aria-label={`Remove ${product.name} from cart`}
                className="text-xs font-bold uppercase tracking-wider text-muted-foreground hover:text-primary"
                onClick={() => removeItem(product.id)}
                type="button"
              >
                Remove
              </button>
            </div>
          )) : null}
          {confirmedOrder ? (
            <div className="mt-6 border-t border-border pt-6">
            <button
              className="w-full rounded bg-primary py-3 text-sm font-bold tracking-widest text-white transition-colors hover:bg-accent"
              onClick={handleCloseCart}
              type="button"
            >
              CONTINUE SHOPPING
            </button>
            </div>
          ) : (
            <div className="mt-6 border-t border-border pt-6">
              <div className="mb-4 flex items-center justify-between text-lg font-bold">
                <span>Subtotal</span>
                <span>${totalPrice.toFixed(2)}</span>
              </div>
              <div className="mb-4 min-w-0 rounded border border-border bg-background p-4 text-sm">
                <div className="font-bold text-foreground">Fulfillment</div>
                <p className="mt-1 break-words text-muted-foreground">
                  Choose delivery or takeout. Delivery pricing and timing are confirmed at checkout based on your nearest store.
                </p>
                <div className="mt-4 flex flex-wrap gap-3">
                  <label className="flex items-center gap-2">
                    <input
                      checked={checkoutForm.fulfillmentOption === "DELIVERY"}
                      name="fulfillmentOption"
                      onChange={handleFieldChange}
                      type="radio"
                      value="DELIVERY"
                    />
                    <span>Delivery</span>
                  </label>
                  <label className="flex items-center gap-2">
                    <input
                      checked={checkoutForm.fulfillmentOption === "TAKEOUT"}
                      name="fulfillmentOption"
                      onChange={handleFieldChange}
                      type="radio"
                      value="TAKEOUT"
                    />
                    <span>Takeout</span>
                  </label>
                </div>
                <div className="mt-4 grid gap-3">
                  <label className="grid min-w-0 gap-1 text-sm font-medium text-foreground">
                    Name
                    <input
                      className="w-full min-w-0 rounded border border-border px-3 py-2"
                      name="name"
                      onChange={handleFieldChange}
                      required
                      type="text"
                      value={checkoutForm.name}
                    />
                  </label>
                  <label className="grid min-w-0 gap-1 text-sm font-medium text-foreground">
                    Email
                    <input
                      className="w-full min-w-0 rounded border border-border px-3 py-2"
                      name="email"
                      onChange={handleFieldChange}
                      required
                      type="email"
                      value={checkoutForm.email}
                    />
                  </label>
                  <label className="grid min-w-0 gap-1 text-sm font-medium text-foreground">
                    Phone
                    <input
                      className="w-full min-w-0 rounded border border-border px-3 py-2"
                      name="phone"
                      onChange={handleFieldChange}
                      required
                      type="tel"
                      value={checkoutForm.phone}
                    />
                  </label>
                </div>

                {checkoutForm.fulfillmentOption === "DELIVERY" ? (
                  <div className="mt-4 grid gap-3">
                    <label className="grid min-w-0 gap-1 text-sm font-medium text-foreground">
                      Street
                      <input
                        className="w-full min-w-0 rounded border border-border px-3 py-2"
                        name="street"
                        onChange={handleFieldChange}
                        required
                        type="text"
                        value={checkoutForm.street}
                      />
                    </label>
                    <label className="grid min-w-0 gap-1 text-sm font-medium text-foreground">
                      City
                      <input
                        className="w-full min-w-0 rounded border border-border px-3 py-2"
                        name="city"
                        onChange={handleFieldChange}
                        required
                        type="text"
                        value={checkoutForm.city}
                      />
                    </label>
                    <div className="grid gap-3 sm:grid-cols-2">
                      <label className="grid min-w-0 gap-1 text-sm font-medium text-foreground">
                        State
                        <input
                          className="w-full min-w-0 rounded border border-border px-3 py-2"
                          name="state"
                          onChange={handleFieldChange}
                          required
                          type="text"
                          value={checkoutForm.state}
                        />
                      </label>
                      <label className="grid min-w-0 gap-1 text-sm font-medium text-foreground">
                        ZIP code
                        <input
                          className="w-full min-w-0 rounded border border-border px-3 py-2"
                          inputMode="numeric"
                          maxLength="5"
                          name="zipCode"
                          onChange={handleFieldChange}
                          required
                          type="text"
                          value={checkoutForm.zipCode}
                        />
                      </label>
                    </div>
                  </div>
                ) : (
                  <div className="mt-4 grid gap-3">
                    <label className="grid min-w-0 gap-1 text-sm font-medium text-foreground">
                      ZIP code for nearby stores
                      <input
                        className="w-full min-w-0 rounded border border-border px-3 py-2"
                        inputMode="numeric"
                        maxLength="5"
                        name="takeoutSortLocation"
                        onChange={handleFieldChange}
                        type="text"
                        value={checkoutForm.takeoutSortLocation}
                      />
                    </label>
                    <label className="grid min-w-0 gap-1 text-sm font-medium text-foreground">
                      Pick up store
                      <select
                        className="w-full min-w-0 rounded border border-border px-3 py-2"
                        name="storeId"
                        onChange={handleFieldChange}
                        required
                        value={checkoutForm.storeId}
                      >
                        {storeOptions.map((store) => (
                          <option key={store.id} value={store.id}>
                            {store.storeName} - {store.city}, {store.state}
                          </option>
                        ))}
                      </select>
                    </label>
                    {isLoadingStores ? <p className="text-xs text-muted-foreground">Loading store options…</p> : null}
                    {/^\d{5}$/.test(takeoutSortZip) ? (
                      <p className="text-xs text-muted-foreground">Showing stores closest-to-farthest for ZIP {takeoutSortZip}.</p>
                    ) : (
                      <p className="text-xs text-muted-foreground">Enter a 5-digit ZIP to sort stores by distance. Delivery address is not required for takeout.</p>
                    )}
                  </div>
                )}
              </div>
              {checkoutError ? (
                <p className="mb-4 rounded border border-red-200 bg-red-50 p-3 text-sm text-red-700" role="alert">
                  {checkoutError}
                </p>
              ) : null}
              {storesError ? (
                <p className="mb-4 rounded border border-red-200 bg-red-50 p-3 text-sm text-red-700" role="alert">
                  {storesError}
                </p>
              ) : null}
              <div className="grid gap-3">
                <button
                  className="flex w-full items-center justify-center gap-2 rounded bg-primary py-3 text-sm font-bold tracking-widest text-white transition-colors hover:bg-accent disabled:cursor-not-allowed disabled:opacity-40"
                  disabled={items.length === 0 || isSubmitting || (checkoutForm.fulfillmentOption === "TAKEOUT" && !checkoutForm.storeId)}
                  onClick={handleCheckout}
                  type="button"
                >
                  {isSubmitting ? <LoaderCircle className="h-4 w-4 animate-spin" /> : null}
                  {isSubmitting ? "PLACING ORDER..." : "PLACE ORDER"}
                </button>
                {items.length > 0 ? (
                  <button
                    className="w-full rounded border border-border py-3 text-sm font-semibold text-muted-foreground transition-colors hover:border-primary hover:text-primary"
                    disabled={isSubmitting}
                    onClick={clearCart}
                    type="button"
                  >
                    Clear Cart
                  </button>
                ) : null}
              </div>
            </div>
          )}
        </div>
      </aside>
    </>
  );
}