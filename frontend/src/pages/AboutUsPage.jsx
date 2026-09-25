export function AboutUsPage() {
  return (
    <div className="mx-auto w-full max-w-5xl px-6 py-16">
      <div className="rounded-2xl border border-border bg-white p-8 shadow-sm md:p-12">
        <div className="text-center">
          <div className="text-xs font-bold uppercase tracking-[0.2em] text-primary">About Us</div>
          <img
            src="/logo.png"
            alt="Frosted Corner"
            className="mx-auto mt-6 h-24 w-auto object-contain md:h-28"
          />
          <h1 className="mt-8 font-serif text-4xl font-bold">Sweet moments, made simpler.</h1>
          <p className="mx-auto mt-5 max-w-3xl text-base leading-8 text-muted-foreground">
            Frosted Corner is a fast-growing dessert brand founded in 2019, now operating 40+ franchise locations.
            With expansion planned into new geographies, the company is modernizing how customers and franchisees
            order desserts and supplies.
          </p>
        </div>

        <div className="mt-12 grid gap-6 md:grid-cols-3">
          <section className="rounded-xl border border-border bg-background p-6">
            <h2 className="font-serif text-xl font-bold">Our approach</h2>
            <p className="mt-3 text-sm leading-7 text-muted-foreground">
              We focus on making dessert ordering easy, convenient, and consistent across every Frosted Corner location.
            </p>
          </section>

          <section className="rounded-xl border border-border bg-background p-6">
            <h2 className="font-serif text-xl font-bold">For customers</h2>
            <p className="mt-3 text-sm leading-7 text-muted-foreground">
              Customers can explore products, discover seasonal options, place orders, and choose the location that works best for them.
            </p>
          </section>

          <section className="rounded-xl border border-border bg-background p-6">
            <h2 className="font-serif text-xl font-bold">For franchise teams</h2>
            <p className="mt-3 text-sm leading-7 text-muted-foreground">
              Franchise teams get a simpler way to understand inventory, monitor demand, and support day-to-day operations.
            </p>
          </section>
        </div>

        <div className="mt-10 rounded-xl bg-secondary/50 p-6 text-center">
          <h2 className="font-serif text-2xl font-bold">Built for growth</h2>
          <p className="mx-auto mt-3 max-w-3xl text-sm leading-7 text-muted-foreground">
            As Frosted Corner expands, the goal is to provide a consistent digital experience that can scale with new locations,
            new customers, and new ways to order.
          </p>
        </div>
      </div>
    </div>
  );
}
