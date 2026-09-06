const CUSTOMER_TABLE_COLUMN_GRID_CLASS_NAME = "grid grid-cols-[2fr_1.4fr_110px_90px_140px_110px] items-center gap-4";

export default function CustomersPage() {
  return (
    <div className="rounded-2xl border border-hairline-soft bg-white p-6.5">
      <div
        className={`${CUSTOMER_TABLE_COLUMN_GRID_CLASS_NAME} border-b border-hairline-soft px-2.5 pb-3.5 text-[10px] uppercase tracking-[0.14em] text-ink-muted`}
      >
        <span>Customer</span>
        <span>Contact</span>
        <span>District</span>
        <span className="text-right">Orders</span>
        <span className="text-right">Lifetime</span>
        <span className="text-center">Tier</span>
      </div>
      <div className="py-15 text-center text-[13px] text-ink-muted">Planned — not part of MVP scope</div>
    </div>
  );
}
