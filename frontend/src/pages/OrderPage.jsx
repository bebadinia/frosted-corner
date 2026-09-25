import { useEffect, useState } from "react";
import { getProducts } from "../api/client";
import { ConversationalAssistant } from "../components/ConversationalAssistant";
import { ProductCatalog } from "../components/ProductCatalog";

function getProductErrorMessage() {
  return "We couldn't load the menu right now. Please try again in a moment.";
}

export function OrderPage() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let isCurrent = true;

    setLoading(true);
    setError("");

    getProducts()
      .then((catalog) => {
        if (!isCurrent) {
          return;
        }

        setProducts(catalog);
      })
      .catch((requestError) => {
        if (!isCurrent) {
          return;
        }

        console.error("Failed to load products", requestError);
        setError(getProductErrorMessage());
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
  return (
    <div className="mx-auto flex w-full max-w-7xl flex-col gap-12 px-6 py-12 lg:flex-row">
      <ProductCatalog error={error} loading={loading} products={products} />
      <ConversationalAssistant />
    </div>
  );
}