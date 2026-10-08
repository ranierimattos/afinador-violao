// Guarda os arquivos no aparelho para o afinador abrir sem internet.
// Responde com a cópia guardada e atualiza em segundo plano.
const FILES = ["./", "index.html", "app.js", "tuner.js", "manifest.webmanifest", "icon.svg", "icon-180.png", "icon-512.png"];

self.addEventListener("install", (e) => e.waitUntil(caches.open("afinador").then((c) => c.addAll(FILES))));

self.addEventListener("fetch", (e) => {
  // Só os arquivos do próprio site; o contador (outro endereço) vai sempre direto à internet.
  if (e.request.method !== "GET" || new URL(e.request.url).origin !== location.origin) return;
  e.respondWith(caches.open("afinador").then(async (cache) => {
    const cached = await cache.match(e.request);
    const fresh = fetch(e.request)
      .then((res) => (res.ok && cache.put(e.request, res.clone()), res))
      .catch(() => cached);
    return cached || fresh;
  }));
});
