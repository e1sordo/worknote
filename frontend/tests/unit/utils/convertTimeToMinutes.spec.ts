import { convertTimeToMinutes } from '@/utils/convertTimeToMinutes';

describe("convertTimeToMinutes", () => {
  it("should convert time with hours and minutes correctly", () => {
    expect(convertTimeToMinutes("2ч 15м")).toBe(135);
  });

  it("should convert time with english hours and minutes correctly", () => {
    expect(convertTimeToMinutes("2h 15m")).toBe(135);
  });

  it("should convert time with only hours correctly", () => {
    expect(convertTimeToMinutes("3ч")).toBe(180);
  });

  it("should convert time with only minutes correctly", () => {
    expect(convertTimeToMinutes("20м")).toBe(20);
  });

  it("should convert numeric time to hours (*60) if less than 5", () => {
    expect(convertTimeToMinutes("8")).toBe(480);
  });

  it("should convert numeric time to minutes if greater or equal to 5", () => {
    expect(convertTimeToMinutes("9")).toBe(9);
  });

  it("should throw an error for invalid time format", () => {
    expect(() => convertTimeToMinutes("invalid")).toThrowError("Incorrect format of time string");
  });
});
