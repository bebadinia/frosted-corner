import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { register } from "../api/client";

export function SignupPage() {
  const navigate = useNavigate();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [passwordConfirmation, setPasswordConfirmation] = useState("");
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");

    if (password !== passwordConfirmation) {
      setError("Passwords do not match.");
      return;
    }

    setIsSubmitting(true);
    try {
      await register(name, email, password);
      navigate("/login", {
        replace: true,
        state: { notice: "Account created. Sign in with your new email and password." },
      });
    } catch (requestError) {
      setError(requestError.message || "Unable to create your account. Please try again.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <main className="mx-auto flex w-full max-w-md flex-1 items-center px-6 py-16">
      <section aria-labelledby="signup-heading" className="w-full rounded-xl border border-border bg-white p-8 shadow-sm">
        <h1 id="signup-heading" className="font-serif text-3xl font-bold">Create an account</h1>
        <p className="mt-2 text-sm text-muted-foreground">Create a customer account to save your Frosted Corner profile.</p>
        <form className="mt-6 space-y-4" onSubmit={handleSubmit}>
          <label className="block text-sm font-bold" htmlFor="name">
            Name
            <input
              autoComplete="name"
              className="mt-1 w-full rounded border border-border px-3 py-2"
              id="name"
              maxLength="100"
              onChange={(event) => setName(event.target.value)}
              required
              value={name}
            />
          </label>
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
              autoComplete="new-password"
              className="mt-1 w-full rounded border border-border px-3 py-2"
              id="password"
              minLength="8"
              onChange={(event) => setPassword(event.target.value)}
              required
              type="password"
              value={password}
            />
          </label>
          <label className="block text-sm font-bold" htmlFor="password-confirmation">
            Confirm password
            <input
              autoComplete="new-password"
              className="mt-1 w-full rounded border border-border px-3 py-2"
              id="password-confirmation"
              minLength="8"
              onChange={(event) => setPasswordConfirmation(event.target.value)}
              required
              type="password"
              value={passwordConfirmation}
            />
          </label>
          {error ? <p role="alert" className="text-sm text-red-700">{error}</p> : null}
          <button
            className="w-full rounded bg-primary px-4 py-2 font-bold text-white hover:bg-accent disabled:opacity-60"
            disabled={isSubmitting}
            type="submit"
          >
            {isSubmitting ? "Creating account…" : "Create account"}
          </button>
        </form>
        <p className="mt-5 text-sm text-muted-foreground">
          Already have an account? <Link className="font-bold text-primary hover:text-accent" to="/login">Sign in</Link>
        </p>
      </section>
    </main>
  );
}