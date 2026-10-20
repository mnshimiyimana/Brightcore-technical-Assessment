"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useApi } from "@/lib/use-api";
import type { JobDetail } from "@/lib/types";
import { JobForm } from "@/components/JobForm";
import { Loading } from "@/components/Loading";
import { Alert } from "@/components/Alert";

export default function EditJobPage() {
  const { id } = useParams<{ id: string }>();
  const { data: job, error, loading } = useApi<JobDetail>(`/api/admin/jobs/${id}`);

  if (loading && !job) return <Loading />;
  if (error || !job) return <Alert kind="error">{error ?? "Job not found."}</Alert>;

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <Link href={`/admin/jobs/${job.id}`} className="text-sm font-medium text-brand-600 hover:underline">
        ← Back to applicants
      </Link>
      <h1 className="text-2xl font-bold text-slate-900">Edit job</h1>
      <JobForm job={job} />
    </div>
  );
}
