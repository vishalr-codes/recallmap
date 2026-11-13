import type { ButtonHTMLAttributes } from 'react';

type ButtonVariant = 'primary' | 'secondary' | 'quiet';

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: ButtonVariant;
};

const variantClasses: Record<ButtonVariant, string> = {
  primary: 'bg-emerald-400 text-emerald-950 hover:bg-emerald-300 focus-visible:ring-emerald-300',
  secondary: 'border border-white/10 bg-white/[0.04] text-slate-200 hover:border-emerald-500/30 hover:bg-white/[0.07] focus-visible:ring-emerald-400',
  quiet: 'text-slate-400 hover:bg-white/[0.05] hover:text-white focus-visible:ring-emerald-400',
};

export function Button({ className = '', variant = 'secondary', type = 'button', ...props }: ButtonProps) {
  return (
    <button
      type={type}
      className={`inline-flex min-h-10 items-center justify-center gap-2 rounded-xl px-3.5 text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-offset-[#080d0b] disabled:pointer-events-none disabled:opacity-50 ${variantClasses[variant]} ${className}`}
      {...props}
    />
  );
}
