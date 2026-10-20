"use client";

import { useState } from "react";
import { ApiError, apiFetch } from "@/lib/api";
import { validateApplication } from "@/lib/validation";
import { Field, fieldProps } from "@/components/Field";
import { Alert } from "@/components/Alert";

export function ApplyForm({ jobId }: { jobId: number }) {
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [formError, setFormError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = event.currentTarget;
    const data = new FormData(form);
    const cv = data.get("cv");
    const file = cv instanceof File && cv.size > 0 ? cv : null;

    const clientErrors = validateApplication({
      fullName: String(data.get("fullName") ?? ""),
      email: String(data.get("email") ?? ""),
      phone: String(data.get("phone") ?? ""),
      coverNote: String(data.get("coverNote") ?? ""),
      cv: file,
    });
    setErrors(clientErrors);
    setFormError(null);
    if (Object.keys(clientErrors).length > 0) return;

    // Send trimmed values; the server validates everything again.
    for (const key of ["fullName", "email", "phone", "coverNote"]) {
      data.set(key, String(data.get(key)).trim());
    }

    setSubmitting(true);
    try {
      await apiFetch(`/api/jobs/${jobId}/applications`, { method: "POST", body: data });
      setSubmitted(true);
    } catch (err) {
      if (err instanceof ApiError) {
        setErrors(err.fieldErrors);
        setFormError(err.message);
      } else {
        setFormError("Could not submit your application. Check your connection and try again.");
      }
    } finally {
      setSubmitting(false);
    }
  }

  if (submitted) {
    return (
      <Alert kind="success">
        <p className="font-semibold">Application submitted 🎉</p>
        <p className="mt-1">
          Thank you for applying. We have received your application and will contact you by email once it has been
          reviewed.
        </p>
      </Alert>
    );
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="space-y-4">
      {formError && <Alert kind="error">{formError}</Alert>}

      <Field id="fullName" label="Full name" error={errors.fullName}>
        <input {...fieldProps("fullName", errors.fullName)} autoComplete="name" maxLength={120} />
      </Field>
      <Field id="email" label="Email" error={errors.email}>
        <input {...fieldProps("email", errors.email)} type="email" autoComplete="email" maxLength={255} />
      </Field>
      <Field id="phone" label="Phone number" error={errors.phone}>
        <input {...fieldProps("phone", errors.phone)} type="tel" autoComplete="tel" placeholder="+250 788 123 456" />
      </Field>
      <Field id="coverNote" label="Cover note" error={errors.coverNote} hint="A few lines on why you are a good fit (max 2,000 characters).">
        <textarea {...fieldProps("coverNote", errors.coverNote)} rows={5} maxLength={2000} />
      </Field>
      <Field id="cv" label="CV (PDF, max 5 MB)" error={errors.cv}>
        <input
          {...fieldProps("cv", errors.cv)}
          type="file"
          accept="application/pdf,.pdf"
          className={`input file:mr-3 file:rounded-md file:border-0 file:bg-brand-50 file:px-3 file:py-1 file:text-sm file:font-medium file:text-brand-700 ${errors.cv ? "input-error" : ""}`}
        />
      </Field>

      <button type="submit" disabled={submitting} className="btn-primary w-full">
        {submitting ? "Submitting…" : "Submit application"}
      </button>
    </form>
  );
}
