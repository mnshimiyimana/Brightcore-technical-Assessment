import Link from "next/link";
import type { Metadata } from "next";
import { serverFetch } from "@/lib/server-api";
import { EMPLOYMENT_TYPE_LABELS, formatDate } from "@/lib/format";
import type { EmploymentType, JobSummary, Page } from "@/lib/types";

export const metadata: Metadata = { title: "Open jobs" };

const PAGE_SIZE = 6;

type SearchParams = Promise<{ search?: string; type?: string; page?: string }>;

export default async function JobsPage({ searchParams }: { searchParams: SearchParams }) {
  const { search = "", type = "", page = "1" } = await searchParams;
  const pageNumber = Math.max(1, Number.parseInt(page, 10) || 1);
  const validType = type in EMPLOYMENT_TYPE_LABELS ? (type as EmploymentType) : "";

  const query = new URLSearchParams({ page: String(pageNumber - 1), size: String(PAGE_SIZE) });
  if (search.trim()) query.set("search", search.trim());
  if (validType) query.set("type", validType);
  const jobs = (await serverFetch<Page<JobSummary>>(`/api/jobs?${query}`))!;

  const filtered = Boolean(search.trim() || validType);
  const pageLink = (p: number) => {
    const q = new URLSearchParams();
    if (search.trim()) q.set("search", search.trim());
    if (validType) q.set("type", validType);
    if (p > 1) q.set("page", String(p));
    const qs = q.toString();
    return qs ? `/jobs?${qs}` : "/jobs";
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 sm:text-3xl">Open positions</h1>
        <p className="mt-1 text-slate-600">Join us in building digital tools for SACCOs and financial institutions.</p>
      </div>

      {/* A plain GET form: works without JavaScript and keeps filters in the URL (shareable). */}
      <form action="/jobs" className="card flex flex-col gap-3 p-4 sm:flex-row sm:items-end">
        <div className="flex-1">
          <label htmlFor="search" className="label">
            Search
          </label>
          <input id="search" name="search" defaultValue={search} placeholder="Job title, location or keyword" className="input" />
        </div>
        <div className="sm:w-48">
          <label htmlFor="type" className="label">
            Employment type
          </label>
          <select id="type" name="type" defaultValue={validType} className="input">
            <option value="">All types</option>
            {Object.entries(EMPLOYMENT_TYPE_LABELS).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
        </div>
        <div className="flex gap-2">
          <button type="submit" className="btn-primary flex-1 sm:flex-none">
            Search
          </button>
          {filtered && (
            <Link href="/jobs" className="btn-secondary">
              Clear
            </Link>
          )}
        </div>
      </form>

      <p className="text-sm text-slate-500">
        {jobs.totalItems} {jobs.totalItems === 1 ? "job" : "jobs"} {filtered ? "match your search" : "open"}
      </p>

      {jobs.items.length === 0 ? (
        <div className="card text-center text-slate-600">
          {filtered ? "No jobs match your search. Try different keywords." : "There are no open positions right now. Please check back soon."}
        </div>
      ) : (
        <ul className="grid gap-4 md:grid-cols-2">
          {jobs.items.map((job) => (
            <li key={job.id}>
              <Link
                href={`/jobs/${job.id}`}
                className="card flex h-full flex-col transition hover:border-brand-500 hover:shadow-md"
              >
                <div className="flex flex-wrap items-start justify-between gap-2">
                  <h2 className="text-lg font-semibold text-slate-900">{job.title}</h2>
                  <span className="rounded-full bg-brand-50 px-2.5 py-0.5 text-xs font-medium text-brand-700">
                    {EMPLOYMENT_TYPE_LABELS[job.employmentType]}
                  </span>
                </div>
                <p className="mt-1 text-sm text-slate-500">{job.location}</p>
                <p className="mt-3 flex-1 text-sm text-slate-700">{job.summary}</p>
                <div className="mt-4 flex items-center justify-between text-sm">
                  <span className="text-slate-500">Closes {formatDate(job.closingDate)}</span>
                  <span className="font-medium text-brand-600">View details →</span>
                </div>
              </Link>
            </li>
          ))}
        </ul>
      )}

      {jobs.totalPages > 1 && (
        <nav className="flex items-center justify-between" aria-label="Pagination">
          {pageNumber > 1 ? (
            <Link href={pageLink(pageNumber - 1)} className="btn-secondary">
              ← Previous
            </Link>
          ) : (
            <span />
          )}
          <span className="text-sm text-slate-500">
            Page {pageNumber} of {jobs.totalPages}
          </span>
          {pageNumber < jobs.totalPages ? (
            <Link href={pageLink(pageNumber + 1)} className="btn-secondary">
              Next →
            </Link>
          ) : (
            <span />
          )}
        </nav>
      )}
    </div>
  );
}
