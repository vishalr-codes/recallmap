import type { HTMLAttributes } from 'react';

export function Surface({ className = '', ...props }: HTMLAttributes<HTMLElement>) {
  return (
    <section
      className={`rounded-2xl border border-white/[0.08] bg-[#0c1310] ${className}`}
      {...props}
    />
  );
}
