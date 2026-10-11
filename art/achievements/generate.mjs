// Draws the 13 achievement icons as SVG. Render to PNG with render.mjs (scratch tool, Playwright).
import fs from 'fs';
const here = new URL('.', import.meta.url).pathname;

// Medal color per tier: bronze (easy), silver, gold (hard), purple (Dipole master).
const tiers = {
  bronze: ['#E9A86A', '#A8622E', '#6E3A16'],
  silver: ['#F1F3F5', '#A9B2BB', '#59636D'],
  gold: ['#FFE08A', '#E3A21A', '#8A5A00'],
  purple: ['#D9B8FF', '#8A4FD6', '#4A2080'],
};
const ink = '#2B1A0E', light = '#F5F0E6', dark = '#26211E', amber = '#FFB02E';
const piece = (cx, cy, r, fill) => `<ellipse cx="${cx}" cy="${cy + r * 0.18}" rx="${r}" ry="${r * 0.55}" fill="${dark}"/><ellipse cx="${cx}" cy="${cy}" rx="${r}" ry="${r * 0.55}" fill="${fill}" stroke="${dark}" stroke-width="5"/>`;
const stack = (cx, cy, r, n, fill) => Array.from({ length: n }, (_, i) => piece(cx, cy - i * r * 0.32, r, fill)).join('');
const s = `stroke="${ink}" stroke-width="10" stroke-linecap="round" stroke-linejoin="round"`;

const glyphs = {
  first_steps: ['bronze', `<g fill="${ink}"><ellipse cx="216" cy="300" rx="34" ry="52" transform="rotate(-12 216 300)"/><ellipse cx="200" cy="372" rx="22" ry="20"/><ellipse cx="296" cy="200" rx="34" ry="52" transform="rotate(12 296 200)"/><ellipse cx="312" cy="272" rx="22" ry="20"/></g>`],
  first_victory: ['bronze', `<path d="M186 160h140v60a70 70 0 0 1-140 0z" fill="${amber}" ${s}/><path d="M186 180h-34a36 36 0 0 0 40 50M326 180h34a36 36 0 0 1-40 50" fill="none" ${s}/><path d="M256 290v40M206 360h100v-30h-100z" fill="${amber}" ${s}/>`],
  friendly_match: ['bronze', stack(196, 300, 58, 1, light) + stack(316, 300, 58, 1, dark)],
  rising_player: ['silver', `<path d="M170 350l60-60 40 40 80-90" fill="none" ${s} stroke-width="16"/><path d="M300 236h56v56" fill="none" ${s} stroke-width="16"/>`],
  grandmaster: ['gold', `<path d="M160 340l-16-150 66 60 46-90 46 90 66-60-16 150z" fill="${amber}" ${s}/><path d="M164 372h184" ${s}/>`],
  both_sides: ['silver', `<circle cx="256" cy="256" r="100" fill="${dark}" stroke="${dark}" stroke-width="8"/><path d="M256 156a100 100 0 0 0 0 200z" fill="${light}"/><circle cx="256" cy="256" r="100" fill="none" stroke="${dark}" stroke-width="8"/>`],
  flawless: ['silver', `<path d="M196 170h120l50 60-110 130-110-130z" fill="#9FE3F5" ${s}/><path d="M146 230h220M216 170l-20 60 60 130 60-130-20-60" fill="none" ${s} stroke-width="6"/>`],
  no_help_needed: ['gold', `<path d="M256 150a70 70 0 0 0-40 128v30h80v-30a70 70 0 0 0-40-128z" fill="${light}" ${s}/><path d="M224 340h64" ${s}/><path d="M166 166l180 180" ${s} stroke="#C0392B" stroke-width="18"/>`],
  big_capture: ['silver', stack(256, 350, 70, 6, light)],
  swift_victory: ['silver', `<path d="M280 140l-100 140h70l-30 100 110-150h-74z" fill="${amber}" ${s}/>`],
  regular: ['silver', `<text x="256" y="300" text-anchor="middle" font-family="Fredoka, sans-serif" font-weight="700" font-size="150" fill="${ink}">10</text>`],
  dipole_master: ['purple', `<path d="M256 140l30 62 68 8-50 46 14 68-62-34-62 34 14-68-50-46 68-8z" fill="${amber}" ${s}/><text x="256" y="392" text-anchor="middle" font-family="Fredoka, sans-serif" font-weight="700" font-size="64" fill="${ink}">50</text>`],
  ai_impostor: ['gold', `<path d="M256 150v-20" ${s}/><circle cx="256" cy="124" r="12" fill="${amber}" ${s} stroke-width="6"/><rect x="166" y="160" width="180" height="150" rx="34" fill="#B8C4CE" ${s}/><path d="M184 214h144v34h-144z" fill="${ink}"/><path d="M206 280q50 -24 100 0q-50 14 -100 0z" fill="${ink}"/><path d="M200 330h112v40h-112z" fill="#B8C4CE" ${s}/>`],
};

// Light/dark board squares behind a medal, matching the app icon.
const board = Array.from({ length: 64 }, (_, i) => (i % 8 + (i >> 3)) % 2 ? `<rect x="${(i % 8) * 64}" y="${(i >> 3) * 64}" width="64" height="64"/>` : '').join('');
fs.mkdirSync(here + 'svg', { recursive: true });
for (const [key, [tier, art]] of Object.entries(glyphs)) {
  const [hi, mid, lo] = tiers[tier];
  fs.writeFileSync(`${here}svg/${key}.svg`, `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512">
<defs><radialGradient id="m" cx="0.4" cy="0.35" r="0.75"><stop offset="0" stop-color="${hi}"/><stop offset="1" stop-color="${mid}"/></radialGradient></defs>
<rect width="512" height="512" fill="#EBD3A8"/><g fill="#7A4E2D">${board}</g>
<circle cx="256" cy="262" r="210" fill="${lo}" opacity="0.5"/>
<circle cx="256" cy="256" r="210" fill="${lo}"/><circle cx="256" cy="256" r="192" fill="url(#m)"/>
<circle cx="256" cy="256" r="172" fill="none" stroke="${lo}" stroke-width="4" opacity="0.6"/>
${art}
</svg>`);
}
