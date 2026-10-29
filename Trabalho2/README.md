7# Trabalho 2 – RMI/RPC (QXD0043 – Sistemas Distribuídos)

Reimplementação da **questão 4 do Trabalho 1 (comunicação multicast)** sobre o serviço remoto
**Biblioteca**, usando **Java RMI** e um **protocolo requisição-resposta** (seção 5.2 do livro texto).
Nenhum socket é criado pelo código da aplicação. Veja o relatório completo em [`RELATORIO.md`](RELATORIO.md).

## Estrutura

```
src/main/java
├── entidades/   Usuario (abstrata) ← Aluno, Professor, Bibliotecario · Autor · Livro · Emprestimo · Notificacao · Sessao
├── rr/          Protocolo requisição-resposta: Mensagem, ClienteRR (doOperation), ServidorRR (getRequest/sendReply),
│                Despachante, ProxyRemoto, RemoteObjectRef, Transporte/TransporteImpl (canal RMI)
├── servidor/    Biblioteca (estado), UsuarioService, LivroService, EmprestimoService, NotificacaoService,
│                GrupoNotificacao (equivalente RMI do grupo multicast), ServidorMain
├── cliente/     BibliotecaCliente (fachada), CallbackCliente (objeto remoto do cliente), ClienteMain (modo texto)
└── util/        Json + UsuarioAdapter (representação externa de dados em JSON, com Gson)
```

## Como executar

Requisitos: JDK 17+ e Gson (já em `lib/gson.jar`; com Maven, `pom.xml` baixa a dependência).

```bash
# compilar
mkdir -p out
javac -encoding UTF-8 -cp lib/gson.jar -d out $(find src -name '*.java')

# terminal 1 – servidor (porta 1099)
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp out:lib/gson.jar servidor.ServidorMain 1099

# terminais 2, 3, 4... – clientes (portas de callback diferentes por máquina/terminal, ou deixe a automática)
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp out:lib/gson.jar cliente.ClienteMain 127.0.0.1 1099 127.0.0.1 11000
```

No Windows, troque `out:lib/gson.jar` por `out;lib/gson.jar`.

Argumentos do cliente: `[hostServidor] [portaServidor] [meuEndereco] [portaCallbackInicial]`.
Argumentos do servidor: `[porta] [enderecoPublico]`. Para rodar em máquinas diferentes, informe o IP real
de cada máquina em `enderecoPublico` / `meuEndereco` (o RMI usa esse endereço nos stubs).

Usuários de teste: `admin/admin123` (bibliotecário), `maria/123` (aluna), `joao/123` (professor).

## Roteiro de demonstração

1. Suba o servidor.
2. Abra dois clientes (`maria` e `joao`) → opção **5** (joinGroup) em ambos.
3. Abra um cliente `admin` → opção **7** (cadastrar livro) ou **8** (enviar notificação): `maria` e `joao`
   recebem a mensagem na hora, sem digitar nada.
4. Em `maria`, empreste o livro 3 (*Dom Casmurro*, 1 exemplar) → todos recebem um **ALERTA**; devolva → **ATUALIZACAO**.
5. Opção **6** (leaveGroup) em um cliente → ele deixa de receber.
