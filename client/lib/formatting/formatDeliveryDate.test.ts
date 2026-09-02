import { describe, expect, it } from "vitest";
import { formatDeliveryDate } from "./formatDeliveryDate";

describe("formatDeliveryDate", () => {
  it("renders today plus three days as weekday, day month per the spec example", () => {
    const orderedOn = new Date(2023, 7, 18);
    expect(formatDeliveryDate(orderedOn)).toBe("Monday, 21 August");
  });

  it("rolls over into the next month when the offset crosses a month boundary", () => {
    const orderedOn = new Date(2023, 7, 30);
    expect(formatDeliveryDate(orderedOn)).toBe("Saturday, 2 September");
  });

  it("honours an explicit offset override", () => {
    const orderedOn = new Date(2023, 7, 18);
    expect(formatDeliveryDate(orderedOn, 1)).toBe("Saturday, 19 August");
  });
});
