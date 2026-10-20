"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useState } from "react";
import { useApi } from "@/lib/use-api";
import { ApiError, apiFetch } from "@/lib/api";
import { formatDateTime, formatFileSize } from "@/lib/format";
import type { ApplicationDetail } from "@/lib/types";
import { Loading } from "@/components/Loading";
import { Alert } from "@/components/Alert";
import { ApplicationStatusBadge } from "@/components/StatusBadge";

type Decision = "ACCEPTED" | "REJECTED";

export default function ApplicationPage() {
  const { id } = useParams<{ id: string }>();
  const { data: app, setData, error, loading } = useApi<ApplicationDetail>(`/api/admin/applications/${id}`);
  const [note, setNote] = useState<string | null>(null);
  const [saving, setSaving] = useState<Decision | null>(null);
  const [message, setMessage] = useState<{ kind: "success" | "error"; text: string } | null>(null);

  async function decide(status: Decision) {
    setSaving(status);
    setMessage(null);
    try {
      const updated = await apiFetch<ApplicationDetail>(`/api/admin/applications/${id}/status`, {
        method: "PATCH",
        body: JSON.stringify({ status, note: note ?? app?.decisionNote ?? "" }),
      });
      setData(updated);
      setNote(null);
      setMessage({
        kind: "success",
        text: `Application ${status === "ACCEPTED" ? "accepted" : "rejected"}. The applicant has been notified by email.`,
      });
    } catch (err) {
      setMessage({ kind: "error", text: err instanceof ApiError ? err.message : "Could not update the status." });
    } finally {
      setSaving(null);
    }
  }

  if (loading && !app) return <Loading />;
  if (error || !app) return <Alert kind="error">{error ?? "Application not found."}</Alert>;

  return (
    <div className="space-y-6">
      <Link href={`/admin/jobs/${app.jobId}`} className="text-sm font-medium text-brand-600 hover:underline">
        ← Applicants for {app.jobTitle}
      </Link>

      <div className="grid gap-6 lg:grid-cols-[1fr_340px]">
        <div className="card space-y-6">
          <div className="flex flex-wrap items-start justify-between gap-3">
            <div>
              <h1 className="text-2xl font-bold text-slate-900">{app.fullName}</h1>
              <p className="text-sm text-slate-500">Applied for {app.jobTitle} · {formatDateTime(app.createdAt)}</p>
            </div>
            <ApplicationStatusBadge status={app.status} />
          </div>

          <dl className="grid gap-4 sm:grid-cols-2">
            <div>
              <dt className="text-xs font-medium uppercase tracking-wide text-slate-500">Email</dt>
              <dd className="mt-1 break-all">
                <a href={`mailto:${app.email}`} className="text-brand-700 hover:underline">
                  {app.email}
                </a>
              </dd>
            </div>
            <div>
              <dt className="text-xs font-medium uppercase tracking-wide text-slate-500">Phone</dt>
              <dd className="mt-1">
                <a href={`tel:${app.phone}`} className="text-brand-700 hover:underline">
                  {app.phone}
                </a>
              </dd>
            </div>
          </dl>

          <div>
            <h2 className="text-xs font-medium uppercase tracking-wide text-slate-500">Cover note</h2>
            <p className="mt-2 whitespace-pre-line text-slate-700">{app.coverNote}</p>
          </div>

          <div className="flex flex-wrap items-center justify-between gap-3 rounded-lg border border-slate-200 bg-slate-50 px-4 py-3">
            <div className="min-w-0">
              <p className="truncate text-sm font-medium text-slate-900">📄 {app.cvFileName}</p>
              <p className="text-xs text-slate-500">{formatFileSize(app.cvSizeBytes)}</p>
            </div>
            {/* Same-origin link: the browser sends the HttpOnly cookie, the API checks it. */}
            <a href={`/api/admin/applications/${app.id}/cv`} className="btn-secondary">
              Download CV
            </a>
          </div>
        </div>

        <aside className="card space-y-4 lg:self-start">
          <h2 className="font-semibold text-slate-900">Decision</h2>
          {message && <Alert kind={message.kind}>{message.text}</Alert>}
          {app.status !== "PENDING" && (
            <p className="text-sm text-slate-600">
              Last updated {formatDateTime(app.updatedAt)}. You can change the decision if needed.
            </p>
          )}
          <div>
            <label htmlFor="note" className="label">
              Note <span className="font-normal text-slate-400">(optional)</span>
            </label>
            <textarea
              id="note"
              rows={4}
              maxLength={1000}
              className="input"
              value={note ?? app.decisionNote ?? ""}
              onChange={(e) => setNote(e.target.value)}
              placeholder="e.g. Strong portfolio, invite for interview"
            />
          </div>
          <div className="grid grid-cols-2 gap-2">
            <button type="button" onClick={() => decide("ACCEPTED")} disabled={saving !== null} className="btn-success">
              {saving === "ACCEPTED" ? "Saving…" : "Accept"}
            </button>
            <button type="button" onClick={() => decide("REJECTED")} disabled={saving !== null} className="btn-danger">
              {saving === "REJECTED" ? "Saving…" : "Reject"}
            </button>
          </div>
        </aside>
      </div>
    </div>
  );
}
