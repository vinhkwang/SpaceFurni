const amountFormatter = new Intl.NumberFormat("de-DE");

export function formatMoney(amountInDong: number): string {
  return `${amountFormatter.format(amountInDong)} ₫`;
}
