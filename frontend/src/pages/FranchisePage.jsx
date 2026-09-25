import { useCallback, useEffect, useState } from "react";
import { getAnalyticsSummary } from "../api/client";
import { useAuth } from "../context/AuthContext";

const INVENTORY_ROWS = [
  { id: "inv-1", item: "Classic Chocolate Cake", category: "Cakes", stock: 14, threshold: 10, unit: "cakes" },
  { id: "inv-2", item: "Vanilla Sprinkle Cupcake", category: "Cupcakes", stock: 36, threshold: 24, unit: "cupcakes" },
  { id: "inv-3", item: "Chocolate Chip Cookie", category: "Cookies", stock: 72, threshold: 40, unit: "cookies" },
  { id: "inv-4", item: "Pastry Boxes", category: "Supplies", stock: 120, threshold: 50, unit: "boxes" },
];

function InventoryStatus({ stock, threshold }) {
  if (stock <= threshold * 0.6) {
    return <span className="font-bold text-red-700">Critical</span>;
  }

  if (stock <= threshold) {
    return <span className="font-bold text-red-600">Low</span>;
  }

  return <span className="font-bold text-green-600">Healthy</span>;
}

const currencyFormatter = new Intl.NumberFormat("en-US", {
  style: "currency",
  currency: "USD",
});

function AnalyticsSummary({ summary }) {
  const cards = [
    { label: "Total Orders", value: summary.totalOrders },
    { label: "Total Revenue", value: currencyFormatter.format(summary.revenue) },
    { label: "Average Order Value", value: currencyFormatter.format(summary.averageOrderValue) },
  ];

  return (
    <div className="grid grid-cols-1 gap-6 md:grid-cols-3">
      {cards.map((card) => (
        <div key={card.label} className="rounded-xl border border-border bg-white p-6 shadow-sm">
          <h3 className="mb-2 font-sans text-sm font-bold uppercase tracking-wider text-muted-foreground">
            {card.label}
          </h3>
          <p className="font-serif text-3xl font-bold">{card.value}</p>
        </div>
      ))}
    </div>
  );
}

function TopSellingProducts({ products }) {
  const maxQuantity = Math.max(...products.map((product) => product.quantitySold), 1);

  return (
    <div className="mt-8 grid gap-8 lg:grid-cols-2">
      <section aria-labelledby="top-products-chart-heading">
        <h3 id="top-products-chart-heading" className="font-serif text-xl font-bold">
          Top-Selling Products Chart
        </h3>
        {products.length === 0 ? (
          <p className="mt-4 text-sm text-muted-foreground">No products to chart yet.</p>
        ) : (
          <div className="mt-5 space-y-5">
            {products.map((product) => (
              <div key={product.productId}>
                <div className="mb-2 flex items-baseline justify-between gap-4 text-sm">
                  <span className="font-bold">{product.name}</span>
                  <span className="shrink-0 font-medium">{product.quantitySold} sold</span>
                </div>
                <div className="h-4 w-full overflow-hidden rounded-full bg-muted" aria-hidden="true">
                  <div
                    className="h-full min-w-1 rounded-full bg-primary"
                    style={{ width: `${(product.quantitySold / maxQuantity) * 100}%` }}
                  />
                </div>
              </div>
            ))}
          </div>
        )}
      </section>

      <section aria-labelledby="top-products-table-heading">
        <h3 id="top-products-table-heading" className="font-serif text-xl font-bold">
          Top-Selling Products Data
        </h3>
        <div className="mt-5 overflow-x-auto rounded-lg border border-border">
          <table aria-label="Top-selling products" className="w-full min-w-[28rem] text-left text-sm">
            <thead className="bg-background text-xs font-bold uppercase tracking-wider text-muted-foreground">
              <tr>
                <th className="px-4 py-3" scope="col">Product</th>
                <th className="px-4 py-3" scope="col">Product ID</th>
                <th className="px-4 py-3" scope="col">Quantity Sold</th>
              </tr>
            </thead>
            <tbody>
              {products.length === 0 ? (
                <tr className="border-t border-border">
                  <td className="px-4 py-4 text-muted-foreground" colSpan="3">No sales data available.</td>
                </tr>
              ) : products.map((product) => (
                <tr key={product.productId} className="border-t border-border">
                  <th className="px-4 py-3 font-bold" scope="row">{product.name}</th>
                  <td className="px-4 py-3">{product.productId}</td>
                  <td className="px-4 py-3">{product.quantitySold}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}

export function FranchisePage({ currentUser: currentUserOverride }) {
  const { currentUser: sessionUser } = useAuth();
  const currentUser = currentUserOverride ?? sessionUser;
  const isManager = currentUser?.role === "MANAGER";
  const isOwner = currentUser?.role === "OWNER";
  const managerStoreId = isManager && typeof currentUser.storeId === "string"
    ? currentUser.storeId.trim()
    : "";
  const accessDenied = !isManager && !isOwner;
  const configurationError = isManager && !managerStoreId;
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const loadAnalytics = useCallback(async () => {
    if (accessDenied || configurationError) {
      return;
    }

    setLoading(true);
    setError("");

    try {
      const result = await getAnalyticsSummary({
        ...(isManager ? { storeId: managerStoreId } : {}),
      });
      setSummary(result);
    } catch (requestError) {
      console.error("Unable to load analytics.", requestError);
      setSummary(null);
      setError("We couldn't load sales analytics right now. Please try again.");
    } finally {
      setLoading(false);
    }
  }, [accessDenied, configurationError, isManager, managerStoreId]);

  useEffect(() => {
    loadAnalytics();
  }, [loadAnalytics]);

  return (
    <div className="mx-auto w-full max-w-6xl p-8">
      <div className="mb-8">
        <div>
          <h1 className="font-serif text-3xl font-bold">Store Overview</h1>
          <p className="mt-1 text-sm font-medium text-muted-foreground">
            Franchise sales insights and inventory snapshot.
          </p>
        </div>
      </div>

      <section className="mb-8 rounded-xl border border-border bg-background/50 p-6" aria-labelledby="sales-analytics-heading">
        <div className="mb-6 flex flex-wrap items-center justify-between gap-4">
          <div>
            <h2 id="sales-analytics-heading" className="font-serif text-2xl font-bold">Sales Analytics</h2>
            {!accessDenied && !configurationError ? (
              <p className="mt-1 text-sm font-medium text-muted-foreground">
                {isManager ? `Store: ${managerStoreId}` : "All stores"}
              </p>
            ) : null}
          </div>
          {!accessDenied && !configurationError ? (
            <button
              className="rounded bg-primary px-4 py-2 text-sm font-bold text-white transition-colors hover:bg-accent disabled:cursor-not-allowed disabled:opacity-60"
              disabled={loading}
              onClick={loadAnalytics}
              type="button"
            >
              {loading ? "Refreshing…" : "Refresh analytics"}
            </button>
          ) : null}
        </div>

        {accessDenied ? (
          <div role="alert" className="rounded-lg border border-border bg-white p-5">
            Access denied. Sales analytics are available only to managers and owners.
          </div>
        ) : configurationError ? (
          <div role="alert" className="rounded-lg border border-border bg-white p-5">
            Analytics cannot be loaded because this manager has no assigned store.
          </div>
        ) : loading && !summary ? (
          <p role="status" aria-live="polite" className="rounded-lg bg-white p-5">Loading sales analytics…</p>
        ) : error ? (
          <div role="alert" className="rounded-lg border border-red-300 bg-white p-5">
            <p>{error}</p>
            <button className="mt-4 rounded bg-primary px-4 py-2 text-sm font-bold text-white hover:bg-accent" onClick={loadAnalytics} type="button">
              Retry analytics request
            </button>
          </div>
        ) : summary ? (
          <>
            <AnalyticsSummary summary={summary} />
            {summary.totalOrders === 0 ? (
              <p role="status" className="mt-6 rounded-lg bg-white p-4 text-sm font-medium">
                No sales have been recorded for this scope yet.
              </p>
            ) : null}
            <TopSellingProducts products={summary.topSellingProducts} />
          </>
        ) : null}
      </section>

      <section className="overflow-hidden rounded-xl border border-border bg-white shadow-sm">
        <div className="flex flex-wrap items-center justify-between gap-2 border-b border-border bg-background/50 p-6">
          <h2 className="font-serif text-xl font-bold">Inventory Snapshot</h2>
          <span className="rounded bg-secondary px-2 py-1 text-xs font-bold text-primary">Frontend placeholder data</span>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full min-w-[42rem] text-left text-sm">
            <thead className="bg-background text-[10px] font-bold uppercase tracking-wider text-muted-foreground">
              <tr>
                <th className="px-6 py-4">Item</th>
                <th className="px-6 py-4">Category</th>
                <th className="px-6 py-4">Stock Level</th>
                <th className="px-6 py-4">Status</th>
              </tr>
            </thead>
            <tbody>
              {INVENTORY_ROWS.map((row) => (
                <tr key={row.id} className="border-t border-border">
                  <td className="px-6 py-4 font-bold">{row.item}</td>
                  <td className="px-6 py-4 text-muted-foreground">{row.category}</td>
                  <td className="px-6 py-4 font-medium">
                    {row.stock} {row.unit}
                  </td>
                  <td className="px-6 py-4">
                    <InventoryStatus stock={row.stock} threshold={row.threshold} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}