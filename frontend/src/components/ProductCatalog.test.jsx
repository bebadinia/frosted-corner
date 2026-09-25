import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { ProductCatalog } from "./ProductCatalog";

const { addItem, showToast } = vi.hoisted(() => ({
  addItem: vi.fn(),
  showToast: vi.fn(),
}));

vi.mock("../context/CartContext", () => ({
  useCart: () => ({ addItem }),
}));

vi.mock("../context/ToastContext", () => ({
  useToast: () => ({ showToast }),
}));

vi.mock("../api/client", () => ({
  getProductImageUrl: (imageFileName) => "/images/products/" + imageFileName,
}));

const products = [
  {
    id: "P001",
    name: "Chocolate Cake",
    description: "Chocolate cake",
    price: 32.99,
    category: "Cakes",
    imageFileName: "cake.jpg",
  },
  {
    id: "P002",
    name: "Vanilla Cupcake",
    description: "Vanilla cupcake",
    price: 3.99,
    category: "Cupcakes",
    imageFileName: "cupcake.jpg",
  },
];

describe("ProductCatalog inventory state", () => {
  it("disables sold-out items and labels low inventory", () => {
    render(
      <ProductCatalog
        availabilityByProductId={{
          P001: { productId: "P001", quantity: 0, lowStockThreshold: 10 },
          P002: { productId: "P002", quantity: 3, lowStockThreshold: 10 },
        }}
        availabilityLoading={false}
        error=""
        loading={false}
        products={products}
      />,
    );

    expect(screen.getByRole("button", { name: "Chocolate Cake sold out" })).toBeDisabled();
    expect(screen.getByText("Sold out")).toBeInTheDocument();
    expect(screen.getAllByText("Low inventory — 3 left").length).toBeGreaterThan(0);
  });
});
