import { useEffect, useRef, useState } from "react";
import { MessageSquare, Mic, Send } from "lucide-react";
import { chatWithAssistant } from "../api/client";
import { useCart } from "../context/CartContext";
import { useToast } from "../context/ToastContext";

const customerId = import.meta.env.VITE_CUSTOMER_ID || "c1";
const storeId = import.meta.env.VITE_STORE_ID || "store1";

function getSpeechRecognition() {
  return window.SpeechRecognition || window.webkitSpeechRecognition;
}

function normalizeAssistantMessage(response) {
  return response.message
    || "I can help with menu recommendations, events, flavors, and adding validated desserts to your cart.";
}

function RecommendationList({ recommendations }) {
  if (!recommendations?.length) {
    return null;
  }

  return (
    <div className="mt-3 grid gap-2">
      {recommendations.map((recommendation) => (
        <div key={recommendation.productId} className="rounded-xl border border-border bg-background/60 p-3">
          <div className="flex items-center justify-between gap-3">
            <div>
              <p className="font-semibold text-foreground">{recommendation.name}</p>
              <p className="text-xs text-muted-foreground">{recommendation.category}</p>
            </div>
            <div className="text-right">
              <p className="font-semibold text-foreground">${Number(recommendation.price).toFixed(2)}</p>
              <p className="text-xs text-muted-foreground">Try {recommendation.suggestedQuantity}</p>
            </div>
          </div>
          <p className="mt-2 text-xs text-muted-foreground">{recommendation.reason}</p>
        </div>
      ))}
    </div>
  );
}

export function ConversationalAssistant() {
  const { addItem, openCart } = useCart();
  const { showToast } = useToast();
  const [messages, setMessages] = useState([
    {
      id: "welcome",
      role: "assistant",
      text: "Tell me an event, a flavor, or a party size and I will recommend active desserts from the backend menu.",
      recommendations: [],
    },
  ]);
  const [input, setInput] = useState("");
  const [isListening, setIsListening] = useState(false);
  const [isSending, setIsSending] = useState(false);
  const [recommendedProductIds, setRecommendedProductIds] = useState([]);
  const recognitionRef = useRef(null);
  const scrollRef = useRef(null);
  const voiceSupported = Boolean(getSpeechRecognition());

  useEffect(() => {
    if (scrollRef.current && typeof scrollRef.current.scrollTo === "function") {
      scrollRef.current.scrollTo({ top: scrollRef.current.scrollHeight, behavior: "smooth" });
    }
  }, [messages]);

  const handleSend = async (rawText) => {
    const text = rawText.trim();

    if (!text || isSending) {
      return;
    }
    setMessages((currentMessages) => [
      ...currentMessages,
      { id: `user-${Date.now()}`, role: "user", text },
    ]);
    setInput("");

    setIsSending(true);

    try {
      const response = await chatWithAssistant({
        customerId,
        storeId,
        message: text,
        recommendedProductIds,
      });

      if (response.cartAction?.items?.length) {
        response.cartAction.items.forEach((item) => {
          addItem({
            id: item.productId,
            name: item.name,
            price: Number(item.price),
            category: item.category,
            imageFileName: item.imageFileName,
          }, item.quantity);
        });

        showToast(`Added ${response.cartAction.items.map((item) => item.name).join(", ")} to your cart.`);

        if (response.cartAction.openCart) {
          openCart();
        }
      }

      const nextRecommendationIds = response.recommendations?.map((recommendation) => recommendation.productId) || [];
      if (nextRecommendationIds.length > 0) {
        setRecommendedProductIds(nextRecommendationIds);
      }

      setMessages((currentMessages) => [
        ...currentMessages,
        {
          id: `assistant-${Date.now()}`,
          role: "assistant",
          text: normalizeAssistantMessage(response),
          recommendations: response.recommendations || [],
        },
      ]);
    } catch (requestError) {
      const errorMessage = requestError instanceof Error
        ? requestError.message
        : "Assistant request failed. Please try again.";
      showToast(errorMessage, "info");
      setMessages((currentMessages) => [
        ...currentMessages,
        {
          id: `assistant-${Date.now()}`,
          role: "assistant",
          text: errorMessage,
          recommendations: [],
        },
      ]);
    } finally {
      setIsSending(false);
    }
  };

  const handleMicClick = () => {
    const RecognitionConstructor = getSpeechRecognition();

    if (!RecognitionConstructor) {
      showToast("Voice ordering is not supported in this browser.", "info");
      return;
    }

    if (isListening) {
      recognitionRef.current?.stop();
      return;
    }

    const recognition = new RecognitionConstructor();
    recognition.lang = "en-US";
    recognition.interimResults = false;
    recognition.maxAlternatives = 1;
    recognition.onresult = (event) => {
      const transcript = event.results[0][0].transcript;
      handleSend(transcript);
    };
    recognition.onerror = () => setIsListening(false);
    recognition.onend = () => setIsListening(false);
    recognitionRef.current = recognition;
    setIsListening(true);
    recognition.start();
  };

  return (
    <div className="flex h-[440px] w-full min-w-0 flex-col overflow-hidden rounded-2xl border border-border bg-white shadow-sm lg:sticky lg:top-24 lg:h-[520px] lg:w-[24rem] lg:shrink-0 xl:w-[26rem]">
      <div className="flex items-center justify-between bg-primary p-4 text-white">
        <div>
          <h3 className="font-serif text-lg font-bold">Guided Ordering</h3>
          <p className="text-xs text-white/80">Voice and chat UI over the current catalog</p>
        </div>
        <MessageSquare className="h-5 w-5 opacity-80" />
      </div>

      <div ref={scrollRef} className="flex flex-1 flex-col gap-4 overflow-y-auto bg-background/30 p-4">
        {messages.map((message) => (
          <div
            key={message.id}
            className={`flex items-start gap-3 ${message.role === "user" ? "flex-row-reverse" : ""}`}
          >
            <div
              className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-full ${
                message.role === "user" ? "bg-secondary text-primary" : "bg-primary/20 text-primary"
              }`}
            >
              {message.role === "user" ? "You" : "FC"}
            </div>
            <div
              className={`max-w-[85%] rounded-2xl p-3 text-sm shadow-sm ${
                message.role === "user"
                  ? "rounded-tr-none bg-primary text-white"
                  : "rounded-tl-none border border-border bg-white"
              }`}
            >
              {message.text}
              <RecommendationList recommendations={message.recommendations} />
              {message.id === "welcome" ? (
                <button
                  className="mt-3 block w-full rounded bg-primary/10 py-2 text-center text-xs font-bold uppercase tracking-wider text-primary"
                  onClick={openCart}
                  type="button"
                >
                  Review Cart
                </button>
              ) : null}
            </div>
          </div>
        ))}
      </div>

      <form
        className="flex min-w-0 items-center gap-2 border-t border-border bg-white p-4"
        onSubmit={(event) => {
          event.preventDefault();
          handleSend(input);
        }}
      >
        <button
          aria-label="Order by voice"
          aria-pressed={isListening}
          className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-full transition-colors ${
            isListening ? "animate-pulse bg-primary text-white" : "bg-secondary text-primary hover:bg-primary/20"
          }`}
          onClick={handleMicClick}
          title={voiceSupported ? "Order by voice" : "Voice ordering is not supported in this browser"}
          type="button"
        >
          <Mic className="h-5 w-5" />
        </button>
        <input
          className="min-w-0 flex-1 rounded-full border border-border bg-background px-4 py-2 text-sm outline-none transition-colors focus:border-primary"
          onChange={(event) => setInput(event.target.value)}
          placeholder="Type or speak your order..."
          disabled={isSending}
          type="text"
          value={input}
        />
        <button
          aria-label="Send message"
          disabled={isSending}
          className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-primary text-white transition-colors hover:bg-accent"
          type="submit"
        >
          <Send className="h-4 w-4" />
        </button>
      </form>
    </div>
  );
}