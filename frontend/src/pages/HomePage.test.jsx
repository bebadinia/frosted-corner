import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { describe, expect, it, vi } from "vitest";
import { HomePage } from "./HomePage";

vi.mock("../components/BackendStatus", () => ({
  BackendStatus: () => <div>backend-status</div>,
}));

vi.mock("../components/StoreFinderPanel", () => ({
  StoreFinderPanel: () => <div>store-finder</div>,
}));

describe("HomePage", () => {
  it("shows the free shipping promotion banner", () => {
    render(
      <MemoryRouter>
        <HomePage />
      </MemoryRouter>,
    );

    expect(
      screen.getByText("If your order is over $25, you get free shipping!"),
    ).toBeInTheDocument();
    expect(screen.queryByText("MVP flow")).not.toBeInTheDocument();
    expect(screen.queryByText("Customer-first storefront")).not.toBeInTheDocument();
  });
});
