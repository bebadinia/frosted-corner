import { Link } from "react-router-dom";

export function NotFoundPage() {
  return (
    <div className="mx-auto flex w-full max-w-3xl flex-1 flex-col items-center justify-center px-6 py-20 text-center">
      <div className="mb-3 text-xs font-bold uppercase tracking-[0.2em] text-primary">404</div>
      <h1 className="font-serif text-4xl font-bold">Page not found</h1>
      <p className="mt-4 max-w-lg text-sm leading-7 text-muted-foreground">
        The route you requested is not part of the current storefront flow.
      </p>
      <Link className="mt-8 rounded bg-primary px-5 py-3 text-sm font-bold text-white transition-colors hover:bg-accent" to="/">
        Return home
      </Link>
    </div>
  );
}