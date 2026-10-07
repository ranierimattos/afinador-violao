import { STRINGS, read, detectPitch } from "./tuner.js";

const IN_TUNE_CENTS = 5;
const $ = (id) => document.getElementById(id);

// Escala: um traço a cada 10 cents, de -50 a +50 (±60°).
for (let c = -50; c <= 50; c += 10) {
  const a = (c / 50) * 60;
  $("ticks").insertAdjacentHTML("beforeend",
    `<line class="tick${c ? "" : " zero"}" y1="${c ? -88 : -80}" y2="-100" transform="rotate(${a})" />`);
}
const strings = STRINGS.map((s) => $("strings").appendChild(Object.assign(document.createElement("span"), { textContent: s.label })));

function show(frequency) {
  const r = frequency && read(frequency);
  $("note").textContent = r ? r.note.label : "–";
  $("freq").textContent = r ? `${r.frequency.toFixed(1)} Hz · ${r.cents > 0 ? "+" : ""}${Math.round(r.cents)} cents` : "";
  strings.forEach((el, i) => el.classList.toggle("on", r?.string === i));

  const needle = $("needle");
  needle.setAttribute("visibility", r ? "visible" : "hidden");
  if (!r) return void ($("hint").textContent = "Toque uma corda solta");
  const off = Math.abs(r.cents);
  needle.style.transform = `rotate(${(Math.max(-50, Math.min(50, r.cents)) / 50) * 60}deg)`;
  needle.style.color = `var(--${off <= IN_TUNE_CENTS ? "green" : off <= 15 ? "yellow" : "red"})`;
  $("hint").textContent = off <= IN_TUNE_CENTS ? "Afinado!" : r.cents < 0 ? "Muito grave: aperte a corda" : "Muito agudo: afrouxe a corda";
}

async function start() {
  // O microfone só pode ser ligado depois de um toque (exigência do iPhone).
  let stream;
  try {
    stream = await navigator.mediaDevices.getUserMedia({
      audio: { echoCancellation: false, noiseSuppression: false, autoGainControl: false },
    });
  } catch {
    $("hint").textContent = "Permita o uso do microfone para afinar";
    return;
  }
  $("start").hidden = true;
  const ctx = new AudioContext();
  const analyser = Object.assign(ctx.createAnalyser(), { fftSize: 4096 });
  ctx.createMediaStreamSource(stream).connect(analyser);
  document.addEventListener("visibilitychange", () => document.hidden || ctx.resume());

  const samples = new Float32Array(analyser.fftSize);
  const recent = [];
  setInterval(() => {
    analyser.getFloatTimeDomainData(samples);
    const pitch = detectPitch(samples, ctx.sampleRate);
    if (pitch === null) recent.length = 0;
    else recent.push(pitch);
    if (recent.length > 5) recent.shift();
    // Mediana das últimas leituras: o ponteiro não treme.
    show([...recent].sort((a, b) => a - b)[recent.length >> 1]);
  }, 50);
  show(null);
}

$("start").onclick = start;
navigator.serviceWorker?.register("sw.js");
