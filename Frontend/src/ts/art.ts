// Ilustraciones vectoriales propias; no requieren recursos de terceros.
const shell = (content: string) =>
  '<svg viewBox="0 0 180 180" fill="none" aria-hidden="true">' +
  content +
  "</svg>";
const icons = [
  shell(
    '<path d="M43 111V78a47 47 0 0194 0v33" stroke="#526c58" stroke-width="15"/><rect x="30" y="89" width="30" height="55" rx="13" fill="#d7ddc9" stroke="#617563" stroke-width="5"/><rect x="120" y="89" width="30" height="55" rx="13" fill="#d7ddc9" stroke="#617563" stroke-width="5"/>',
  ),
  shell(
    '<g transform="rotate(-12 90 90)"><rect x="18" y="49" width="144" height="84" rx="10" fill="#596a5d"/><rect x="24" y="54" width="132" height="72" rx="7" fill="#abb6a2"/><path d="M34 68h112M34 83h112M34 98h112" stroke="#e6e8d9" stroke-width="9" stroke-dasharray="10 5"/><path d="M58 113h65" stroke="#dce3cc" stroke-width="8"/></g>',
  ),
  shell(
    '<ellipse cx="87" cy="148" rx="42" ry="10" fill="#667d63"/><path d="M87 144V88l31-39" stroke="#4d6555" stroke-width="9"/><path d="M83 61l31-30 40 39z" fill="#d5bd87" stroke="#a18f69" stroke-width="4"/><path d="M117 67l20 16" stroke="#faedb9" stroke-width="6"/>',
  ),
  shell(
    '<path d="M69 51v-8a21 21 0 0142 0v8" stroke="#48594c" stroke-width="7"/><rect x="44" y="45" width="92" height="112" rx="29" fill="#647c65"/><path d="M50 76h80" stroke="#aab99b" stroke-width="3"/><rect x="60" y="100" width="60" height="43" rx="11" fill="#92a383"/><path d="M78 61h24" stroke="#c5d1b2" stroke-width="3"/><path d="M136 65v58M44 65v58" stroke="#455c4a" stroke-width="6"/>',
  ),
  shell(
    '<g transform="rotate(-18 90 90)"><rect x="52" y="24" width="77" height="130" rx="38" fill="#d8dacb" stroke="#9ba990" stroke-width="3"/><path d="M90 27v45M55 76h70" stroke="#9ba990" stroke-width="2"/><rect x="86" y="42" width="9" height="20" rx="4" fill="#586e58"/></g>',
  ),
  shell(
    '<path d="M26 70l66-33 62 30-64 36z" fill="#d5bd93"/><path d="M26 70v63l64 28v-58z" fill="#ad936f"/><path d="M90 103l64-36v62l-64 32z" fill="#c4a77b"/><path d="M26 94l64 29 64-35M90 103v58M57 54l63 32v61" stroke="#eddbc0" stroke-width="3"/>',
  ),
];
export function art(id: number): string {
  return icons[(id - 1) % icons.length] || icons[0];
}
