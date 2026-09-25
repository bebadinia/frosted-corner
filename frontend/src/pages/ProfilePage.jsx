import { useEffect, useState } from "react";
import { getCustomerProfile } from "../api/client";
import { useAuth } from "../context/AuthContext";

function ProfileField({ label, value }) {
  return (
    <div className="rounded-xl border border-border bg-background p-4">
      <div className="text-xs font-bold uppercase tracking-wider text-muted-foreground">{label}</div>
      <div className="mt-2 font-medium text-foreground">{value || "Not provided"}</div>
    </div>
  );
}

export function ProfilePage() {
  const { currentUser } = useAuth();
  const [profile, setProfile] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!currentUser?.customerId) {
      return;
    }

    let isCurrent = true;
    setError("");

    getCustomerProfile(currentUser.customerId)
      .then((result) => {
        if (isCurrent) {
          setProfile(result);
        }
      })
      .catch((requestError) => {
        if (isCurrent) {
          setError(requestError.message || "Unable to load your profile.");
        }
      });

    return () => {
      isCurrent = false;
    };
  }, [currentUser?.customerId]);

  return (
    <div className="mx-auto w-full max-w-5xl px-6 py-12">
      <div className="mb-8">
        <div className="text-xs font-bold uppercase tracking-[0.2em] text-primary">Customer Account</div>
        <h1 className="mt-2 font-serif text-4xl font-bold">My Profile</h1>
        <p className="mt-2 text-sm text-muted-foreground">
          Your Frosted Corner customer information and saved preferences.
        </p>
      </div>

      {error ? (
        <p className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700" role="alert">
          {error}
        </p>
      ) : null}

      {!profile && !error ? (
        <p className="rounded-xl border border-border bg-white p-6 text-sm text-muted-foreground">Loading profile…</p>
      ) : null}

      {profile ? (
        <div className="space-y-8">
          <section className="rounded-2xl border border-border bg-white p-6 shadow-sm">
            <h2 className="font-serif text-2xl font-bold">{profile.name}</h2>
            <div className="mt-6 grid gap-4 md:grid-cols-2">
              <ProfileField label="Email" value={profile.email} />
              <ProfileField label="Phone" value={profile.phone} />
              <ProfileField label="Street" value={profile.street} />
              <ProfileField label="City / State / ZIP" value={[profile.city, profile.state, profile.zipCode].filter(Boolean).join(", ")} />
            </div>
          </section>

          <section className="grid gap-6 md:grid-cols-2">
            <div className="rounded-2xl border border-border bg-white p-6 shadow-sm">
              <h2 className="font-serif text-xl font-bold">Preferences</h2>
              <div className="mt-4 flex flex-wrap gap-2">
                {(profile.preferences || []).length > 0
                  ? profile.preferences.map((preference) => (
                    <span key={preference} className="rounded-full bg-secondary px-3 py-1 text-sm font-semibold text-primary">
                      {preference}
                    </span>
                  ))
                  : <span className="text-sm text-muted-foreground">No saved preferences yet.</span>}
              </div>
            </div>

            <div className="rounded-2xl border border-border bg-white p-6 shadow-sm">
              <h2 className="font-serif text-xl font-bold">Favorite Categories</h2>
              <div className="mt-4 flex flex-wrap gap-2">
                {(profile.favoriteCategories || []).length > 0
                  ? profile.favoriteCategories.map((category) => (
                    <span key={category} className="rounded-full bg-secondary px-3 py-1 text-sm font-semibold text-primary">
                      {category}
                    </span>
                  ))
                  : <span className="text-sm text-muted-foreground">No favorite categories yet.</span>}
              </div>
            </div>
          </section>
        </div>
      ) : null}
    </div>
  );
}
