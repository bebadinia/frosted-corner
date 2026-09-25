import { Plus } from "lucide-react";
import { getProductImageUrl } from "../api/client";
import { useCart } from "../context/CartContext";
import { useToast } from "../context/ToastContext";

function isSoldOut(product, availabilityByProductId) {
  const stock = availabilityByProductId[product.id];
  return stock ? stock.quantity <= 0 : false;
}

function buildRecommendation(products, availabilityByProductId) {
  const availableProducts = products.filter(
    (product) => !isSoldOut(product, availabilityByProductId),
  );

  if (availableProducts.length === 0) {
    return null;
  }

  const seasonalProduct = availableProducts.find((product) => product.category === "Seasonal");
  const featuredProduct = seasonalProduct || availableProducts[0];

  return {
    product: featuredProduct,
    title: featuredProduct.name,
    reason: "Featured from the current " + featuredProduct.category + " collection.",
    price: featuredProduct.price,
  };
}

function StockCaption({ stock }) {
  if (!stock) {
    return null;
  }

  if (stock.quantity <= 0) {
    return <span className="text-xs font-bold uppercase tracking-wider text-muted-foreground">Sold out</span>;
  }

  if (stock.quantity <= stock.lowStockThreshold) {
    return <span className="text-xs font-bold text-amber-700">Low inventory — {stock.quantity} left</span>;
  }

  return null;
}

function productCardClass(soldOut) {
  return soldOut
    ? "flex gap-4 rounded-xl border border-border bg-muted/70 p-4 opacity-60 grayscale"
    : "flex gap-4 rounded-xl border border-border bg-white p-4 transition-colors hover:border-primary";
}

export function ProductCatalog({
  products,
  loading,
  error,
  availabilityByProductId = {},
  availabilityLoading = false,
}) {
  const { addItem } = useCart();
  const { showToast } = useToast();
  const recommendation = buildRecommendation(products, availabilityByProductId);

  if (loading) {
    return (
      <div className="rounded-2xl border border-border bg-white p-8 shadow-sm">
        <p className="text-sm font-medium text-muted-foreground">Loading products from the backend catalog...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="rounded-2xl border border-red-200 bg-red-50 p-8 shadow-sm">
        <p className="text-sm font-medium text-red-700">{error}</p>
      </div>
    );
  }

  return (
    <div className="flex-1">
      <div className="mb-6 flex flex-wrap items-end justify-between gap-4">
        <div>
          <h2 className="font-serif text-3xl font-bold">Order Online</h2>
          <p className="mt-2 text-sm text-muted-foreground">
            Live product catalog sourced from Spring Boot. Prices and active products stay backend-owned.
          </p>
          {availabilityLoading ? (
            <p className="mt-1 text-xs text-muted-foreground">Checking store inventory…</p>
          ) : null}
        </div>
        <div className="rounded-full border border-border px-4 py-2 text-xs font-bold uppercase tracking-wider text-muted-foreground">
          {products.length} active desserts
        </div>
      </div>

      {recommendation ? (
        <div className="mb-10 rounded-2xl border border-border bg-secondary/50 p-6">
          <span className="rounded bg-primary px-2 py-1 text-[10px] font-bold uppercase tracking-wider text-white">
            Featured Pick
          </span>
          <div className="mt-4 flex flex-wrap items-center gap-4 rounded-xl border border-border bg-white p-4">
            <div className="flex-1">
              <h3 className="font-serif text-xl font-bold">{recommendation.title}</h3>
              <p className="mt-1 text-sm text-muted-foreground">{recommendation.reason}</p>
              <div className="mt-2">
                <StockCaption stock={availabilityByProductId[recommendation.product.id]} />
              </div>
            </div>
            <button
              className="rounded bg-primary px-4 py-2 text-xs font-bold text-white transition-colors hover:bg-accent"
              onClick={() => {
                addItem(recommendation.product);
                showToast(recommendation.product.name + " added to cart.");
              }}
              type="button"
            >
              Add - ${recommendation.price.toFixed(2)}
            </button>
          </div>
        </div>
      ) : null}

      <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
        {products.map((product) => {
          const stock = availabilityByProductId[product.id];
          const soldOut = stock ? stock.quantity <= 0 : false;

          return (
            <article key={product.id} className={productCardClass(soldOut)}>
              <img
                alt={product.name}
                className="h-24 w-24 shrink-0 rounded bg-muted object-cover"
                src={getProductImageUrl(product.imageFileName)}
              />
              <div className="flex flex-1 flex-col justify-between gap-3">
                <div>
                  <div className="mb-2 text-[10px] font-bold uppercase tracking-[0.2em] text-muted-foreground">
                    {product.category}
                  </div>
                  <h3 className="font-serif text-xl font-bold leading-tight">{product.name}</h3>
                  <p className="mt-2 text-sm text-muted-foreground">{product.description}</p>
                  <div className="mt-2">
                    <StockCaption stock={stock} />
                  </div>
                </div>
                <div className="flex items-center justify-between">
                  <span className="font-semibold">${product.price.toFixed(2)}</span>
                  <button
                    aria-label={soldOut ? product.name + " sold out" : "Add " + product.name + " to cart"}
                    className="flex h-8 w-8 items-center justify-center rounded-full bg-secondary text-primary transition-colors hover:bg-primary hover:text-white disabled:cursor-not-allowed disabled:opacity-50 disabled:hover:bg-secondary disabled:hover:text-primary"
                    disabled={soldOut}
                    onClick={() => {
                      addItem(product);
                      showToast(product.name + " added to cart.");
                    }}
                    type="button"
                  >
                    <Plus className="h-4 w-4" />
                  </button>
                </div>
              </div>
            </article>
          );
        })}
      </div>
    </div>
  );
}