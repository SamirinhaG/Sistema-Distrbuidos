---
title: "Trabalho 2 – Remote Procedure Call / Remote Method Invocation"
subtitle: "Serviço remoto: Biblioteca com grupo de notificações (RMI)"
---

**Universidade Federal do Ceará – Campus de Quixadá**
**Disciplina:** QXD0043 – Sistemas Distribuídos · **Professor:** Antonio Rafael Braga
**Estudante(s):** _________________________ · **Repositório:** _________________________

# 1. Visão geral do serviço

O serviço remoto é o sistema de **Biblioteca** do Trabalho 1 (entidades `Livro`, `Usuario`, `Emprestimo`). A
**questão 4 do Trabalho 1** pedia comunicação multicast: o servidor enviava mensagens (`NOTIFICACAO`, `ALERTA`,
`ATUALIZACAO`) a todos os clientes que tinham entrado no grupo (`joinGroup`/`leaveGroup`), com autenticação por
TCP, vários clientes recebendo em tempo real e threads separadas para escuta e interação.

Neste trabalho a mesma funcionalidade foi **reimplementada com Java RMI** e organizada num **protocolo
requisição-resposta** (seção 5.2 do livro texto). Nenhum `Socket`, `ServerSocket`, `DatagramSocket` ou
`MulticastSocket` é criado pelo código: toda a comunicação passa pelo RMI.

Além do grupo de notificações, o serviço oferece as operações de biblioteca (cadastro de usuários e livros,
empréstimo e devolução). Mudanças relevantes geram notificações automáticas ao grupo (livro novo, último exemplar
emprestado, livro de volta à disponibilidade).

# 2. Arquitetura

```
 Cliente                                                     Servidor
 ───────                                                     ────────
 BibliotecaCliente ─► ProxyRemoto ─► ClienteRR.doOperation ──RMI──► TransporteImpl.enviar
   (métodos tipados)   (empacota JSON)   (monta Mensagem)               │  (fila de requisições)
                                                                        ▼
                                                           ServidorRR.getRequest ─► Despachante ─► XxxService
                                                                        ▲                              │
                                                       ServidorRR.sendReply ◄────── resultado (JSON) ──┘

 Notificações (sentido inverso, mesmo protocolo):
 GrupoNotificacao ─► ProxyRemoto ─► doOperation ──RMI──► ServidorRR do CLIENTE ─► CallbackCliente.receberNotificacao
```

O único objeto remoto RMI é `Transporte` (`byte[] enviar(byte[])`), publicado por todo nó que oferece objetos
remotos. Ele é apenas o **canal** por onde trafegam as mensagens empacotadas; o protocolo
requisição-resposta é implementado por cima dele. Tanto o servidor da biblioteca quanto cada cliente (para o seu
callback) usam a mesma classe `ServidorRR`, o que torna o protocolo simétrico.

## 2.1 Mensagens

Formato da mensagem (figura da seção 5.2), classe `rr.Mensagem`:

| Campo             | Tipo     | Função                                                                       |
|-------------------|----------|------------------------------------------------------------------------------|
| `messageType`     | int      | 0 = requisição (`REQUEST`), 1 = resposta (`REPLY`)                           |
| `requestId`       | long     | Identifica a chamada; a resposta devolve o mesmo valor (único por cliente)   |
| `objectReference` | String   | Nome do objeto remoto que presta o serviço (ex.: `LivroService`)             |
| `methodId`        | String   | Nome do método a invocar (ex.: `cadastrarLivro`)                             |
| `arguments`       | byte[]   | Argumentos (requisição) ou resultado (resposta), em JSON UTF-8               |

O envelope é empacotado em binário (`DataOutputStream`) e o conteúdo de `arguments` em JSON. A resposta leva em
`arguments` um objeto `{"sucesso": true, "dados": ..., "erro": null}`; em caso de falha de regra de negócio,
`sucesso=false` e `erro` traz a mensagem, que o cliente transforma em `ExcecaoRemota`.

## 2.2 Métodos do protocolo

| Método (livro)                                   | Onde está               | Observação                                                                                         |
|--------------------------------------------------|-------------------------|----------------------------------------------------------------------------------------------------|
| `byte[] doOperation(RemoteObjectRef o, String methodId, byte[] arguments)` | `rr.ClienteRR`  | `methodId` é `String` (permitido no enunciado). Monta a requisição, envia pelo RMI e devolve os `arguments` da resposta. |
| `byte[] getRequest()`                            | `rr.ServidorRR`         | Bloqueia até chegar uma requisição (fila alimentada pelo `Transporte`).                            |
| `void sendReply(byte[] reply, InetAddress clientHost, long requestId)` | `rr.ServidorRR` | Assinatura alterada: no lugar de `clientPort` (conceito de sockets) usa-se o `requestId`, que identifica a chamada RMI aguardando. |

O laço do servidor é `getRequest → Despachante.despachar → sendReply`; cada requisição é atendida em uma thread
de um pool, portanto o servidor é **multithread**.

`RemoteObjectRef` (host, porta do registro RMI, nome do objeto) é a referência remota usada no `doOperation`.

# 3. Entidades e relacionamentos

| Classe          | Papel                                                                                      |
|-----------------|--------------------------------------------------------------------------------------------|
| `Usuario`       | Classe abstrata base (id, nome, login, senha, e-mail)                                      |
| `Aluno`         | **extends** `Usuario` (matrícula, curso; limite 3 empréstimos, 14 dias)                    |
| `Professor`     | **extends** `Usuario` (departamento; limite 5 empréstimos, 30 dias)                        |
| `Bibliotecario` | **extends** `Usuario` (administrador: cadastra livros e envia notificações)                |
| `Autor`         | Autor de livros                                                                            |
| `Livro`         | Título, descrição, ano, exemplares; **agrega** um `Autor`                                  |
| `Emprestimo`    | **agrega** um `Usuario` e um `Livro`; datas e status                                       |
| `Notificacao`   | `tipo` (`NOTIFICACAO`/`ALERTA`/`ATUALIZACAO`), `mensagem`, `timestamp` (formato da questão 4) |
| `Sessao`        | Token de login + usuário autenticado                                                       |

# 4. Requisitos do enunciado × implementação

| Requisito                                                | Atendimento                                                                                                  |
|----------------------------------------------------------|--------------------------------------------------------------------------------------------------------------|
| RPC/RMI, **sem sockets**                                 | Java RMI (`Transporte`); nenhum socket criado no código                                                      |
| Protocolo requisição-resposta com os 3 métodos           | `doOperation`, `getRequest`, `sendReply` (seção 2.2)                                                         |
| Mensagens empacotadas como na figura                     | `rr.Mensagem` com os 5 campos; `objectReference` e `methodId` como `String`                                  |
| Mínimo 4 classes de entidade                             | `Usuario`, `Autor`, `Livro`, `Emprestimo`, `Notificacao` (+ 3 subclasses e `Sessao`)                        |
| Mínimo 2 agregações ("tem-um")                           | `Livro`→`Autor`; `Emprestimo`→`Usuario`; `Emprestimo`→`Livro`                                                |
| Mínimo 2 extensões ("é-um")                              | `Aluno`, `Professor` e `Bibliotecario` estendem `Usuario`                                                    |
| Mínimo 4 métodos de invocação remota                     | 10 métodos no servidor (seção 5) + 1 no cliente (`receberNotificacao`)                                       |
| Passagem **por referência** de objetos remotos           | `RemoteObjectRef` do `CallbackCliente` enviado em `entrarNoGrupo`; o servidor invoca o objeto remoto do cliente |
| Passagem **por valor** de objetos locais no servidor     | `Livro`, `Usuario`, `Notificacao` chegam como cópias desempacotadas do JSON; o servidor guarda e altera só as suas cópias |
| Representação externa de dados                           | JSON (Gson), com adaptador que preserva a subclasse de `Usuario` (campo `tipo`)                              |
| Sem GUI                                                  | Cliente em modo texto                                                                                        |

# 5. Métodos remotos

**`UsuarioService`**

- `Usuario cadastrarUsuario(Usuario)` – cadastro de aluno ou professor (por valor; a senha é guardada como hash SHA-256).
- `Sessao autenticar(String login, String senha)` – substitui a autenticação TCP da questão 4; devolve o token de sessão.

**`LivroService`**

- `Livro cadastrarLivro(String token, Livro)` – só bibliotecário; notifica o grupo (`NOTIFICACAO`).
- `List<Livro> listarLivros()`
- `Livro buscarLivro(int idLivro)`

**`EmprestimoService`**

- `Emprestimo emprestarLivro(String token, int idLivro)` – valida limite do perfil e disponibilidade; se acabarem os exemplares, envia `ALERTA`.
- `Emprestimo devolverLivro(String token, int idEmprestimo)` – se o livro volta a ficar disponível, envia `ATUALIZACAO`.
- `List<Emprestimo> listarMeusEmprestimos(String token)`

**`NotificacaoService`** (equivalente ao grupo multicast)

- `int entrarNoGrupo(String token, RemoteObjectRef callback)` – `joinGroup`; o `callback` é passado **por referência**.
- `boolean sairDoGrupo(String token)` – `leaveGroup`.
- `int enviarNotificacao(String token, Notificacao)` – só bibliotecário; envia a todos os membros.

**`CallbackCliente`** (objeto remoto publicado por cada cliente)

- `void receberNotificacao(Notificacao)` – chamado pelo servidor para cada membro do grupo.

# 6. Do multicast (Trabalho 1) ao RMI (Trabalho 2)

| Questão 4 – Trabalho 1                         | Trabalho 2                                                                                         |
|------------------------------------------------|----------------------------------------------------------------------------------------------------|
| Endereço classe D + porta fixa                 | Registro RMI do servidor (host/porta); grupo mantido em `GrupoNotificacao`                         |
| `joinGroup` / `leaveGroup`                     | `entrarNoGrupo` / `sairDoGrupo` (o cliente entrega a referência do seu callback)                   |
| Servidor envia para todos os clientes          | `GrupoNotificacao.enviar` invoca `receberNotificacao` em cada membro, **em paralelo** (uma tarefa por membro) |
| Autenticação por socket TCP                    | `UsuarioService.autenticar` + token de sessão                                                      |
| Thread de escuta + thread de interação         | Thread do `ServidorRR` do cliente recebe e exibe notificações; thread principal cuida do menu      |
| Mensagem `{tipo, mensagem, timestamp}`         | Entidade `Notificacao`, serializada em JSON                                                        |
| Servidor com envio concorrente                 | Pool de threads no envio e no atendimento das requisições                                          |

Membros inacessíveis (cliente que caiu) são removidos do grupo automaticamente na primeira falha de entrega.

# 7. Passagem de parâmetros

- **Por valor (objetos locais do servidor):** os argumentos são serializados em JSON no cliente e desempacotados no
  servidor, que passa a trabalhar com uma cópia. Ex.: o `Livro` enviado em `cadastrarLivro` é copiado; o servidor atribui
  o id e o guarda; o cliente recebe outra cópia no retorno. O servidor nunca devolve a senha nem expõe os objetos internos.
- **Por referência (objetos remotos):** o cliente publica `CallbackCliente` no seu próprio nó e envia o
  `RemoteObjectRef` (host, porta, nome) em `entrarNoGrupo`. O servidor guarda **a referência**, não uma cópia, e
  executa `receberNotificacao` no objeto que continua vivendo no cliente.

# 8. Testes e demonstração

Foi executado um cenário com um servidor e três clientes simultâneos:

1. `joao` (professor) e `maria` (aluna) entram no grupo (`joinGroup`); o servidor registra os dois membros.
2. `admin` tenta login com senha errada (recusado) e depois entra; cadastra o livro *Redes de Computadores* →
   `maria` e `joao` recebem, em tempo real e sem interagir, a `NOTIFICACAO` do novo livro.
3. `admin` envia um `ALERTA` manual → recebido pelos dois clientes.
4. `admin` tenta emprestar um livro → recusado (bibliotecário não realiza empréstimos).
5. `maria` empresta o único exemplar de *Dom Casmurro* → `ALERTA` automático para o grupo; ao devolver →
   `ATUALIZACAO` automática.
6. `maria` executa `leaveGroup` e deixa de receber; o servidor atualiza o número de membros.

Roteiro para reproduzir: seção "Roteiro de demonstração" do `README.md`.

# 9. Limitações

- Os dados ficam em memória (reiniciar o servidor zera o acervo).
- A comunicação não usa TLS: a senha trafega em claro no cadastro/login (o servidor guarda apenas o hash).
- Em máquinas distintas é preciso informar o IP real de cada nó (parâmetro `enderecoPublico`/`meuEndereco`) e liberar
  as portas do registro RMI (1099 no servidor; a partir de 11000 nos clientes).
- A semântica de invocação é a do RMI (no máximo uma vez); não há reenvio de notificações a membros
  que estavam fora do ar.
