const DEFAULT_DELIVERY_OFFSET_DAYS = 3;

const weekdayFormatter = new Intl.DateTimeFormat("en-GB", { weekday: "long" });
const dayMonthFormatter = new Intl.DateTimeFormat("en-GB", { day: "numeric", month: "long" });

export function formatDeliveryDate(fromDate: Date, offsetDays: number = DEFAULT_DELIVERY_OFFSET_DAYS): string {
  const deliveryDate = new Date(fromDate);
  deliveryDate.setDate(deliveryDate.getDate() + offsetDays);
  return `${weekdayFormatter.format(deliveryDate)}, ${dayMonthFormatter.format(deliveryDate)}`;
}
