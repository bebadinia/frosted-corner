import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export function LoginPage() {
  const { login } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");
    setIsSubmitting(true);

    try {
      const user = await login(email, password);
      const fallbackDestination = user.role === "MANAGER" || user.role === "OWNER"
        ? "/franchise"
        : "/profile";
      navigate(location.state?.from || fallbackDestination, { replace: true });
    } catch (requestError) {
      setError(requestError.message || "Unable to sign in. Please try again.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <main className="mx-auto flex w-full max-w-md flex-1 items-center px-6 py-16">
      <section aria-labelledby="login-heading" className="w-full rounded-xl border border-border bg-white p-8 shadow-sm">
        <h1 id="login-heading" className="font-serif text-3xl font-bold">Sign in</h1>
        <p className="mt-2 text-sm text-muted-foreground">Use a Frosted Corner demo account to access role-specific tools.</p>
        {location.state?.notice ? <p role="status" className="mt-3 text-sm font-medium text-green-700">{location.state.notice}</p> : null}
        <div className="mt-5 grid gap-2 text-xs">
          <button
            className="rounded border border-border bg-background px-3 py-2 text-left font-semibold hover:border-primary"
            onClick={() => {
              setEmail("customer@frostedcorner.demo");
              setPassword("demo-password");
            }}
            type="button"
          >
            Customer demo — customer@frostedcorner.demo
          </button>
          <button
            className="rounded border border-border bg-background px-3 py-2 text-left font-semibold hover:border-primary"
            onClick={() => {
              setEmail("manager@frostedcorner.demo");
              setPassword("demo-password");
            }}
            type="button"
          >
            Manager demo — manager@frostedcorner.demo
          </button>
          <span className="text-muted-foreground">Demo password: demo-password</span>
        </div>
        <form className="mt-6 space-y-4" onSubmit={handleSubmit}>
          <label className="block text-sm font-bold" htmlFor="email">
            Email
            <input
              autoComplete="email"
              className="mt-1 w-full rounded border border-border px-3 py-2"
              id="email"
              onChange={(event) => setEmail(event.target.value)}
              required
              type="email"
              value={email}
            />
          </label>
          <label className="block text-sm font-bold" htmlFor="password">
            Password
            <input
              autoComplete="current-password"
              className="mt-1 w-full rounded border border-border px-3 py-2"
              id="password"
              onChange={(event) => setPassword(event.target.value)}
              required
              type="password"
              value={password}
            />
          </label>
          {error ? <p role="alert" className="text-sm text-red-700">{error}</p> : null}
          <button
            className="w-full rounded bg-primary px-4 py-2 font-bold text-white hover:bg-accent disabled:opacity-60"
            disabled={isSubmitting}
            type="submit"
          >
            {isSubmitting ? "Signing in…" : "Sign in"}
          </button>
        </form>
        <p className="mt-5 text-sm text-muted-foreground">
          New to Frosted Corner? <Link className="font-bold text-primary hover:text-accent" to="/signup">Create an account</Link>
        </p>
      </section>
    </main>
  );
}