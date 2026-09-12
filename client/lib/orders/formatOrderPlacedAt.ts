const placedDateFormatter = new Intl.DateTimeFormat("en-GB", { day: "numeric", month: "short", year: "numeric" });
const placedTimeFormatter = new Intl.DateTimeFormat("en-GB", { hour: "2-digit", minute: "2-digit", hour12: false });

export type OrderPlacedAtParts = {
  date: string;
  time: string;
};

export function formatOrderPlacedAt(placedAtIso: string): OrderPlacedAtParts {
  const placedAtDate = new Date(placedAtIso);
  return {
    date: placedDateFormatter.format(placedAtDate),
    time: placedTimeFormatter.format(placedAtDate),
  };
}
