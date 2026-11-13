/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      boxShadow: {
        glow: '0 0 30px rgba(16, 185, 129, 0.35)',
      },
      colors: {
        obs: {
          950: '#020a07',
          900: '#071410',
          800: '#0d1d1b',
          700: '#12312a',
          600: '#0f7a5d',
          500: '#10b981',
          400: '#34d399',
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
