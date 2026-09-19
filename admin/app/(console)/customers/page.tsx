import { getDictionary } from "@/lib/i18n/getDictionary";
import { getLocale } from "@/lib/i18n/locale";

const CUSTOMER_TABLE_COLUMN_GRID_CLASS_NAME = "grid grid-cols-[2fr_1.4fr_110px_90px_140px_110px] items-center gap-4";

export default async function CustomersPage() {
  const dictionary = getDictionary(await getLocale());

  return (
    <div className="rounded-2xl border border-hairline-soft bg-white p-6.5">
      <div
        className={`${CUSTOMER_TABLE_COLUMN_GRID_CLASS_NAME} border-b border-hairline-soft px-2.5 pb-3.5 text-[10px] uppercase tracking-[0.14em] text-ink-muted`}
      >
        <span>{dictionary.customers.columnCustomer}</span>
        <span>{dictionary.customers.columnContact}</span>
        <span>{dictionary.customers.columnDistrict}</span>
        <span className="text-right">{dictionary.customers.columnOrders}</span>
        <span className="text-right">{dictionary.customers.columnLifetime}</span>
        <span className="text-center">{dictionary.customers.columnTier}</span>
      </div>
      <div className="py-15 text-center text-[13px] text-ink-muted">{dictionary.customers.plannedNotInScope}</div>
    </div>
  );
}
