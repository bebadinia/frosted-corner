import { Link } from "react-router-dom";
import { BackendStatus } from "../components/BackendStatus";
import { StoreFinderPanel } from "../components/StoreFinderPanel";

export function HomePage() {
  return (
    <>
      <div className="bg-primary px-4 py-3 text-center text-sm font-bold tracking-wide text-white">
        If your order is over $25, you get free shipping.
      </div>
      <section className="relative flex min-h-[600px] items-center justify-center overflow-hidden lg:min-h-[700px]">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_top,#f9d9c0_0%,#f3b485_30%,#6d2b11_100%)]" />
        <div className="absolute inset-0 bg-[linear-gradient(135deg,rgba(45,24,15,0.18),rgba(45,24,15,0.6))]" />
        <div className="relative z-10 w-11/12 max-w-3xl bg-primary/90 p-3 shadow-2xl backdrop-blur-sm">
          <div className="flex flex-col items-center border border-white/40 p-10 text-center md:p-16">
            <h1 className="mb-6 font-sans text-3xl font-light tracking-wide text-white md:text-5xl">
              WARM DESSERTS, MODERN ORDERING
            </h1>
            <p className="mb-8 max-w-xl text-base font-medium leading-relaxed text-white/95 md:text-lg">
              Frosted Corner pairs a bakery-inspired storefront with guided ordering, live catalog data,
              and franchise-friendly operational views.
            </p>
            <Link
              className="border-2 border-white px-10 py-3.5 text-sm font-bold tracking-widest text-white transition-colors hover:bg-white hover:text-primary"
              to="/order"
            >
              ORDER NOW
            </Link>
          </div>
        </div>
      </section>

      <section className="mx-auto grid w-full max-w-7xl gap-6 px-6 py-16 lg:grid-cols-2">
        <div className="rounded-2xl border border-border bg-white p-8 shadow-sm">
          <div className="mb-3 text-xs font-bold uppercase tracking-[0.2em] text-primary">System status</div>
          <h2 className="font-serif text-2xl font-bold">Backend connectivity</h2>
          <p className="mt-4 text-sm leading-7 text-muted-foreground">
            This storefront already checks the backend health endpoint and consumes the live product catalog.
          </p>
          <div className="mt-6 rounded-xl bg-background p-4">
            <BackendStatus />
          </div>
          <div className="mt-6 text-sm text-muted-foreground">
            Product images belong in /frontend/public/images/products so the backend imageFileName field can map directly to display URLs.
          </div>
        </div>

        <StoreFinderPanel />
      </section>
    </>
  );
}