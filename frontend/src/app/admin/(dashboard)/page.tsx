"use client";

import Link from "next/link";
import { useApi } from "@/lib/use-api";
import type { Overview } from "@/lib/types";
import { Loading } from "@/components/Loading";
import { Alert } from "@/components/Alert";
import { JobStatusBadge } from "@/components/StatusBadge";

const STAT_CARDS = [
  { key: "total", label: "Total applications", style: "text-slate-900" },
  { key: "pending", label: "Pending", style: "text-amber-700" },
  { key: "accepted", label: "Accepted", style: "text-emerald-700" },
  { key: "rejected", label: "Rejected", style: "text-red-700" },
] as const;

export default function OverviewPage() {
  const { data, error, loading } = useApi<Overview>("/api/admin/overview");

  if (loading && !data) return <Loading />;
  if (error || !data) return <Alert kind="error">{error ?? "Could not load overview."}</Alert>;

  return (
    <div className="space-y-8">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-2xl font-bold text-slate-900">Overview</h1>
        <Link href="/admin/jobs/new" className="btn-primary">
          + Post a job
        </Link>
      </div>

      <section className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        {STAT_CARDS.map((card) => (
          <div key={card.key} className="card">
            <p className="text-sm text-slate-500">{card.label}</p>
            <p className={`mt-1 text-3xl font-bold ${card.style}`}>{data.totals[card.key]}</p>
          </div>
        ))}
      </section>

      <section className="card overflow-hidden p-0">
        <h2 className="border-b border-slate-200 px-5 py-4 font-semibold text-slate-900">Applications per job</h2>
        {data.jobs.length === 0 ? (
          <p className="px-5 py-6 text-sm text-slate-500">No jobs yet.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full text-sm">
              <thead className="bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
                <tr>
                  <th className="px-5 py-3 font-medium">Job</th>
                  <th className="px-3 py-3 text-right font-medium">Total</th>
                  <th className="px-3 py-3 text-right font-medium">Pending</th>
                  <th className="px-3 py-3 text-right font-medium">Accepted</th>
                  <th className="px-5 py-3 text-right font-medium">Rejected</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {data.jobs.map((job) => (
                  <tr key={job.id} className="hover:bg-slate-50">
                    <td className="px-5 py-3">
                      <Link href={`/admin/jobs/${job.id}`} className="font-medium text-brand-700 hover:underline">
                        {job.title}
                      </Link>
                      <div className="mt-1">
                        <JobStatusBadge accepting={job.acceptingApplications} closedByAdmin={job.status === "CLOSED"} />
                      </div>
                    </td>
                    <td className="px-3 py-3 text-right font-semibold">{job.applications.total}</td>
                    <td className="px-3 py-3 text-right text-amber-700">{job.applications.pending}</td>
                    <td className="px-3 py-3 text-right text-emerald-700">{job.applications.accepted}</td>
                    <td className="px-5 py-3 text-right text-red-700">{job.applications.rejected}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}
