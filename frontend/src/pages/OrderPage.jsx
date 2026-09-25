import { useEffect, useMemo, useState } from "react";
import {
  getCustomerProfile,
  getCustomerRecommendations,
  getNearestStore,
  getProductAvailability,
  getProductImageUrl,
  getProducts,
} from "../api/client";
import { ConversationalAssistant } from "../components/ConversationalAssistant";
import { ProductCatalog } from "../components/ProductCatalog";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";

const INVENTORY_LOCATIONS = [
  { zip: "97205", label: "Portland, OR" },
  { zip: "98101", label: "Seattle, WA" },
  { zip: "10001", label: "New York, NY" },
  { zip: "60601", label: "Chicago, IL" },
  { zip: "33130", label: "Miami, FL" },
];

function getProductErrorMessage() {
  return "We couldn't load the menu right now. Please try again in a moment.";
}

export function OrderPage() {
  const { currentUser } = useAuth();
  const { addItem } = useCart();
  const [products, setProducts] = useState([]);
  const [recommendations, setRecommendations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [selectedInventoryZip, setSelectedInventoryZip] = useState(INVENTORY_LOCATIONS[0].zip);
  const [inventoryStore, setInventoryStore] = useState(null);
  const [availability, setAvailability] = useState([]);
  const [availabilityLoading, setAvailabilityLoading] = useState(true);
  const [availabilityError, setAvailabilityError] = useState("");
  const [usingSavedLocation, setUsingSavedLocation] = useState(false);

  useEffect(() => {
    let isCurrent = true;

    setLoading(true);
    setError("");

    getProducts()
      .then((catalog) => {
        if (!isCurrent) {
          return;
        }

        setProducts(catalog.filter((product) => product.category !== "Subscriptions"));
      })
      .catch((requestError) => {
        if (!isCurrent) {
          return;
        }

        console.error("Failed to load products", requestError);
        setError(getProductErrorMessage());
      })
      .finally(() => {
        if (isCurrent) {
          setLoading(false);
        }
      });

    return () => {
      isCurrent = false;
    };
  }, []);

  useEffect(() => {
    if (currentUser?.role !== "CUSTOMER" || !currentUser.customerId) {
      setRecommendations([]);
      setUsingSavedLocation(false);
      return undefined;
    }

    let isCurrent = true;

    Promise.all([
      getCustomerProfile(currentUser.customerId),
      getCustomerRecommendations(currentUser.customerId),
    ])
      .then(([profile, suggestedProducts]) => {
        if (!isCurrent) {
          return;
        }

        setRecommendations(
          suggestedProducts.filter((product) => product.category !== "Subscriptions"),
        );

        const savedLocation = INVENTORY_LOCATIONS.find(
          (location) => location.zip === profile.zipCode,
        );

        if (savedLocation) {
          setSelectedInventoryZip(savedLocation.zip);
          setUsingSavedLocation(true);
        }
      })
      .catch((requestError) => {
        if (!isCurrent) {
          return;
        }

        console.error("Unable to load personalized ordering context.", requestError);
        setRecommendations([]);
      });

    return () => {
      isCurrent = false;
    };
  }, [currentUser?.customerId, currentUser?.role]);

  useEffect(() => {
    let isCurrent = true;

    setAvailabilityLoading(true);
    setAvailabilityError("");

    getNearestStore(selectedInventoryZip)
      .then(async (result) => {
        const nearestStore = result.nearestStore;
        const storeAvailability = await getProductAvailability(nearestStore.id);

        if (!isCurrent) {
          return;
        }

        setInventoryStore(nearestStore);
        setAvailability(storeAvailability);
      })
      .catch((requestError) => {
        if (!isCurrent) {
          return;
        }

        console.error("Failed to load inventory availability", requestError);
        setInventoryStore(null);
        setAvailability([]);
        setAvailabilityError("We couldn't load inventory for this demo location.");
      })
      .finally(() => {
        if (isCurrent) {
          setAvailabilityLoading(false);
        }
      });

    return () => {
      isCurrent = false;
    };
  }, [selectedInventoryZip]);

  const availabilityByProductId = useMemo(
    () => Object.fromEntries(
      availability.map((item) => [item.productId, item]),
    ),
    [availability],
  );

  return (
    <div className="mx-auto w-full max-w-7xl px-6 py-12">
      {currentUser?.role === "CUSTOMER" && recommendations.length > 0 ? (
        <section className="mb-8 rounded-2xl border border-primary/20 bg-secondary/40 p-6">
          <div className="text-xs font-bold uppercase tracking-[0.18em] text-primary">
            Picked for you
          </div>
          <h2 className="mt-2 font-serif text-2xl font-bold">Based on your order history</h2>
          <p className="mt-1 text-sm text-muted-foreground">
            These are active products you have ordered most often.
          </p>
          <div className="mt-5 grid gap-4 md:grid-cols-3">
            {recommendations.map((product) => {
              const stock = availabilityByProductId[product.id];
              const soldOut = stock ? stock.quantity <= 0 : false;

              return (
                <article key={product.id} className="flex items-center gap-4 rounded-xl border border-border bg-white p-4">
                  <img
                    alt={product.name}
                    className="h-16 w-16 rounded bg-muted object-cover"
                    src={getProductImageUrl(product.imageFileName)}
                  />
                  <div className="min-w-0 flex-1">
                    <h3 className="font-serif text-lg font-bold">{product.name}</h3>
                    <p className="mt-1 text-sm font-semibold text-primary">
                      ${Number(product.price).toFixed(2)}
                    </p>
                    <button
                      className="mt-2 text-xs font-bold uppercase tracking-wider text-primary hover:text-accent disabled:cursor-not-allowed disabled:text-muted-foreground"
                      disabled={soldOut}
                      onClick={() => addItem(product)}
                      type="button"
                    >
                      {soldOut ? "Sold out" : "Add to cart"}
                    </button>
                  </div>
                </article>
              );
            })}
          </div>
        </section>
      ) : null}

      <div className="mb-8 flex flex-wrap items-end justify-between gap-4 rounded-2xl border border-border bg-white p-5 shadow-sm">
        <div>
          <div className="text-xs font-bold uppercase tracking-[0.18em] text-primary">Demo inventory location</div>
          <p className="mt-1 text-sm text-muted-foreground">
            Product stock below is loaded from the nearest store for this selected ZIP.
          </p>
          {inventoryStore ? (
            <p className="mt-1 text-xs font-semibold text-foreground">
              Using {inventoryStore.storeName}
              {usingSavedLocation ? " — selected from your saved customer ZIP" : ""}
            </p>
          ) : null}
        </div>
        <label className="text-xs font-bold uppercase tracking-wider text-muted-foreground">
          Inventory location
          <select
            className="ml-3 rounded border border-border bg-background px-3 py-2 text-sm font-medium normal-case tracking-normal text-foreground"
            onChange={(event) => {
              setSelectedInventoryZip(event.target.value);
              setUsingSavedLocation(false);
            }}
            value={selectedInventoryZip}
          >
            {INVENTORY_LOCATIONS.map((location) => (
              <option key={location.zip} value={location.zip}>
                {location.label}
              </option>
            ))}
          </select>
        </label>
      </div>

      {availabilityError ? (
        <p className="mb-6 rounded border border-red-200 bg-red-50 p-3 text-sm text-red-700" role="alert">
          {availabilityError}
        </p>
      ) : null}

      <div className="flex flex-col gap-12 lg:flex-row">
        <ProductCatalog
          availabilityByProductId={availabilityByProductId}
          availabilityLoading={availabilityLoading}
          error={error}
          loading={loading}
          products={products}
        />
        <ConversationalAssistant />
      </div>
    </div>
  );
}
