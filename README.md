# Afinador de violão 🎸

Um afinador para Android **simples**: abra, toque uma corda e veja se está afinada.
Sem anúncios, sem cadastro, sem internet. Só o microfone.

- Mostra a nota que você tocou, em letras grandes.
- O ponteiro fica **verde** quando a corda está afinada.
- Diz se é para **apertar** ou **afrouxar** a corda.
- Afinação padrão: E A D G B E (Mi Lá Ré Sol Si Mi).

Funciona em Android 8.0 ou mais novo.

## Instalar no celular

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

Toque uma corda **solta** (sem apertar nenhuma casa) perto do celular.

| Na tela                         | O que fazer                  |
| ------------------------------- | ---------------------------- |
| Ponteiro à esquerda / "aperte"  | A corda está grave: aperte a tarraxa |
| Ponteiro à direita / "afrouxe"  | A corda está aguda: afrouxe a tarraxa |
| Ponteiro verde / "Afinado!"     | Pronto, vá para a próxima corda |

Embaixo aparecem as 6 cordas; a que você está tocando fica destacada.

## Para quem quer mexer no código

O app é pequeno de propósito: 5 arquivos Kotlin e nenhuma biblioteca externa.

| Arquivo | O que faz |
| ------- | --------- |
| `MainActivity.kt` | A tela: mostra a nota, o ponteiro e as cordas |
| `AudioInput.kt` | Lê o microfone e envia a frequência para a tela |
| `PitchDetector.kt` | Descobre a frequência do som (algoritmo [YIN](https://pt.wikipedia.org/wiki/Algoritmo_YIN)) |
| `Tuning.kt` | Converte a frequência em nota e mede o quanto está desafinada |
| `TunerView.kt` | Desenha o ponteiro |

Ficam em `app/src/main/java/com/ranierimattos/afinador/`. Os textos da tela estão em
`app/src/main/res/values/strings.xml`.

### Compilar no seu computador

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

A cada mudança, o GitHub compila o app e roda os testes sozinho. Quando a mudança entra
na `main`, o `afinador.apk` do link acima é atualizado automaticamente.

## Licença

[MIT](LICENSE): use, copie e modifique à vontade, mantendo o crédito.

---

Desejado por Ranieri e realizado pelo Claude Code em 32min. Thx Anthropic
