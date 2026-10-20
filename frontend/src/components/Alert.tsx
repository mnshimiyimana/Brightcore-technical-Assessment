const STYLES = {
  error: "border-red-200 bg-red-50 text-red-800",
  success: "border-emerald-200 bg-emerald-50 text-emerald-800",
  info: "border-brand-100 bg-brand-50 text-brand-900",
};

export function Alert({ kind, children }: { kind: keyof typeof STYLES; children: React.ReactNode }) {
  return (
    <div role={kind === "error" ? "alert" : "status"} className={`rounded-lg border px-4 py-3 text-sm ${STYLES[kind]}`}>
      {children}
    </div>
  );
}
