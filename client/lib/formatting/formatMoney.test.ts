import { describe, expect, it } from "vitest";
import { formatMoney } from "./formatMoney";

describe("formatMoney", () => {
  it("formats a large amount with dot thousands separators and a trailing dong sign", () => {
    expect(formatMoney(24900000)).toBe("24.900.000 ₫");
  });

  it("formats an amount below one thousand without separators", () => {
    expect(formatMoney(300)).toBe("300 ₫");
  });

  it("formats zero", () => {
    expect(formatMoney(0)).toBe("0 ₫");
  });

  it("formats the standard shipping fee", () => {
    expect(formatMoney(300000)).toBe("300.000 ₫");
  });
});
