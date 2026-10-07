import { test } from "node:test";
import assert from "node:assert/strict";
import { STRINGS, read, detectPitch } from "./tuner.js";

const RATE = 48000;

/** Corda dedilhada simplificada: fundamental + harmônicos que decaem. */
function pluck(freq, size = 4096) {
  const harmonics = [[1, 1], [2, 0.6], [3, 0.4], [4, 0.2], [5, 0.1]];
  return Float32Array.from({ length: size }, (_, i) => {
    const t = i / RATE;
    const sum = harmonics.reduce((acc, [h, a]) => acc + a * Math.sin(2 * Math.PI * freq * h * t), 0);
    return sum * 0.3 * Math.exp(-t * 2);
  });
}

const near = (actual, expected, tol, msg) =>
  assert.ok(Math.abs(actual - expected) <= tol, `${msg}: ${actual} ≠ ${expected}`);

test("detecta as 6 cordas soltas", () => {
  for (const s of STRINGS) near(detectPitch(pluck(s.frequency), RATE), s.frequency, 0.5, s.label);
});

test("detecta senoide desafinada", () => {
  const sine = Float32Array.from({ length: 4096 }, (_, i) => 0.5 * Math.sin((2 * Math.PI * 112 * i) / RATE));
  near(detectPitch(sine, RATE), 112, 0.5, "112 Hz");
});

test("silêncio e ruído dão null", () => {
  assert.equal(detectPitch(new Float32Array(4096), RATE), null);
  let seed = 1;
  const rand = () => ((seed = (seed * 16807) % 2147483647) / 2147483647) * 2 - 1;
  assert.equal(detectPitch(Float32Array.from({ length: 4096 }, () => rand() * 0.5), RATE), null);
});

test("cordas da afinação padrão", () => {
  assert.deepEqual(STRINGS.map((s) => s.label), ["E2", "A2", "D3", "G3", "B3", "E4"]);
  near(STRINGS[0].frequency, 82.41, 0.01, "E2");
});

test("nota, cents e corda mais próxima", () => {
  const a4 = read(440);
  assert.equal(a4.note.label, "A4");
  near(a4.cents, 0, 1e-9, "cents A4");

  const flat = read(110 * 2 ** (-20 / 1200));
  assert.equal(flat.note.label, "A2");
  near(flat.cents, -20, 1e-6, "cents A2");
  assert.equal(flat.string, 1);

  assert.equal(read(80).string, 0);
  assert.equal(read(340).string, 5);
});
