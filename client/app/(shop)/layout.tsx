import { ShopFooter } from "@/components/layout/ShopFooter";
import { ShopHeader } from "@/components/layout/ShopHeader";

export default function ShopLayout({ children }: { children: React.ReactNode }) {
  return (
    <>
      <ShopHeader />
      <div className="flex-1">{children}</div>
      <ShopFooter />
    </>
  );
}
