import { APPLICATION_STATUS_LABELS } from "@/lib/format";
import type { ApplicationStatus } from "@/lib/types";

const APPLICATION_STYLES: Record<ApplicationStatus, string> = {
  PENDING: "bg-amber-50 text-amber-800 ring-amber-200",
  ACCEPTED: "bg-emerald-50 text-emerald-800 ring-emerald-200",
  REJECTED: "bg-red-50 text-red-700 ring-red-200",
};

const base = "inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ring-1 ring-inset";

export function ApplicationStatusBadge({ status }: { status: ApplicationStatus }) {
  return <span className={`${base} ${APPLICATION_STYLES[status]}`}>{APPLICATION_STATUS_LABELS[status]}</span>;
}

/** "Open" only when the job is actually accepting applications (status OPEN and not past closing date). */
export function JobStatusBadge({ accepting, closedByAdmin }: { accepting: boolean; closedByAdmin: boolean }) {
  if (accepting) return <span className={`${base} bg-emerald-50 text-emerald-800 ring-emerald-200`}>Open</span>;
  return (
    <span className={`${base} bg-slate-100 text-slate-700 ring-slate-200`}>
      {closedByAdmin ? "Closed" : "Past closing date"}
    </span>
  );
}
