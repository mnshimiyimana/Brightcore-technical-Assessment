import Link from "next/link";
import { notFound } from "next/navigation";
import type { Metadata } from "next";
import { serverFetch } from "@/lib/server-api";
import { EMPLOYMENT_TYPE_LABELS, formatDate } from "@/lib/format";
import type { JobDetail } from "@/lib/types";
import { BulletList } from "@/components/BulletList";
import { Alert } from "@/components/Alert";
import { ApplyForm } from "./ApplyForm";

type Params = Promise<{ id: string }>;

async function loadJob(id: string) {
  if (!/^\d+$/.test(id)) return null;
  return serverFetch<JobDetail>(`/api/jobs/${id}`);
}

export async function generateMetadata({ params }: { params: Params }): Promise<Metadata> {
  const job = await loadJob((await params).id);
  return job ? { title: job.title, description: job.summary } : {};
}

export default async function JobDetailsPage({ params }: { params: Params }) {
  const job = await loadJob((await params).id);
  if (!job) notFound();

  return (
    <div className="space-y-6">
      <Link href="/jobs" className="text-sm font-medium text-brand-600 hover:underline">
        ← All jobs
      </Link>

      <div className="grid gap-6 lg:grid-cols-[1fr_380px]">
        <article className="card space-y-6">
          <header>
            <h1 className="text-2xl font-bold text-slate-900 sm:text-3xl">{job.title}</h1>
            <dl className="mt-3 flex flex-wrap gap-x-6 gap-y-2 text-sm text-slate-600">
              <div>
                <dt className="sr-only">Location</dt>
                <dd>📍 {job.location}</dd>
              </div>
              <div>
                <dt className="sr-only">Employment type</dt>
                <dd>💼 {EMPLOYMENT_TYPE_LABELS[job.employmentType]}</dd>
              </div>
              <div>
                <dt className="sr-only">Closing date</dt>
                <dd>📅 Closes {formatDate(job.closingDate)}</dd>
              </div>
            </dl>
          </header>

          <section>
            <h2 className="mb-2 text-lg font-semibold text-slate-900">About the role</h2>
            <p className="whitespace-pre-line text-slate-700">{job.description}</p>
          </section>
          <section>
            <h2 className="mb-2 text-lg font-semibold text-slate-900">Responsibilities</h2>
            <BulletList text={job.responsibilities} />
          </section>
          <section>
            <h2 className="mb-2 text-lg font-semibold text-slate-900">Requirements</h2>
            <BulletList text={job.requirements} />
          </section>
        </article>

        <aside className="lg:sticky lg:top-6 lg:self-start">
          {job.acceptingApplications ? (
            <div className="card">
              <h2 className="mb-4 text-lg font-semibold text-slate-900">Apply for this job</h2>
              <ApplyForm jobId={job.id} />
            </div>
          ) : (
            <Alert kind="info">
              <p className="font-medium">Applications are closed</p>
              <p className="mt-1">This position is no longer accepting applications.</p>
              <Link href="/jobs" className="mt-2 inline-block font-medium underline">
                See other open jobs
              </Link>
            </Alert>
          )}
        </aside>
      </div>
    </div>
  );
}
