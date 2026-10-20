import { describe, expect, it } from "vitest";
import { MAX_CV_BYTES, validateApplication, type ApplicationInput } from "./validation";
import { formatDate, toLines } from "./format";

const valid: ApplicationInput = {
  fullName: "Aline Uwase",
  email: "aline@example.com",
  phone: "+250 788 123 456",
  coverNote: "I would love to join.",
  cv: { name: "cv.pdf", size: 1000, type: "application/pdf" },
};

describe("validateApplication", () => {
  it("accepts a valid application", () => {
    expect(validateApplication(valid)).toEqual({});
  });

  it("requires every field", () => {
    const errors = validateApplication({ fullName: " ", email: "", phone: "", coverNote: "", cv: null });
    expect(Object.keys(errors).sort()).toEqual(["coverNote", "cv", "email", "fullName", "phone"]);
  });

  it("rejects malformed email and phone", () => {
    const errors = validateApplication({ ...valid, email: "aline@", phone: "12ab" });
    expect(errors.email).toBe("Enter a valid email address.");
    expect(errors.phone).toMatch(/valid phone/);
  });

  it("only accepts PDFs up to 5 MB", () => {
    expect(validateApplication({ ...valid, cv: { name: "cv.docx", size: 10, type: "application/msword" } }).cv)
      .toBe("The CV must be a PDF file.");
    expect(validateApplication({ ...valid, cv: { name: "cv.pdf", size: MAX_CV_BYTES + 1, type: "application/pdf" } }).cv)
      .toBe("The CV must be 5 MB or smaller.");
  });
});

describe("format helpers", () => {
  it("formats dates without time-zone drift", () => {
    expect(formatDate("2026-11-01")).toBe("1 Nov 2026");
  });

  it("splits multi-line text into clean list items", () => {
    expect(toLines("- First\n\n• Second \n Third")).toEqual(["First", "Second", "Third"]);
  });
});
