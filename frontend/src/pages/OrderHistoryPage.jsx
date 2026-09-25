import { useEffect, useState } from "react";
import { getCustomerOrderHistory } from "../api/client";
import { useAuth } from "../context/AuthContext";

const currencyFormatter = new Intl.NumberFormat("en-US", {
  style: "currency",
  currency: "USD",
});

const dateFormatter = new Intl.DateTimeFormat("en-US", {
  month: "short",
  day: "numeric",
  year: "numeric",
});

export function OrderHistoryPage() {
  const { currentUser } = useAuth();
  const [history, setHistory] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!currentUser?.customerId) {
      return;
    }

    let isCurrent = true;
    setError("");

    getCustomerOrderHistory(currentUser.customerId)
      .then((result) => {
        if (isCurrent) {
          setHistory(result);
        }
      })
      .catch((requestError) => {
        if (isCurrent) {
          setError(requestError.message || "Unable to load your order history.");
        }
      });

    return () => {
      isCurrent = false;
    };
  }, [currentUser?.customerId]);

  return (
    <div className="mx-auto w-full max-w-6xl px-6 py-12">
      <div className="mb-8">
        <div className="text-xs font-bold uppercase tracking-[0.2em] text-primary">Customer Account</div>
        <h1 className="mt-2 font-serif text-4xl font-bold">Order History</h1>
        <p className="mt-2 text-sm text-muted-foreground">
          Review your previous Frosted Corner orders and the items you order most often.
        </p>
      </div>

      {error ? (
        <p className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700" role="alert">
          {error}
        </p>
      ) : null}

      {!history && !error ? (
        <p className="rounded-xl border border-border bg-white p-6 text-sm text-muted-foreground">Loading order history…</p>
      ) : null}

      {history ? (
        <div className="space-y-10">
          <section>
            <h2 className="font-serif text-2xl font-bold">Most Ordered Items</h2>
            <div className="mt-5 grid gap-4 md:grid-cols-3">
              {history.favoriteItems.length > 0 ? history.favoriteItems.map((item, index) => (
                <div key={item.productId} className="rounded-xl border border-border bg-white p-5 shadow-sm">
                  <div className="text-xs font-bold uppercase tracking-wider text-primary">#{index + 1} favorite</div>
                  <h3 className="mt-2 font-serif text-xl font-bold">{item.productName}</h3>
                  <p className="mt-2 text-sm text-muted-foreground">{item.quantityOrdered} ordered across your history</p>
                </div>
              )) : (
                <p className="text-sm text-muted-foreground">No favorite items yet.</p>
              )}
            </div>
          </section>

          <section>
            <h2 className="font-serif text-2xl font-bold">Previous Orders</h2>
            <div className="mt-5 space-y-4">
              {history.orders.length > 0 ? history.orders.map((order) => (
                <article key={order.id} className="rounded-2xl border border-border bg-white p-6 shadow-sm">
                  <div className="flex flex-wrap items-start justify-between gap-4">
                    <div>
                      <div className="text-xs font-bold uppercase tracking-wider text-muted-foreground">
                        {order.createdAt ? dateFormatter.format(new Date(order.createdAt)) : "Order date unavailable"}
                      </div>
                      <h3 className="mt-1 font-serif text-xl font-bold">Order {order.id}</h3>
                      <p className="mt-1 text-sm text-muted-foreground">
                        {order.fulfillmentType?.replaceAll("_", " ")} · {order.status}
                      </p>
                    </div>
                    <div className="text-right">
                      <div className="text-xs font-bold uppercase tracking-wider text-muted-foreground">Total</div>
                      <div className="mt-1 font-serif text-2xl font-bold">{currencyFormatter.format(order.total)}</div>
                    </div>
                  </div>

                  <div className="mt-5 border-t border-border pt-4">
                    <ul className="space-y-2">
                      {order.items.map((item) => (
                        <li key={item.productId} className="flex justify-between gap-4 text-sm">
                          <span>{item.quantity} × {item.productName}</span>
                          <span className="font-semibold">{currencyFormatter.format(item.lineTotal)}</span>
                        </li>
                      ))}
                    </ul>
                  </div>
                </article>
              )) : (
                <p className="rounded-xl border border-border bg-white p-6 text-sm text-muted-foreground">No previous orders yet.</p>
              )}
            </div>
          </section>
        </div>
      ) : null}
    </div>
  );
}
