import { formatDeliveryDate } from "@/lib/formatting/formatDeliveryDate";
import type { AdminOrderCustomerResponse, AdminOrderDeliveryAddressResponse, DeliveryWindow } from "@/lib/api/types";

type OrderCustomerPanelProps = {
  customer: AdminOrderCustomerResponse;
  deliveryAddress: AdminOrderDeliveryAddressResponse;
  deliveryWindow: DeliveryWindow;
  placedAt: string;
};

const NEXT_DAY_DELIVERY_OFFSET_DAYS = 1;
const STANDARD_DELIVERY_OFFSET_DAYS = 3;

function deliveryOffsetDays(deliveryWindow: DeliveryWindow): number {
  return deliveryWindow === "NEXT_DAY" ? NEXT_DAY_DELIVERY_OFFSET_DAYS : STANDARD_DELIVERY_OFFSET_DAYS;
}

function initialsFor(fullName: string): string {
  return fullName
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((namePart) => namePart[0]?.toUpperCase())
    .join("");
}

function PhoneIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="mt-0.5 h-3 w-3 shrink-0 fill-current text-terracotta">
      <path d="M6.6 10.8c1.4 2.8 3.8 5.1 6.6 6.6l2.2-2.2c.3-.3.7-.4 1-.2 1.1.4 2.3.6 3.6.6.6 0 1 .4 1 1V20c0 .6-.4 1-1 1C10.5 21 3 13.5 3 4.6c0-.6.4-1 1-1h3.4c.6 0 1 .4 1 1 0 1.3.2 2.5.6 3.6.1.3 0 .7-.2 1z" />
    </svg>
  );
}

function MapPinIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden className="mt-0.5 h-3.5 w-3.5 shrink-0 fill-current text-terracotta">
      <path d="M12 2C7.6 2 4 5.6 4 10c0 5.6 7 11.5 7.3 11.7a1 1 0 0 0 1.4 0C13 21.5 20 15.6 20 10c0-4.4-3.6-8-8-8zm0 10.8a2.8 2.8 0 1 1 0-5.6 2.8 2.8 0 0 1 0 5.6z" />
    </svg>
  );
}

function TruckIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden
      className="mt-0.5 h-3 w-3 shrink-0 stroke-current text-terracotta"
      fill="none"
      strokeWidth={1.8}
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M2 6h11v10H2z" />
      <path d="M13 10h4l4 3.5V16h-8z" />
      <circle cx="6.5" cy="17.5" r="1.7" />
      <circle cx="16.5" cy="17.5" r="1.7" />
    </svg>
  );
}

export function OrderCustomerPanel({ customer, deliveryAddress, deliveryWindow, placedAt }: OrderCustomerPanelProps) {
  const deliveryDateLabel = formatDeliveryDate(new Date(placedAt), deliveryOffsetDays(deliveryWindow));
  const addressLine = [deliveryAddress.street, deliveryAddress.district, deliveryAddress.city].join(", ");

  return (
    <div className="rounded-2xl border border-hairline-soft bg-white p-6.5">
      <div className="mb-5 text-[10.5px] uppercase tracking-[0.18em] text-ink-muted">Customer</div>

      <div className="mb-5.5 flex items-center gap-3.5">
        <span className="flex h-11 w-11 items-center justify-center rounded-pill bg-deep text-[13px] font-semibold text-white">
          {initialsFor(customer.fullName)}
        </span>
        <div>
          <div className="text-[14px] font-semibold text-ink">{customer.fullName}</div>
          <div className="mt-0.5 text-[11.5px] text-ink-muted">{customer.email}</div>
        </div>
      </div>

      <div className="flex flex-col gap-3.5">
        <div className="flex items-start gap-3">
          <PhoneIcon />
          <div>
            <div className="mb-1 text-[10px] uppercase tracking-[0.14em] text-ink-muted">Phone</div>
            <div className="text-[12.5px] text-ink">{customer.phone}</div>
          </div>
        </div>
        <div className="flex items-start gap-3">
          <MapPinIcon />
          <div>
            <div className="mb-1 text-[10px] uppercase tracking-[0.14em] text-ink-muted">Delivery address</div>
            <div className="text-[12.5px] leading-[1.55] text-ink">
              {addressLine}
              {deliveryAddress.note ? (
                <>
                  <br />
                  {deliveryAddress.note}
                </>
              ) : null}
            </div>
          </div>
        </div>
        <div className="flex items-start gap-3">
          <TruckIcon />
          <div>
            <div className="mb-1 text-[10px] uppercase tracking-[0.14em] text-ink-muted">Delivery window</div>
            <div className="text-[12.5px] text-ink">{deliveryDateLabel}, 9:00–18:00</div>
          </div>
        </div>
      </div>
    </div>
  );
}
