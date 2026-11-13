/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      boxShadow: {
        subtle: '0 12px 32px rgba(0, 0, 0, 0.22)',
      },
      colors: {
        obs: {
          950: '#080d0b',
          900: '#0c1310',
          800: '#111916',
          700: '#1b2922',
          600: '#315141',
          500: '#34d399',
          400: '#6ee7b7',
        },
      },
      fontFamily: {
        sans: ['Inter', 'Segoe UI', 'sans-serif'],
        display: ['"Space Grotesk"', 'Inter', 'sans-serif'],
      },
    },
  },
  plugins: [],
};
