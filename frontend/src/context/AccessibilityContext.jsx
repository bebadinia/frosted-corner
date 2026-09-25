import { createContext, useContext, useEffect, useState } from "react";

const AccessibilityContext = createContext(undefined);
const FONT_SIZE_KEY = "frosted-corner-font-size";

const FONT_SCALE = {
  normal: "100%",
  large: "112%",
  "extra-large": "125%",
};

export function AccessibilityProvider({ children }) {
  const [fontSize, setFontSize] = useState(() => localStorage.getItem(FONT_SIZE_KEY) || "normal");

  useEffect(() => {
    document.documentElement.style.fontSize = FONT_SCALE[fontSize] || FONT_SCALE.normal;
    localStorage.setItem(FONT_SIZE_KEY, fontSize);
  }, [fontSize]);

  return (
    <AccessibilityContext.Provider value={{ fontSize, setFontSize }}>
      {children}
    </AccessibilityContext.Provider>
  );
}

export function useAccessibility() {
  const contextValue = useContext(AccessibilityContext);

  if (!contextValue) {
    throw new Error("useAccessibility must be used within an AccessibilityProvider.");
  }

  return contextValue;
}
