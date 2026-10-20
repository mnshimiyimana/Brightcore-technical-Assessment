import Link from "next/link";
import { Logo } from "@/components/Logo";

export default function PublicLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex min-h-screen flex-col">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-4">
          <Logo />
          <nav className="flex items-center gap-4 text-sm font-medium">
            <Link href="/jobs" className="text-slate-700 hover:text-brand-600">
              Open jobs
            </Link>
            <Link href="/admin" className="text-slate-500 hover:text-brand-600">
              Admin
            </Link>
          </nav>
        </div>
      </header>
      <main className="mx-auto w-full max-w-5xl flex-1 px-4 py-8">{children}</main>
      <footer className="border-t border-slate-200 bg-white py-6 text-center text-xs text-slate-500">
        © {new Date().getFullYear()} BrightCore. All rights reserved.
      </footer>
    </div>
  );
}
