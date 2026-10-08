# Afinador de violão e baixo 🎸

Um afinador **simples**: abra, toque uma corda e veja se está afinada.
Sem anúncios, sem cadastro. Só o microfone.

Funciona no **Android**, no **iPhone** e no **Windows** (e também no Mac e no Linux).

- Mostra a nota que você tocou, em letras grandes.
- O ponteiro fica **verde** quando a corda está afinada.
- Diz se é para **apertar** ou **afrouxar** a corda.
- **Violão** ou **baixo**, com várias afinações:

| Instrumento | Afinações |
| ----------- | --------- |
| Violão | Padrão (E A D G B E), Drop D, Meio tom abaixo, Open G, DADGAD |
| Baixo (4 cordas) | Padrão (E A D G), Drop D, Meio tom abaixo |

- No rodapé, um contador mostra quantas visitas e quantas cordas afinadas o afinador já
  teve, somando todo mundo.

## Instalar

### iPhone e iPad

1. Abra **[ranierimattos.github.io/afinador-violao](https://ranierimattos.github.io/afinador-violao/)**
   no **Safari**.
2. Toque no botão **Compartilhar** (o quadrado com a seta para cima) e depois em
   **Adicionar à Tela de Início**.
3. Abra o **Afinador** pelo ícone novo, toque em **Começar** e permita o microfone.

Depois da primeira vez, ele abre até sem internet.

### Windows, Mac e Linux

1. Abra **[ranierimattos.github.io/afinador-violao](https://ranierimattos.github.io/afinador-violao/)**
   no **Edge** ou no **Chrome**.
2. Para ter um ícone próprio, clique no ícone de instalar na barra de endereço (um
   monitor com uma seta) ou vá em **menu ⋯ → Aplicativos → Instalar este site como
   aplicativo**.
3. Clique em **Começar** e permita o microfone.

Use o microfone do notebook ou da webcam, perto do violão.

### Android

Funciona em Android 8.0 ou mais novo. (Também dá para usar a versão web acima.)

1. No celular, toque neste link para baixar o app:
   **[afinador.apk](https://github.com/ranierimattos/afinador-violao/releases/download/afinador/afinador.apk)**
2. Abra o arquivo baixado (toque na notificação do download ou procure em
   **Arquivos → Downloads**).
3. O Android vai avisar que não instala apps desta fonte. Toque em **Configurações**,
   ative **Permitir desta fonte** e volte.
4. Toque em **Instalar**. Se o Play Protect avisar que o app é desconhecido, toque em
   **Mais detalhes → Instalar assim mesmo** (o aviso aparece porque o app não veio da
   Play Store).
5. Abra o **Afinador** e permita o uso do microfone.

Depois de instalar, você pode desligar de novo a permissão do passo 3 em
**Configurações → Apps → Acesso especial → Instalar apps desconhecidos**.

> Se o Android disser que o app "não foi instalado" ao atualizar, desinstale a versão
> antiga e instale de novo.

## Como usar

1. No alto da tela, escolha **Violão** ou **Baixo** e a afinação (por exemplo, **Drop D**).
   O app lembra a sua escolha.
2. Na versão web, toque em **Começar**.
3. Toque uma corda **solta** (sem apertar nenhuma casa) perto do aparelho.

| Na tela                         | O que fazer                  |
| ------------------------------- | ---------------------------- |
| Ponteiro à esquerda / "aperte"  | A corda está grave: aperte a tarraxa |
| Ponteiro à direita / "afrouxe"  | A corda está aguda: afrouxe a tarraxa |
| Ponteiro verde / "Afinado!"     | Pronto, vá para a próxima corda |

Embaixo aparecem as cordas da afinação escolhida; a que você está tocando fica destacada.

### Privacidade

O som do microfone é analisado só no seu aparelho e nunca sai dele. A única coisa enviada
pela internet é um aviso anônimo para o contador (uma visita, uma corda afinada) ao serviço
gratuito [Abacus](https://abacus.jasoncameron.dev). Sem internet, o afinador funciona
normalmente e o contador apenas não aparece.

## Para quem quer mexer no código

O projeto é pequeno de propósito e não usa nenhuma biblioteca externa. São duas versões
com a mesma lógica:

- **`web/`**: a versão para iPhone, Windows e navegadores. HTML e JavaScript puros.
- **`app/`**: o app nativo de Android, em Kotlin.

### Versão web (`web/`)

| Arquivo | O que faz |
| ------- | --------- |
| `index.html` | A tela e o visual |
| `app.js` | Liga o microfone, atualiza a tela e o contador |
| `tuner.js` | Afinações, frequência do som (YIN), nota e cents |
| `sw.js` | Guarda os arquivos no aparelho para abrir sem internet |
| `manifest.webmanifest` | Nome e ícone quando instalado na tela de início |

Para testar no computador, dentro da pasta `web/` rode `python3 -m http.server` e abra
http://localhost:8000. Os testes rodam com `node --test web/tuner.test.js`.

**Quer uma afinação nova?** Acrescente uma linha na lista `TUNINGS` em `web/tuner.js` e
outra em `Tuning.kt` (no app Android). As cordas são escritas como notas MIDI: 40 = E2
(Mi grave do violão), e cada número a mais sobe um semitom.

### App Android (`app/`)

| Arquivo | O que faz |
| ------- | --------- |
| `MainActivity.kt` | A tela: mostra a nota, o ponteiro e as cordas |
| `AudioInput.kt` | Lê o microfone e envia a frequência para a tela |
| `PitchDetector.kt` | Descobre a frequência do som (algoritmo [YIN](https://pt.wikipedia.org/wiki/Algoritmo_YIN)) |
| `Tuning.kt` | Afinações; converte a frequência em nota e mede o quanto está desafinada |
| `Counter.kt` | Contador anônimo de visitas e cordas afinadas |
| `TunerView.kt` | Desenha o ponteiro |

Ficam em `app/src/main/java/com/ranierimattos/afinador/`. Os textos da tela estão em
`app/src/main/res/values/strings.xml`.

#### Compilar no seu computador

1. Instale o [Android Studio](https://developer.android.com/studio) (é gratuito).
2. Baixe este projeto: botão verde **Code → Download ZIP** aqui no GitHub, e descompacte.
3. No Android Studio, use **File → Open** e escolha a pasta do projeto.
4. Ligue o celular no cabo USB (com a
   [depuração USB](https://developer.android.com/studio/debug/dev-options) ativada) e
   aperte o botão ▶ **Run**.

Pelo terminal também funciona: `./gradlew assembleRelease` gera o APK em
`app/build/outputs/apk/release/`, e `./gradlew testDebugUnitTest` roda os testes.

### Contribuir

Achou um problema ou tem uma ideia? Abra uma
[issue](https://github.com/ranierimattos/afinador-violao/issues) contando o que
aconteceu.

Para enviar uma mudança no código:

1. Clique em **Fork** (canto superior direito) para criar sua cópia do projeto.
2. Faça a mudança na sua cópia (dá até para editar pelo próprio site, no ícone de lápis).
3. Clique em **Contribute → Open pull request** e explique o que mudou.

A cada mudança, o GitHub roda os testes sozinho. Quando a mudança entra na `main`, o
site e o `afinador.apk` são atualizados automaticamente.

## Licença

[MIT](LICENSE): use, copie e modifique à vontade, mantendo o crédito.

---

Desejado por Ranieri e realizado pelo Claude Code em 32min. Thx Anthropic
