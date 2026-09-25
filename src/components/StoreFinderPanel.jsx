import { useState } from "react";
import { MapPin } from "lucide-react";
import { getNearestStore } from "../api/client";

const APPROVED_DEMO_LOCATIONS = [
  { value: "97205", label: "Portland, OR 97205" },
  { value: "98101", label: "Seattle, WA 98101" },
  { value: "10001", label: "New York, NY 10001" },
  { value: "60601", label: "Chicago, IL 60601" },
  { value: "33130", label: "Miami, FL 33130" },
];

export function StoreFinderPanel() {
  const [selectedLocation, setSelectedLocation] = useState(APPROVED_DEMO_LOCATIONS[0].value);
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError("");

    try {
      const nextResult = await getNearestStore(selectedLocation);
      setResult(nextResult);
    } catch (requestError) {
      setResult(null);
      setError(requestError.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="rounded-2xl border border-border bg-white p-8 shadow-sm" aria-labelledby="store-finder-heading">
      <div className="mb-3 text-xs font-bold uppercase tracking-[0.2em] text-primary">Closest store demo</div>
      <div className="flex items-start gap-3">
        <div className="mt-1 flex h-10 w-10 items-center justify-center rounded-full bg-secondary text-primary">
          <MapPin className="h-5 w-5" />
        </div>
        <div>
          <h2 id="store-finder-heading" className="font-serif text-2xl font-bold">Find the nearest Frosted Corner</h2>
          <p className="mt-2 text-sm leading-7 text-muted-foreground">
            Choose one approved demo customer ZIP code and compare the nearest stores using backend-calculated Haversine distance.
          </p>
        </div>
      </div>

      <form className="mt-6 grid gap-4 md:grid-cols-[1fr_auto]" onSubmit={handleSubmit}>
        <label className="text-sm font-semibold text-foreground" htmlFor="store-finder-location">
          Demo customer location
          <select
            className="mt-2 w-full rounded-xl border border-border bg-background px-4 py-3 text-sm"
            id="store-finder-location"
            onChange={(event) => setSelectedLocation(event.target.value)}
            value={selectedLocation}
          >
            {APPROVED_DEMO_LOCATIONS.map((location) => (
              <option key={location.value} value={location.value}>{location.label}</option>
            ))}
          </select>
        </label>
        <button
          className="self-end rounded-xl bg-primary px-5 py-3 text-sm font-bold text-white transition-colors hover:bg-accent disabled:cursor-not-allowed disabled:opacity-60"
          disabled={loading}
          type="submit"
        >
          {loading ? "Finding…" : "Find nearest store"}
        </button>
      </form>

      {error ? (
        <p className="mt-5 rounded-xl border border-red-300 bg-red-50 px-4 py-3 text-sm font-medium text-red-700" role="alert">
          {error}
        </p>
      ) : null}

      {result ? (
        <div className="mt-6 space-y-5">
          <div className="rounded-2xl bg-background p-5">
            <div className="text-xs font-bold uppercase tracking-[0.18em] text-primary">Nearest store</div>
            <div className="mt-3 flex flex-wrap items-end justify-between gap-3">
              <div>
                <h3 className="font-serif text-2xl font-bold">{result.nearestStore.storeName}</h3>
                <p className="mt-1 text-sm text-muted-foreground">
                  {result.nearestStore.street}, {result.nearestStore.city}, {result.nearestStore.state} {result.nearestStore.zipCode}
                </p>
                <p className="mt-1 text-sm text-muted-foreground">Manager: {result.nearestStore.managerName}</p>
              </div>
              <div className="rounded-full bg-white px-4 py-2 text-sm font-bold text-primary">
                {result.nearestStore.distanceMiles.toFixed(2)} miles
              </div>
            </div>
          </div>

          <div>
            <div className="mb-3 text-xs font-bold uppercase tracking-[0.18em] text-primary">Closest-to-farthest preview</div>
            <ul className="space-y-3">
              {result.stores.slice(0, 3).map((store) => (
                <li key={store.id} className="flex items-center justify-between gap-4 rounded-xl border border-border px-4 py-3 text-sm">
                  <div>
                    <div className="font-bold">{store.storeName}</div>
                    <div className="text-muted-foreground">{store.city}, {store.state}</div>
                  </div>
                  <div className="shrink-0 font-semibold text-primary">{store.distanceMiles.toFixed(2)} mi</div>
                </li>
              ))}
            </ul>
          </div>
        </div>
      ) : null}
    </section>
  );
}