import { useEffect, useMemo, useState } from "react";
import { getNearestStore, getProductAvailability, getProducts } from "../api/client";
import { ConversationalAssistant } from "../components/ConversationalAssistant";
import { ProductCatalog } from "../components/ProductCatalog";

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
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [selectedInventoryZip, setSelectedInventoryZip] = useState(INVENTORY_LOCATIONS[0].zip);
  const [inventoryStore, setInventoryStore] = useState(null);
  const [availability, setAvailability] = useState([]);
  const [availabilityLoading, setAvailabilityLoading] = useState(true);
  const [availabilityError, setAvailabilityError] = useState("");

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
      <div className="mb-8 flex flex-wrap items-end justify-between gap-4 rounded-2xl border border-border bg-white p-5 shadow-sm">
        <div>
          <div className="text-xs font-bold uppercase tracking-[0.18em] text-primary">Demo inventory location</div>
          <p className="mt-1 text-sm text-muted-foreground">
            Product stock below is loaded from the nearest store for this selected ZIP.
          </p>
          {inventoryStore ? (
            <p className="mt-1 text-xs font-semibold text-foreground">
              Using {inventoryStore.storeName}
            </p>
          ) : null}
        </div>
        <label className="text-xs font-bold uppercase tracking-wider text-muted-foreground">
          Inventory location
          <select
            className="ml-3 rounded border border-border bg-background px-3 py-2 text-sm font-medium normal-case tracking-normal text-foreground"
            onChange={(event) => setSelectedInventoryZip(event.target.value)}
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
