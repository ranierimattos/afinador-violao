import { TUNINGS, strings, read, detectPitch } from "./tuner.js";

const IN_TUNE_CENTS = 5;
const COUNTER = "https://abacus.jasoncameron.dev";
const $ = (id) => document.getElementById(id);

// Escala: um traço a cada 10 cents, de -50 a +50 (±60°).
for (let c = -50; c <= 50; c += 10) {
  $("ticks").insertAdjacentHTML("beforeend",
    `<line class="tick${c ? "" : " zero"}" y1="${c ? -88 : -80}" y2="-100" transform="rotate(${(c / 50) * 60})" />`);
}

// Instrumento e afinação escolhidos (lembrados entre visitas).
let saved = {};
try { saved = JSON.parse(localStorage.afinador || "{}"); } catch {}
let instrument = TUNINGS[saved.instrument] ? saved.instrument : "violao";
let tuning, notes, stringEls, lastCounted, inTuneFrames;

function setTuning(index) {
  tuning = TUNINGS[instrument][index] || TUNINGS[instrument][0];
  notes = strings(tuning);
  $("strings").replaceChildren(...notes.map((n) => Object.assign(document.createElement("span"), { textContent: n.label })));
  stringEls = [...$("strings").children];
  lastCounted = null;
  inTuneFrames = 0;
  try { localStorage.afinador = JSON.stringify({ instrument, tuning: index }); } catch {}
}

function setInstrument(name, index = 0) {
  instrument = name;
  document.querySelectorAll("#instrument button").forEach((b) => b.classList.toggle("on", b.value === name));
  $("tuning").replaceChildren(...TUNINGS[name].map((t, i) => new Option(t.name, i)));
  $("tuning").value = index;
  setTuning(index);
}

document.querySelectorAll("#instrument button").forEach((b) => (b.onclick = () => setInstrument(b.value)));
$("tuning").onchange = (e) => setTuning(+e.target.value);
setInstrument(instrument, saved.tuning || 0);

// Contador global (anônimo). Se o serviço não responder, o contador fica escondido.
const stats = {};
async function count(key, hit) {
  try {
    const res = await fetch(`${COUNTER}/${hit ? "hit" : "get"}/afinador-violao/${key}`);
    stats[key] = (await res.json()).value ?? stats[key];
  } catch {}
  if (stats.visitas == null) return;
  $("stats").textContent = `${stats.visitas.toLocaleString("pt-BR")} visitas · ${(stats.afinacoes ?? 0).toLocaleString("pt-BR")} cordas afinadas`;
}
count("visitas", true).then(() => count("afinacoes", false));

function show(frequency) {
  const r = frequency && read(frequency, notes);
  $("note").textContent = r ? r.note.label : "–";
  const cents = r && Math.round(r.cents);
  $("freq").textContent = r ? `${r.frequency.toFixed(1)} Hz · ${cents > 0 ? "+" : ""}${cents} cents` : "";
  stringEls.forEach((el, i) => el.classList.toggle("on", r?.string === i));

  const needle = $("needle");
  needle.setAttribute("visibility", r ? "visible" : "hidden");
  if (!r) return void ($("hint").textContent = "Toque uma corda solta");
  const off = Math.abs(r.cents);
  const inTune = off <= IN_TUNE_CENTS && r.note.label === notes[r.string].label;
  needle.style.transform = `rotate(${(Math.max(-50, Math.min(50, r.cents)) / 50) * 60}deg)`;
  needle.style.color = `var(--${off <= IN_TUNE_CENTS ? "green" : off <= 15 ? "yellow" : "red"})`;
  $("hint").textContent = off <= IN_TUNE_CENTS ? "Afinado!" : r.cents < 0 ? "Muito grave: aperte a corda" : "Muito agudo: afrouxe a corda";

  // Conta uma corda afinada quando ela fica no tom por ~0,5 s (uma vez por corda).
  inTuneFrames = inTune ? inTuneFrames + 1 : 0;
  if (inTuneFrames === 10 && lastCounted !== r.string) {
    lastCounted = r.string;
    count("afinacoes", true);
  }
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
    // Procura só um pouco abaixo da corda mais grave: evita confundir ruído com nota.
    const pitch = detectPitch(samples, ctx.sampleRate, notes[0].frequency * 0.7);
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
