import { test } from "node:test";
import assert from "node:assert/strict";
import { TUNINGS, strings, read, detectPitch } from "./tuner.js";

const GUITAR = strings(TUNINGS.violao[0]);
const BASS = strings(TUNINGS.baixo[0]);

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

test("detecta as cordas soltas de todas as afinações (violão e baixo)", () => {
  for (const [instrument, list] of Object.entries(TUNINGS))
    for (const tuning of list)
      for (const s of strings(tuning))
        near(detectPitch(pluck(s.frequency), RATE, s.frequency * 0.7), s.frequency, 0.5, `${instrument} ${tuning.name} ${s.label}`);
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

test("afinações", () => {
  assert.deepEqual(GUITAR.map((s) => s.label), ["E2", "A2", "D3", "G3", "B3", "E4"]);
  assert.deepEqual(strings(TUNINGS.violao[1]).map((s) => s.label), ["D2", "A2", "D3", "G3", "B3", "E4"]);
  assert.deepEqual(BASS.map((s) => s.label), ["E1", "A1", "D2", "G2"]);
  near(GUITAR[0].frequency, 82.41, 0.01, "E2");
  near(BASS[0].frequency, 41.2, 0.01, "E1");
});

test("nota, cents e corda mais próxima", () => {
  const a4 = read(440, GUITAR);
  assert.equal(a4.note.label, "A4");
  near(a4.cents, 0, 1e-9, "cents A4");

  const flat = read(110 * 2 ** (-20 / 1200), GUITAR);
  assert.equal(flat.note.label, "A2");
  near(flat.cents, -20, 1e-6, "cents A2");
  assert.equal(flat.string, 1);

  assert.equal(read(80, GUITAR).string, 0);
  assert.equal(read(340, GUITAR).string, 5);
  assert.equal(read(73, strings(TUNINGS.violao[1])).string, 0); // Drop D: corda 6 em D2
  assert.equal(read(98, BASS).string, 3);
});
