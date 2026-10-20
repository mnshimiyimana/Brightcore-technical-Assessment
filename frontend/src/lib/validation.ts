// Client-side checks give instant feedback. The server repeats every check and is the source of truth.

export const MAX_CV_BYTES = 5 * 1024 * 1024;

export interface ApplicationInput {
  fullName: string;
  email: string;
  phone: string;
  coverNote: string;
  cv: { name: string; size: number; type: string } | null;
}

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const PHONE_PATTERN = /^\+?[0-9][0-9 -]{7,18}[0-9]$/;

export function validateApplication(input: ApplicationInput): Record<string, string> {
  const errors: Record<string, string> = {};
  const fullName = input.fullName.trim();
  const email = input.email.trim();
  const phone = input.phone.trim();
  const coverNote = input.coverNote.trim();

  if (!fullName) errors.fullName = "Full name is required.";
  else if (fullName.length > 120) errors.fullName = "Full name must be at most 120 characters.";

  if (!email) errors.email = "Email is required.";
  else if (!EMAIL_PATTERN.test(email)) errors.email = "Enter a valid email address.";

  if (!phone) errors.phone = "Phone number is required.";
  else if (!PHONE_PATTERN.test(phone)) errors.phone = "Enter a valid phone number, e.g. +250 788 123 456.";

  if (!coverNote) errors.coverNote = "Cover note is required.";
  else if (coverNote.length > 2000) errors.coverNote = "Cover note must be at most 2,000 characters.";

  if (!input.cv) errors.cv = "Please attach your CV as a PDF.";
  else if (!input.cv.name.toLowerCase().endsWith(".pdf") || input.cv.type !== "application/pdf")
    errors.cv = "The CV must be a PDF file.";
  else if (input.cv.size > MAX_CV_BYTES) errors.cv = "The CV must be 5 MB or smaller.";

  return errors;
}
