// Lógica do afinador, sem nada de navegador: dá para testar com `node --test`.

const NAMES = ["C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"];

export function noteFromMidi(midi) {
  const name = NAMES[midi % 12];
  const octave = Math.floor(midi / 12) - 1;
  return { label: name + octave, frequency: 440 * 2 ** ((midi - 69) / 12) };
}

/** Cordas soltas do violão, da 6ª (mais grave) para a 1ª: E2 A2 D3 G3 B3 E4. */
export const STRINGS = [40, 45, 50, 55, 59, 64].map(noteFromMidi);

/** Frequência → nota mais próxima, desvio em cents e índice da corda mais próxima. */
export function read(frequency) {
  const note = noteFromMidi(Math.round(69 + 12 * Math.log2(frequency / 440)));
  const cents = 1200 * Math.log2(frequency / note.frequency);
  const dist = (s) => Math.abs(Math.log2(frequency / s.frequency));
  const string = STRINGS.reduce((best, s, i) => (dist(s) < dist(STRINGS[best]) ? i : best), 0);
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
