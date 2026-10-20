import { JobForm } from "@/components/JobForm";

export default function NewJobPage() {
  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <h1 className="text-2xl font-bold text-slate-900">Post a job</h1>
      <JobForm />
    </div>
  );
}
