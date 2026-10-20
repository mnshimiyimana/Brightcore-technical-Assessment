"use client";

import Link from "next/link";
import { useState } from "react";
import { ApiError, apiFetch } from "@/lib/api";
import { useApi } from "@/lib/use-api";
import { EMPLOYMENT_TYPE_LABELS, formatDate } from "@/lib/format";
import type { AdminJobRow } from "@/lib/types";
import { Loading } from "@/components/Loading";
import { Alert } from "@/components/Alert";
import { JobStatusBadge } from "@/components/StatusBadge";

export default function AdminJobsPage() {
  const { data: jobs, error, loading, reload } = useApi<AdminJobRow[]>("/api/admin/jobs");
  const [actionError, setActionError] = useState<string | null>(null);
  const [busyId, setBusyId] = useState<number | null>(null);

  async function toggle(job: AdminJobRow) {
    const action = job.status === "OPEN" ? "close" : "reopen";
    if (action === "close" && !confirm(`Close "${job.title}"? Candidates will no longer be able to apply.`)) return;
    setBusyId(job.id);
    setActionError(null);
    try {
      await apiFetch(`/api/admin/jobs/${job.id}/${action}`, { method: "POST" });
      reload();
    } catch (err) {
      setActionError(err instanceof ApiError ? err.message : "Action failed.");
    } finally {
      setBusyId(null);
    }
  }

  if (loading && !jobs) return <Loading />;
  if (error || !jobs) return <Alert kind="error">{error ?? "Could not load jobs."}</Alert>;

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-2xl font-bold text-slate-900">Jobs</h1>
        <Link href="/admin/jobs/new" className="btn-primary">
          + Post a job
        </Link>
      </div>
      {actionError && <Alert kind="error">{actionError}</Alert>}

      {jobs.length === 0 ? (
        <div className="card text-center text-slate-600">No jobs yet. Post your first job to get started.</div>
      ) : (
        <ul className="space-y-3">
          {jobs.map((job) => (
            <li key={job.id} className="card flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
              <div className="min-w-0">
                <div className="flex flex-wrap items-center gap-2">
                  <Link href={`/admin/jobs/${job.id}`} className="text-lg font-semibold text-slate-900 hover:text-brand-700">
                    {job.title}
                  </Link>
                  <JobStatusBadge accepting={job.acceptingApplications} closedByAdmin={job.status === "CLOSED"} />
                </div>
                <p className="mt-1 text-sm text-slate-500">
                  {job.location} · {EMPLOYMENT_TYPE_LABELS[job.employmentType]} · Closes {formatDate(job.closingDate)}
                </p>
                <p className="mt-1 text-sm text-slate-600">
                  {job.applications.total} applications ({job.applications.pending} pending)
                </p>
              </div>
              <div className="flex flex-wrap gap-2">
                <Link href={`/admin/jobs/${job.id}`} className="btn-primary">
                  Applicants
                </Link>
                <Link href={`/admin/jobs/${job.id}/edit`} className="btn-secondary">
                  Edit
                </Link>
                <button type="button" onClick={() => toggle(job)} disabled={busyId === job.id} className="btn-secondary">
                  {job.status === "OPEN" ? "Close" : "Reopen"}
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
