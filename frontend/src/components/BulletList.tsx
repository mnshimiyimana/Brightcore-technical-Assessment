import { toLines } from "@/lib/format";

export function BulletList({ text }: { text: string }) {
  return (
    <ul className="list-disc space-y-1.5 pl-5 text-slate-700 marker:text-brand-500">
      {toLines(text).map((line, i) => (
        <li key={i}>{line}</li>
      ))}
    </ul>
  );
}
