import { useEffect, useState } from "react";
import { getHealth } from "../api/client";

export function BackendStatus() {
  const [status, setStatus] = useState("Checking connection...");
  const [error, setError] = useState("");

  useEffect(() => {
    let isCurrent = true;

    getHealth()
      .then((health) => {
        if (isCurrent) {
          setStatus(health.status);
        }
      })
      .catch((requestError) => {
        if (isCurrent) {
          setError(requestError.message);
        }
      });

    return () => {
      isCurrent = false;
    };
  }, []);

  return error ? (
    <p className="text-sm font-medium text-red-700" role="alert">
      Backend status: {error}
    </p>
  ) : (
    <p className="text-sm font-medium text-primary">Backend status: {status}</p>
  );
}