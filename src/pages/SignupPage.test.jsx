import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { SignupPage } from "./SignupPage";

const { register } = vi.hoisted(() => ({ register: vi.fn() }));

vi.mock("../api/client", () => ({ register }));

function renderSignupPage() {
  return render(
    <MemoryRouter initialEntries={["/signup"]}>
      <Routes>
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/login" element={<p>Login destination</p>} />
      </Routes>
    </MemoryRouter>,
  );
}

describe("SignupPage", () => {
  afterEach(() => {
    register.mockReset();
  });

  it("creates a customer account and returns the customer to sign in", async () => {
    register.mockResolvedValue({ id: "user-1", role: "CUSTOMER", customerId: "customer-1" });
    renderSignupPage();

    fireEvent.change(screen.getByLabelText("Name"), { target: { value: "New Customer" } });
    fireEvent.change(screen.getByLabelText("Email"), { target: { value: "new@example.com" } });
    fireEvent.change(screen.getByLabelText("Password", { exact: true }), {
      target: { value: "safe-password" },
    });
    fireEvent.change(screen.getByLabelText("Confirm password"), {
      target: { value: "safe-password" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Create account" }));

    await waitFor(() => expect(register).toHaveBeenCalledWith(
      "New Customer", "new@example.com", "safe-password",
    ));
    expect(await screen.findByText("Login destination")).toBeInTheDocument();
  });

  it("does not submit mismatched passwords", () => {
    renderSignupPage();

    fireEvent.change(screen.getByLabelText("Name"), { target: { value: "New Customer" } });
    fireEvent.change(screen.getByLabelText("Email"), { target: { value: "new@example.com" } });
    fireEvent.change(screen.getByLabelText("Password", { exact: true }), {
      target: { value: "safe-password" },
    });
    fireEvent.change(screen.getByLabelText("Confirm password"), {
      target: { value: "different-password" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Create account" }));

    expect(screen.getByRole("alert")).toHaveTextContent("Passwords do not match.");
    expect(register).not.toHaveBeenCalled();
  });
});