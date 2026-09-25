import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { chatWithAssistant } from "../api/client";
import { ConversationalAssistant } from "./ConversationalAssistant";
import { CartProvider, useCart } from "../context/CartContext";
import { ToastProvider } from "../context/ToastContext";

vi.mock("../api/client", () => ({
  chatWithAssistant: vi.fn(),
}));

function CartProbe() {
  const { totalItems } = useCart();
  return <div data-testid="cart-total-items">{totalItems}</div>;
}

function renderAssistant() {
  return render(
    <ToastProvider>
      <CartProvider>
        <CartProbe />
        <ConversationalAssistant />
      </CartProvider>
    </ToastProvider>,
  );
}

describe("ConversationalAssistant", () => {
  beforeEach(() => {
    chatWithAssistant.mockReset();
    localStorage.clear();
    delete window.SpeechRecognition;
    delete window.webkitSpeechRecognition;
  });

  afterEach(() => {
    cleanup();
  });

  it("keeps typed chat working and adds backend-validated cart items", async () => {
    chatWithAssistant
      .mockResolvedValueOnce({
        message: "Here is a chocolate option from the current menu.",
        recommendations: [
          {
            productId: "P005",
            name: "Chocolate Fudge Cupcake",
            price: 4.49,
            category: "Cupcakes",
            imageFileName: "cupcake.jpg",
            suggestedQuantity: 1,
            reason: "Matches the chocolate preference.",
          },
        ],
      })
      .mockResolvedValueOnce({
        message: "I validated 2 of Chocolate Fudge Cupcake against current inventory and it is ready for your cart.",
        recommendations: [],
        cartAction: {
          openCart: true,
          items: [
            {
              productId: "P005",
              name: "Chocolate Fudge Cupcake",
              price: 4.49,
              category: "Cupcakes",
              imageFileName: "cupcake.jpg",
              quantity: 2,
            },
          ],
        },
      });

    renderAssistant();

    fireEvent.change(screen.getByPlaceholderText("Type or speak your order..."), {
      target: { value: "Show me something chocolate." },
    });
    fireEvent.click(screen.getByRole("button", { name: "Send message" }));

    await screen.findByText("Here is a chocolate option from the current menu.");

    fireEvent.change(screen.getByPlaceholderText("Type or speak your order..."), {
      target: { value: "Add two of those to my cart." },
    });
    fireEvent.click(screen.getByRole("button", { name: "Send message" }));

    await waitFor(() => {
      expect(chatWithAssistant).toHaveBeenLastCalledWith({
        customerId: "c1",
        storeId: "store1",
        message: "Add two of those to my cart.",
        recommendedProductIds: ["P005"],
      });
    });
    expect(screen.getByTestId("cart-total-items")).toHaveTextContent("2");
  });

  it("uses the same backend endpoint for voice input", async () => {
    class FakeSpeechRecognition {
      start() {
        this.onresult({ results: [[{ transcript: "Recommend something for Christmas." }]] });
        this.onend();
      }

      stop() {
        this.onend();
      }
    }

    window.SpeechRecognition = FakeSpeechRecognition;
    chatWithAssistant.mockResolvedValue({
      message: "Here are Christmas-friendly options from the current menu.",
      recommendations: [],
    });

    renderAssistant();
    fireEvent.click(screen.getByRole("button", { name: "Order by voice" }));

    await waitFor(() => {
      expect(chatWithAssistant).toHaveBeenCalledWith({
        customerId: "c1",
        storeId: "store1",
        message: "Recommend something for Christmas.",
        recommendedProductIds: [],
      });
    });
  });
});