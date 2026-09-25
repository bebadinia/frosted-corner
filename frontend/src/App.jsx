import { ArrowRight, BarChart3, Search, ShoppingBag, User } from "lucide-react";
import {
  Link,
  Outlet,
  RouterProvider,
  createBrowserRouter,
  useLocation,
} from "react-router-dom";
import { AccessibilityBar } from "./components/AccessibilityBar";
import { AboutUsPage } from "./pages/AboutUsPage";
import { CartDrawer } from "./components/CartDrawer";
import { AccessibilityProvider } from "./context/AccessibilityContext";
import { AuthProvider, useAuth } from "./context/AuthContext";
import { CartProvider, useCart } from "./context/CartContext";
import { ToastProvider } from "./context/ToastContext";
import { FranchisePage } from "./pages/FranchisePage";
import { HomePage } from "./pages/HomePage";
import { LoginPage } from "./pages/LoginPage";
import { NotFoundPage } from "./pages/NotFoundPage";
import { OrderPage } from "./pages/OrderPage";
import { SignupPage } from "./pages/SignupPage";
import { SubscriptionsPage } from "./pages/SubscriptionsPage";

function Layout() {
  const location = useLocation();
  const { totalItems, openCart } = useCart();
  const { currentUser, isLoading, logout } = useAuth();
  const isFranchiseRoute = location.pathname.startsWith("/franchise");
  const canViewAnalytics = currentUser?.role === "MANAGER" || currentUser?.role === "OWNER";

  if (isFranchiseRoute) {
    return (
      <div className="min-h-screen bg-background md:flex">
        <aside className="w-full border-b border-border bg-white md:w-64 md:border-b-0 md:border-r">
          <div className="flex items-center gap-3 border-b border-border px-6 py-6">
            <div className="flex h-8 w-8 items-center justify-center rounded bg-primary text-white">
              <BarChart3 className="h-4 w-4" />
            </div>
            <span className="font-serif text-xl font-bold text-primary">Frosted Portal</span>
          </div>
          <nav className="flex flex-col gap-2 p-4 text-sm font-medium">
            {canViewAnalytics ? (
              <Link className="rounded-lg bg-primary/10 px-4 py-3 text-primary" to="/franchise">
                Insights
              </Link>
            ) : null}
            <a className="rounded-lg px-4 py-3 text-muted-foreground transition-colors hover:bg-primary/5 hover:text-primary" href="#inventory-supplies">
              Inventory & Supplies
            </a>
          </nav>
          <div className="border-t border-border p-4">
            <Link
              className="inline-flex items-center gap-2 text-sm font-medium text-muted-foreground transition-colors hover:text-primary"
              to="/"
            >
              <ArrowRight className="h-4 w-4" />
              Back to Storefront
            </Link>
          </div>
        </aside>
        <main className="flex-1 overflow-y-auto">
          <Outlet />
        </main>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background text-foreground">
      <div className="hidden border-b border-border bg-white md:block">
        <div className="mx-auto flex h-10 max-w-7xl items-center justify-between px-6 text-xs font-semibold tracking-wide">
          <AccessibilityBar />
          <div className="flex items-center">
            {canViewAnalytics ? (
              <Link className="flex h-full items-center border-r border-border px-4 transition-colors hover:text-primary" to="/franchise">
                Franchise Portal
              </Link>
            ) : null}
            {isLoading ? null : currentUser ? (
              <button
                className="flex h-full items-center gap-1.5 pl-4 text-muted-foreground hover:text-primary"
                onClick={() => logout().catch(() => {})}
                type="button"
              >
                <User className="h-3.5 w-3.5" />
                Sign out ({currentUser.role})
              </button>
            ) : (
              <Link className="flex h-full items-center gap-1.5 pl-4 text-muted-foreground hover:text-primary" to="/login">
                <User className="h-3.5 w-3.5" />
                Sign in
              </Link>
            )}
          </div>
        </div>
      </div>

      <header className="sticky top-0 z-50 border-b border-border bg-white/95 shadow-sm backdrop-blur-sm">
        <div className="mx-auto flex h-20 max-w-7xl items-center justify-between px-6">
          <Link className="group flex items-center gap-3" to="/">
            <img
              src="/logo.png"
              alt="Frosted Corner"
              className="h-12 w-auto object-contain"
            />
          </Link>
          <nav className="hidden items-center gap-8 text-[13px] font-bold tracking-wider lg:flex">
            <Link className="transition-colors hover:text-primary" to="/order">
              ORDER
            </Link>
            <Link className="transition-colors hover:text-primary" to="/subscriptions">
              SUBSCRIPTIONS
            </Link>
            <Link className="transition-colors hover:text-primary" to="/about">
              ABOUT US
            </Link>
          </nav>

          <div className="flex items-center gap-4">
            <Link aria-label="Browse catalog" className="text-foreground lg:hidden" to="/order">
              <Search className="h-6 w-6" />
            </Link>
            <button aria-label="Open cart" className="relative text-foreground" onClick={openCart} type="button">
              <ShoppingBag className="h-6 w-6" />
              {totalItems > 0 ? (
                <span className="absolute -right-1 -top-1 flex h-4 w-4 items-center justify-center rounded-full bg-primary text-[10px] text-white">
                  {totalItems}
                </span>
              ) : null}
            </button>
          </div>
        </div>
        <nav className="flex items-center justify-center gap-6 border-t border-border px-4 py-3 text-[11px] font-bold tracking-wider lg:hidden sm:gap-8 sm:text-[13px]">
          <Link className="transition-colors hover:text-primary" to="/order">
            ORDER
          </Link>
          <Link className="transition-colors hover:text-primary" to="/subscriptions">
            SUBSCRIPTIONS
          </Link>
          <Link className="transition-colors hover:text-primary" to="/about">
            ABOUT US
          </Link>
        </nav>
      </header>

      <main className="flex min-h-[calc(100vh-10rem)] flex-col">
        <Outlet />
      </main>

      <footer className="border-t border-border bg-white py-12">
        <div className="mx-auto max-w-7xl px-6 text-center text-sm font-medium text-muted-foreground">
          Frosted Corner - Crafted desserts, event menus, and simplified ordering for every celebration.
        </div>
      </footer>

      <CartDrawer />
    </div>
  );
}

const router = createBrowserRouter([
  {
    path: "/",
    element: <Layout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: "login", element: <LoginPage /> },
      { path: "signup", element: <SignupPage /> },
      { path: "order", element: <OrderPage /> },
      { path: "subscriptions", element: <SubscriptionsPage /> },
      { path: "about", element: <AboutUsPage /> },
      { path: "franchise", element: <FranchisePage /> },
      { path: "*", element: <NotFoundPage /> },
    ],
  },
]);

export default function App() {
  return (
    <ToastProvider>
      <CartProvider>
        <AuthProvider>
          <AccessibilityProvider>
            <RouterProvider router={router} />
          </AccessibilityProvider>
        </AuthProvider>
      </CartProvider>
    </ToastProvider>
  );
}
