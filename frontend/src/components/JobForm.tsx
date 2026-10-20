"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { ApiError, apiFetch } from "@/lib/api";
import { EMPLOYMENT_TYPE_LABELS } from "@/lib/format";
import type { JobDetail } from "@/lib/types";
import { Field, fieldProps } from "./Field";
import { Alert } from "./Alert";

/** Create and edit form for jobs. Validation messages come from the server. */
export function JobForm({ job }: { job?: JobDetail }) {
  const router = useRouter();
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [formError, setFormError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const data = Object.fromEntries(new FormData(event.currentTarget));
    setSaving(true);
    setErrors({});
    setFormError(null);
    try {
      const saved = await apiFetch<JobDetail>(job ? `/api/admin/jobs/${job.id}` : "/api/admin/jobs", {
        method: job ? "PUT" : "POST",
        body: JSON.stringify({ ...data, employmentType: data.employmentType || null, closingDate: data.closingDate || null }),
      });
      router.push(`/admin/jobs/${saved.id}`);
    } catch (err) {
      if (err instanceof ApiError) {
        setErrors(err.fieldErrors);
        setFormError(err.message);
      } else {
        setFormError("Could not save the job. Please try again.");
      }
      setSaving(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="card space-y-5">
      {formError && <Alert kind="error">{formError}</Alert>}

      <Field id="title" label="Job title" error={errors.title}>
        <input {...fieldProps("title", errors.title)} defaultValue={job?.title} maxLength={150} />
      </Field>

      <div className="grid gap-5 sm:grid-cols-3">
        <Field id="location" label="Location" error={errors.location}>
          <input {...fieldProps("location", errors.location)} defaultValue={job?.location} maxLength={120} placeholder="Kigali, Rwanda" />
        </Field>
        <Field id="employmentType" label="Employment type" error={errors.employmentType}>
          <select {...fieldProps("employmentType", errors.employmentType)} defaultValue={job?.employmentType ?? ""}>
            <option value="" disabled>
              Select…
            </option>
            {Object.entries(EMPLOYMENT_TYPE_LABELS).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
        </Field>
        <Field id="closingDate" label="Closing date" error={errors.closingDate}>
          <input {...fieldProps("closingDate", errors.closingDate)} type="date" defaultValue={job?.closingDate} />
        </Field>
      </div>

      <Field id="summary" label="Short summary" error={errors.summary} hint="Shown on the jobs list (max 300 characters).">
        <textarea {...fieldProps("summary", errors.summary)} rows={2} defaultValue={job?.summary} maxLength={300} />
      </Field>
      <Field id="description" label="Full description" error={errors.description}>
        <textarea {...fieldProps("description", errors.description)} rows={5} defaultValue={job?.description} />
      </Field>
      <Field id="responsibilities" label="Responsibilities" error={errors.responsibilities} hint="One per line.">
        <textarea {...fieldProps("responsibilities", errors.responsibilities)} rows={5} defaultValue={job?.responsibilities} />
      </Field>
      <Field id="requirements" label="Requirements" error={errors.requirements} hint="One per line.">
        <textarea {...fieldProps("requirements", errors.requirements)} rows={5} defaultValue={job?.requirements} />
      </Field>

      <div className="flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
        <button type="button" onClick={() => router.back()} className="btn-secondary">
          Cancel
        </button>
        <button type="submit" disabled={saving} className="btn-primary">
          {saving ? "Saving…" : job ? "Save changes" : "Publish job"}
        </button>
      </div>
    </form>
  );
}
