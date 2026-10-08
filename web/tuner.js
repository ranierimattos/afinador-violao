// Lógica do afinador, sem nada de navegador: dá para testar com `node --test`.

const NAMES = ["C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"];

export function noteFromMidi(midi) {
  const name = NAMES[midi % 12];
  const octave = Math.floor(midi / 12) - 1;
  return { label: name + octave, frequency: 440 * 2 ** ((midi - 69) / 12) };
}

/** Afinações por instrumento: cordas soltas (em notas MIDI) da mais grave para a mais aguda. */
export const TUNINGS = {
  violao: [
    { name: "Padrão", midi: [40, 45, 50, 55, 59, 64] }, // E A D G B E
    { name: "Drop D", midi: [38, 45, 50, 55, 59, 64] }, // D A D G B E
    { name: "Meio tom abaixo", midi: [39, 44, 49, 54, 58, 63] },
    { name: "Open G", midi: [38, 43, 50, 55, 59, 62] }, // D G D G B D
    { name: "DADGAD", midi: [38, 45, 50, 55, 57, 62] },
  ],
  baixo: [
    { name: "Padrão", midi: [28, 33, 38, 43] }, // E A D G
    { name: "Drop D", midi: [26, 33, 38, 43] }, // D A D G
    { name: "Meio tom abaixo", midi: [27, 32, 37, 42] },
  ],
};

export const strings = (tuning) => tuning.midi.map(noteFromMidi);

/** Frequência → nota mais próxima, desvio em cents e índice da corda mais próxima. */
export function read(frequency, strings) {
  const note = noteFromMidi(Math.round(69 + 12 * Math.log2(frequency / 440)));
  const cents = 1200 * Math.log2(frequency / note.frequency);
  const dist = (s) => Math.abs(Math.log2(frequency / s.frequency));
  const string = strings.reduce((best, s, i) => (dist(s) < dist(strings[best]) ? i : best), 0);
  return { frequency, note, cents, string };
}

/**
 * Frequência fundamental pelo algoritmo YIN, ou null em silêncio / sem tom claro.
 * `samples` são amostras entre -1 e 1.
 */
export function detectPitch(samples, sampleRate, minFreq = 60, maxFreq = 1000) {
  const minLag = Math.max(2, Math.floor(sampleRate / maxFreq));
  const maxLag = Math.floor(sampleRate / minFreq);
  const n = samples.length - maxLag;
  if (n <= 0) return null;

  let sumSq = 0;
  for (const s of samples) sumSq += s * s;
  if (Math.sqrt(sumSq / samples.length) < 0.01) return null;

  // Diferença d(tau) e média cumulativa normalizada d'(tau).
  const cmnd = new Float64Array(maxLag + 1).fill(1);
  let running = 0;
  for (let tau = 1; tau <= maxLag; tau++) {
    let sum = 0;
    for (let i = 0; i < n; i++) {
      const d = samples[i] - samples[i + tau];
      sum += d * d;
    }
    running += sum;
    cmnd[tau] = running === 0 ? 1 : (sum * tau) / running;
  }

  // Primeiro mínimo abaixo do limiar.
  let tau = minLag;
  while (tau < maxLag && cmnd[tau] >= 0.15) tau++;
  if (tau >= maxLag) return null;
  while (tau + 1 < maxLag && cmnd[tau + 1] < cmnd[tau]) tau++;

  // Interpolação parabólica para precisão abaixo de uma amostra.
  const [s0, s1, s2] = [cmnd[tau - 1], cmnd[tau], cmnd[tau + 1]];
  const denom = 2 * (2 * s1 - s2 - s0);
  return sampleRate / (denom ? tau + (s2 - s0) / denom : tau);
}
