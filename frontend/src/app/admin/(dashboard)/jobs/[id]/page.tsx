"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useState } from "react";
import { useApi } from "@/lib/use-api";
import { ApiError, apiFetch } from "@/lib/api";
import { EMPLOYMENT_TYPE_LABELS, formatDate, formatDateTime } from "@/lib/format";
import type { ApplicationStatus, ApplicationSummary, JobDetail } from "@/lib/types";
import { Loading } from "@/components/Loading";
import { Alert } from "@/components/Alert";
import { ApplicationStatusBadge, JobStatusBadge } from "@/components/StatusBadge";

const FILTERS: { value: ApplicationStatus | ""; label: string }[] = [
  { value: "", label: "All" },
  { value: "PENDING", label: "Pending" },
  { value: "ACCEPTED", label: "Accepted" },
  { value: "REJECTED", label: "Rejected" },
];

export default function JobApplicantsPage() {
  const { id } = useParams<{ id: string }>();
  const [filter, setFilter] = useState<ApplicationStatus | "">("");
  const [actionError, setActionError] = useState<string | null>(null);
  const job = useApi<JobDetail>(`/api/admin/jobs/${id}`);
  const applicants = useApi<ApplicationSummary[]>(
    `/api/admin/jobs/${id}/applications${filter ? `?status=${filter}` : ""}`,
  );

  async function toggleStatus() {
    if (!job.data) return;
    const action = job.data.status === "OPEN" ? "close" : "reopen";
    if (action === "close" && !confirm("Close this job? Candidates will no longer be able to apply.")) return;
    try {
      job.setData(await apiFetch<JobDetail>(`/api/admin/jobs/${id}/${action}`, { method: "POST" }));
    } catch (err) {
      setActionError(err instanceof ApiError ? err.message : "Action failed.");
    }
  }

  if (job.loading && !job.data) return <Loading />;
  if (job.error || !job.data) return <Alert kind="error">{job.error ?? "Job not found."}</Alert>;
  const j = job.data;

  return (
    <div className="space-y-6">
      <Link href="/admin/jobs" className="text-sm font-medium text-brand-600 hover:underline">
        ← All jobs
      </Link>

      <div className="card flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
        <div>
          <div className="flex flex-wrap items-center gap-2">
            <h1 className="text-2xl font-bold text-slate-900">{j.title}</h1>
            <JobStatusBadge accepting={j.acceptingApplications} closedByAdmin={j.status === "CLOSED"} />
          </div>
          <p className="mt-1 text-sm text-slate-500">
            {j.location} · {EMPLOYMENT_TYPE_LABELS[j.employmentType]} · Closes {formatDate(j.closingDate)}
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Link href={`/jobs/${j.id}`} target="_blank" className="btn-secondary">
            Public page ↗
          </Link>
          <Link href={`/admin/jobs/${j.id}/edit`} className="btn-secondary">
            Edit
          </Link>
          <button type="button" onClick={toggleStatus} className={j.status === "OPEN" ? "btn-danger" : "btn-success"}>
            {j.status === "OPEN" ? "Close job" : "Reopen job"}
          </button>
        </div>
      </div>
      {actionError && <Alert kind="error">{actionError}</Alert>}

      <section className="space-y-4">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <h2 className="text-lg font-semibold text-slate-900">Applicants</h2>
          <div role="tablist" aria-label="Filter by status" className="flex flex-wrap gap-1 rounded-lg bg-slate-100 p-1">
            {FILTERS.map((f) => (
              <button
                key={f.label}
                role="tab"
                aria-selected={filter === f.value}
                onClick={() => setFilter(f.value)}
                className={`rounded-md px-3 py-1.5 text-sm font-medium ${filter === f.value ? "bg-white text-brand-700 shadow-sm" : "text-slate-600 hover:text-slate-900"}`}
              >
                {f.label}
              </button>
            ))}
          </div>
        </div>

        {applicants.error ? (
          <Alert kind="error">{applicants.error}</Alert>
        ) : applicants.loading && !applicants.data ? (
          <Loading />
        ) : applicants.data && applicants.data.length === 0 ? (
          <div className="card text-center text-sm text-slate-600">
            {filter ? "No applications with this status." : "No applications yet."}
          </div>
        ) : (
          <ul className="card divide-y divide-slate-100 p-0">
            {applicants.data?.map((a) => (
              <li key={a.id}>
                <Link
                  href={`/admin/applications/${a.id}`}
                  className="flex flex-col gap-2 px-5 py-4 hover:bg-slate-50 sm:flex-row sm:items-center sm:justify-between"
                >
                  <div className="min-w-0">
                    <p className="font-medium text-slate-900">{a.fullName}</p>
                    <p className="truncate text-sm text-slate-500">
                      {a.email} · {a.phone}
                    </p>
                  </div>
                  <div className="flex items-center gap-3 text-sm text-slate-500">
                    <span>Applied {formatDateTime(a.createdAt)}</span>
                    <ApplicationStatusBadge status={a.status} />
                  </div>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
