const apiBaseUrl = import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, "") || "";

function buildApiUrl(path) {
  return `${apiBaseUrl}${path}`;
}

async function readError(response, fallbackMessage) {
  try {
    const error = await response.json();
    return error.message || error.detail || fallbackMessage;
  } catch {
    return fallbackMessage;
  }
}

function sessionOptions(options = {}) {
  return { ...options, credentials: "include" };
}

export async function getHealth() {
  const response = await fetch(buildApiUrl("/api/health"), sessionOptions());

  if (!response.ok) {
    throw new Error(`Backend health check failed with status ${response.status}.`);
  }

  return response.json();
}

export async function getProducts() {
  const response = await fetch(buildApiUrl("/api/products"), sessionOptions());

  if (!response.ok) {
    throw new Error(`Product request failed with status ${response.status}.`);
  }

  return response.json();
}

export async function getAnalyticsSummary({ storeId } = {}) {
  const query = storeId === undefined
    ? ""
    : `?storeId=${encodeURIComponent(storeId)}`;
  const response = await fetch(buildApiUrl(`/api/analytics/summary${query}`), sessionOptions());

  if (!response.ok) {
    throw new Error(`Analytics request failed with status ${response.status}.`);
  }

  return response.json();
}

export async function createOrder(order) {
  const response = await fetch(buildApiUrl("/api/orders"), sessionOptions({
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(order),
  }));

  if (!response.ok) {
    throw new Error(await readError(response,
      "Checkout failed. Please review your cart and try again."));
  }

  return response.json();
}

export async function getStores() {
  const response = await fetch(buildApiUrl("/api/locations"));

  if (!response.ok) {
    throw new Error(`Store list request failed with status ${response.status}.`);
  }

  return response.json();
}

export async function getNearestStore(demoAddressOrZip) {
  const query = `?demoAddressOrZip=${encodeURIComponent(demoAddressOrZip)}`;
  const response = await fetch(buildApiUrl(`/api/locations/nearest${query}`));

  if (!response.ok) {
    throw new Error(await readError(response,
      "Unable to rank nearby stores. Please choose an approved demo address or ZIP."));
  }

  return response.json();
}

export async function chatWithAssistant(request) {
  const response = await fetch(buildApiUrl("/api/assistant/chat"), sessionOptions({
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(request),
  }));

  if (!response.ok) {
    throw new Error(await readError(response, "Assistant request failed. Please try again."));
  }

  return response.json();
}

export function getProductImageUrl(imageFileName) {
  if (!imageFileName) {
    return "/images/products/placeholder.svg";
  }

  return `/images/products/${imageFileName}`;
}

export async function login(email, password) {
  const response = await fetch(buildApiUrl("/api/auth/login"), sessionOptions({
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password }),
  }));

  if (!response.ok) {
    throw new Error(await readError(response, "Unable to sign in. Please try again."));
  }

  return response.json();
}

export async function register(name, email, password) {
  const response = await fetch(buildApiUrl("/api/auth/register"), sessionOptions({
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name, email, password }),
  }));

  if (!response.ok) {
    throw new Error(await readError(response, "Unable to create your account. Please try again."));
  }

  return response.json();
}

export async function getCurrentUser() {
  const response = await fetch(buildApiUrl("/api/auth/me"), sessionOptions());

  if (response.status === 401 || response.status === 403) {
    return null;
  }
  if (!response.ok) {
    throw new Error(await readError(response, "Unable to determine the signed-in user."));
  }

  return response.json();
}

export async function logout() {
  const response = await fetch(buildApiUrl("/api/auth/logout"), sessionOptions({
    method: "POST",
  }));

  if (!response.ok) {
    throw new Error(await readError(response, "Unable to sign out. Please try again."));
  }
}