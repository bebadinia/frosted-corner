const SUBSCRIPTION_PLANS = [
  {
    id: "starter",
    name: "The Starter",
    price: 25,
    features: ["4 hand-picked desserts", "Weekend delivery window", "Preference-based picks"],
  },
  {
    id: "family",
    name: "Family Box",
    price: 45,
    features: ["12 desserts plus 1 cake", "Delivery on demand", "Seasonal swaps"],
    popular: true,
  },
  {
    id: "artisan",
    name: "Artisan Tier",
    price: 80,
    features: ["Premium rotating menu", "Priority planning for events", "Franchise pickup coordination"],
  },
];

function CheckIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" className="h-4 w-4 shrink-0 text-primary">
      <polyline points="20 6 9 17 4 12" />
    </svg>
  );
}

export function SubscriptionsPage() {
  return (
    <div className="mx-auto w-full max-w-7xl px-6 py-16 text-center">
      <div className="mb-3 text-xs font-bold uppercase tracking-[0.2em] text-primary">Subscriptions</div>
      <h1 className="font-serif text-4xl font-bold">Dessert Plans</h1>
      <p className="mx-auto mb-16 mt-4 max-w-2xl text-sm font-medium leading-7 text-muted-foreground">
        This page maps the attached subscription design into the current storefront. The cards are ready for a backend subscription endpoint when that contract is implemented.
      </p>

      <div className="grid grid-cols-1 gap-8 text-left md:grid-cols-3">
        {SUBSCRIPTION_PLANS.map((plan) => (
          <section
            key={plan.id}
            className={`relative rounded-2xl bg-white p-8 transition-shadow ${
              plan.popular ? "border-2 border-primary bg-primary/5 shadow-xl" : "border border-border hover:shadow-xl"
            }`}
          >
            {plan.popular ? (
              <span className="absolute -top-3 left-1/2 -translate-x-1/2 rounded-full bg-primary px-4 py-1 text-xs font-bold uppercase tracking-widest text-white">
                Most Popular
              </span>
            ) : null}
            <h2 className="font-serif text-2xl font-bold">{plan.name}</h2>
            <p className="mb-6 mt-2 text-xl font-bold text-primary">
              ${plan.price} <span className="text-sm font-normal text-muted-foreground">/ month</span>
            </p>
            <ul className="mb-8 space-y-3 text-sm font-medium text-foreground/80">
              {plan.features.map((feature) => (
                <li key={feature} className="flex items-center gap-2">
                  <CheckIcon />
                  {feature}
                </li>
              ))}
            </ul>
            <button
              className={`w-full rounded py-3 text-sm font-bold tracking-widest transition-colors ${
                plan.popular
                  ? "bg-primary text-white hover:bg-accent"
                  : "border-2 border-primary text-primary hover:bg-primary hover:text-white"
              }`}
              type="button"
            >
              EXPLORE PLAN
            </button>
          </section>
        ))}
      </div>
    </div>
  );
}