import type { Metadata } from "next";
import { Montserrat } from "next/font/google";
import { getLocale } from "@/lib/i18n/locale";
import { LocaleProvider } from "@/lib/i18n/LocaleProvider";
import "./globals.css";

const montserrat = Montserrat({
  variable: "--font-montserrat",
  subsets: ["latin"],
  weight: ["300", "400", "500", "600", "700"],
});

export const metadata: Metadata = {
  title: {
    template: "%s | SpaceFurni",
    default: "SpaceFurni — furniture made in Hanoi",
  },
  description:
    "Sofas, tables and storage designed as considered sets — made in Hanoi and delivered across Vietnam.",
};

export default async function RootLayout({ children }: LayoutProps<"/">) {
  const locale = await getLocale();

  return (
    <html lang={locale} className={`${montserrat.variable} h-full antialiased`}>
      <body className="min-h-full flex flex-col">
        <LocaleProvider locale={locale}>{children}</LocaleProvider>
      </body>
    </html>
  );
}
