import { useEffect, useState } from "react";
import { getProducts } from "../api/client";
import { useCart } from "../context/CartContext";

const PLAN_DETAILS = {
  P021: ["4 hand-picked desserts", "Weekend delivery window", "Preference-based picks"],
  P022: ["12 desserts plus 1 cake", "Delivery on demand", "Seasonal swaps"],
  P023: ["Premium rotating menu", "Priority planning for events", "Franchise pickup coordination"],
};

function CheckIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" className="h-4 w-4 shrink-0 text-primary">
      <polyline points="20 6 9 17 4 12" />
    </svg>
  );
}

function planCardClass(popular) {
  return popular
    ? "relative rounded-2xl border-2 border-primary bg-primary/5 p-8 shadow-xl transition-shadow"
    : "relative rounded-2xl border border-border bg-white p-8 transition-shadow hover:shadow-xl";
}

function addButtonClass(popular) {
  return popular
    ? "w-full rounded bg-primary py-3 text-sm font-bold tracking-widest text-white transition-colors hover:bg-accent"
    : "w-full rounded border-2 border-primary py-3 text-sm font-bold tracking-widest text-primary transition-colors hover:bg-primary hover:text-white";
}

export function SubscriptionsPage() {
  const { addItem, openCart } = useCart();
  const [plans, setPlans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let isCurrent = true;

    getProducts()
      .then((products) => {
        if (isCurrent) {
          setPlans(products.filter((product) => product.category === "Subscriptions"));
        }
      })
      .catch(() => {
        if (isCurrent) {
          setError("We couldn't load subscription plans right now.");
        }
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

  const handleAddPlan = (plan) => {
    addItem(plan, 1);
    openCart();
  };

  return (
    <div className="mx-auto w-full max-w-7xl px-6 py-16 text-center">
      <div className="mb-3 text-xs font-bold uppercase tracking-[0.2em] text-primary">Subscriptions</div>
      <h1 className="font-serif text-4xl font-bold">Dessert Plans</h1>
      <p className="mx-auto mb-16 mt-4 max-w-2xl text-sm font-medium leading-7 text-muted-foreground">
        Choose a monthly dessert plan and add it to your cart. Pricing comes from the same backend product catalog used by checkout.
      </p>

      {loading ? <p className="text-sm text-muted-foreground">Loading subscription plans…</p> : null}
      {error ? <p className="text-sm font-medium text-red-700" role="alert">{error}</p> : null}

      {!loading && !error ? (
        <div className="grid grid-cols-1 gap-8 text-left md:grid-cols-3">
          {plans.map((plan) => {
            const features = PLAN_DETAILS[plan.id] || [plan.description];
            const popular = plan.id === "P022";

            return (
              <section key={plan.id} className={planCardClass(popular)}>
                {popular ? (
                  <span className="absolute -top-3 left-1/2 -translate-x-1/2 rounded-full bg-primary px-4 py-1 text-xs font-bold uppercase tracking-widest text-white">
                    Most Popular
                  </span>
                ) : null}
                <h2 className="font-serif text-2xl font-bold">{plan.name}</h2>
                <p className="mb-6 mt-2 text-xl font-bold text-primary">
                  {Number(plan.price).toFixed(2)} <span className="text-sm font-normal text-muted-foreground">/ month</span>
                </p>
                <ul className="mb-8 space-y-3 text-sm font-medium text-foreground/80">
                  {features.map((feature) => (
                    <li key={feature} className="flex items-center gap-2">
                      <CheckIcon />
                      {feature}
                    </li>
                  ))}
                </ul>
                <button
                  className={addButtonClass(popular)}
                  onClick={() => handleAddPlan(plan)}
                  type="button"
                >
                  ADD TO CART
                </button>
              </section>
            );
          })}
        </div>
      ) : null}
    </div>
  );
}
