import { Type } from "lucide-react";
import { useAccessibility } from "../context/AccessibilityContext";

const SIZES = [
  { value: "normal", label: "A" },
  { value: "large", label: "A+" },
  { value: "extra-large", label: "A++" },
];

export function AccessibilityBar() {
  const { fontSize, setFontSize } = useAccessibility();

  return (
    <div className="flex items-center gap-3 text-xs font-semibold" role="group" aria-label="Accessibility preferences">
      <div className="flex items-center gap-1">
        <Type className="h-3.5 w-3.5 text-muted-foreground" />
        {SIZES.map((size) => (
          <button
            key={size.value}
            aria-label={`Set text size ${size.label}`}
            aria-pressed={fontSize === size.value}
            className={`rounded px-2 py-1 transition-colors ${
              fontSize === size.value ? "bg-primary text-white" : "hover:bg-secondary"
            }`}
            onClick={() => setFontSize(size.value)}
            type="button"
          >
            {size.label}
          </button>
        ))}
      </div>
    </div>
  );
}
