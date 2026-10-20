import Link from "next/link";

export function Logo({ href = "/jobs", suffix = "Careers" }: { href?: string; suffix?: string }) {
  return (
    <Link href={href} className="flex items-center gap-2 font-bold tracking-tight text-brand-900">
      <span className="grid h-8 w-8 place-items-center rounded-lg bg-brand-600 text-sm text-white">B</span>
      <span>
        BrightCore <span className="font-medium text-brand-600">{suffix}</span>
      </span>
    </Link>
  );
}
