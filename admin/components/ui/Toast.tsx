"use client";

import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from "react";

type ToastContextValue = {
  showToast: (message: string) => void;
};

const ToastContext = createContext<ToastContextValue | null>(null);

const TOAST_VISIBLE_DURATION_MS = 2700;

function CheckCircleIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="h-3.5 w-3.5 shrink-0 text-brass" fill="currentColor">
      <path
        fillRule="evenodd"
        clipRule="evenodd"
        d="M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20Zm4.53 7.53-5.5 5.5a.75.75 0 0 1-1.06 0l-2.5-2.5a.75.75 0 1 1 1.06-1.06L10.5 13.44l4.97-4.97a.75.75 0 1 1 1.06 1.06Z"
      />
    </svg>
  );
}

export function ToastProvider({ children }: { children: ReactNode }) {
  const [message, setMessage] = useState("");
  const [toastKey, setToastKey] = useState(0);

  useEffect(() => {
    if (!message) {
      return;
    }
    const timeoutId = setTimeout(() => setMessage(""), TOAST_VISIBLE_DURATION_MS);
    return () => clearTimeout(timeoutId);
  }, [message, toastKey]);

  const showToast = useCallback((nextMessage: string) => {
    setMessage(nextMessage);
    setToastKey((previousKey) => previousKey + 1);
  }, []);

  return (
    <ToastContext.Provider value={{ showToast }}>
      {children}
      {message ? (
        <div
          key={toastKey}
          role="status"
          className="animate-toast-in fixed bottom-8.5 left-1/2 z-50 flex items-center gap-3.5 rounded-pill bg-deep px-6 py-3.5 text-white shadow-[0_24px_50px_-24px_rgba(26,24,21,0.7)]"
        >
          <CheckCircleIcon />
          <span className="text-[12.5px] tracking-[0.03em]">{message}</span>
        </div>
      ) : null}
    </ToastContext.Provider>
  );
}

export function useToast(): ToastContextValue {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error("useToast must be used within a ToastProvider");
  }
  return context;
}
