package cliente;

import entidades.Aluno;
import entidades.Autor;
import entidades.Emprestimo;
import entidades.Livro;
import entidades.Notificacao;
import entidades.Professor;
import entidades.Sessao;
import entidades.TipoNotificacao;
import entidades.Usuario;
import rr.ClienteRR;
import rr.Despachante;
import rr.ExcecaoRemota;
import rr.RemoteObjectRef;
import rr.ServidorRR;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Scanner;

/**
 * Uso: java cliente.ClienteMain [hostServidor=127.0.0.1] [portaServidor=1099] [meuEndereco=127.0.0.1] [portaCallbackInicial=11000]
 *
 * Duas "threads" de trabalho, como pedido na questão 4: a principal cuida do menu (interação com o
 * usuário) e a do ServidorRR do cliente recebe as notificações do grupo e as exibe em tempo real.
 */
public class ClienteMain {
    private static Scanner entrada;
    private static BibliotecaCliente biblioteca;
    private static RemoteObjectRef meuCallback;
    private static Sessao sessao;
    private static boolean noGrupo;

    public static void main(String[] args) throws Exception {
        String hostServidor = args.length > 0 ? args[0] : "127.0.0.1";
        int portaServidor = args.length > 1 ? Integer.parseInt(args[1]) : 1099;
        String meuEndereco = args.length > 2 ? args[2] : "127.0.0.1";
        int portaCallback = args.length > 3 ? Integer.parseInt(args[3]) : 11000;
        System.setProperty("java.rmi.server.hostname", meuEndereco);

        entrada = new Scanner(System.in);
        biblioteca = new BibliotecaCliente(new ClienteRR(), hostServidor, portaServidor);

        System.out.println("=== Biblioteca (RMI) ===");
        if (!autenticarOuCadastrar()) {
            return;
        }
        meuCallback = publicarCallback(meuEndereco, portaCallback, sessao.getUsuario().getLogin());
        menu();
        System.exit(0);
    }

    private static boolean autenticarOuCadastrar() {
        while (true) {
            System.out.println("\n1) Entrar   2) Cadastrar-se   0) Sair");
            String opcao = ler("> ");
            if (opcao == null || opcao.equals("0")) {
                return false;
            }
            try {
                if (opcao.equals("2")) {
                    cadastrar();
                } else if (opcao.equals("1")) {
                    String login = ler("Login: ");
                    String senha = ler("Senha: ");
                    sessao = biblioteca.autenticar(login, senha);
                    System.out.println("Bem-vindo, " + sessao.getUsuario().getNome() + " [" + sessao.getUsuario().getTipo() + "]");
                    return true;
                }
            } catch (ExcecaoRemota e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }
    }

    private static void cadastrar() {
        String tipo = ler("Tipo (1 = Aluno, 2 = Professor): ");
        String nome = ler("Nome: ");
        String login = ler("Login: ");
        String senha = ler("Senha: ");
        String email = ler("E-mail: ");
        Usuario novo;
        if ("2".equals(tipo)) {
            novo = new Professor(null, nome, login, senha, email, ler("Departamento: "));
        } else {
            novo = new Aluno(null, nome, login, senha, email, ler("Matrícula: "), ler("Curso: "));
        }
        Usuario criado = biblioteca.cadastrarUsuario(novo);
        System.out.println("Cadastro realizado: " + criado + ". Agora faça login.");
    }


    private static RemoteObjectRef publicarCallback(String meuEndereco, int portaInicial, String login)
            throws RemoteException {
        Despachante despachante = new Despachante();
        despachante.registrar(CallbackCliente.NOME, new CallbackCliente(login));
        for (int porta = portaInicial; porta < portaInicial + 200; porta++) {
            try {
                new ServidorRR(porta, despachante).iniciar();
                return new RemoteObjectRef(meuEndereco, porta, CallbackCliente.NOME);
            } catch (RemoteException portaOcupada) {
                // tenta a próxima porta
            }
        }
        throw new RemoteException("Nenhuma porta livre para o callback a partir de " + portaInicial);
    }


    private static void menu() {
        boolean bibliotecario = sessao.getUsuario().getLimiteEmprestimos() == 0;
        while (true) {
            System.out.println("\n--- Menu ---");
            System.out.println("1) Listar livros");
            System.out.println("2) Emprestar livro");
            System.out.println("3) Devolver livro");
            System.out.println("4) Meus empréstimos");
            System.out.println("5) Entrar no grupo de notificações (joinGroup)");
            System.out.println("6) Sair do grupo de notificações (leaveGroup)");
            if (bibliotecario) {
                System.out.println("7) Cadastrar livro");
                System.out.println("8) Enviar notificação ao grupo");
            }
            System.out.println("0) Sair");
            String opcao = ler("> ");
            if (opcao == null || opcao.equals("0")) {
                sairDoGrupoSilenciosamente();
                return;
            }
            try {
                executar(opcao, bibliotecario);
            } catch (ExcecaoRemota e) {
                System.out.println("Erro: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Informe um número válido.");
            }
        }
    }

    private static void executar(String opcao, boolean bibliotecario) {
        String token = sessao.getToken();
        switch (opcao) {
            case "1":
                for (Livro l : biblioteca.listarLivros()) {
                    System.out.println("  " + l);
                }
                break;
            case "2": {
                Emprestimo e = biblioteca.emprestarLivro(token, Integer.parseInt(ler("Id do livro: ")));
                System.out.println("Emprestado! " + e);
                break;
            }
            case "3": {
                Emprestimo e = biblioteca.devolverLivro(token, Integer.parseInt(ler("Id do empréstimo: ")));
                System.out.println("Devolvido! " + e);
                break;
            }
            case "4": {
                List<Emprestimo> lista = biblioteca.listarMeusEmprestimos(token);
                if (lista.isEmpty()) {
                    System.out.println("  (nenhum empréstimo)");
                }
                for (Emprestimo e : lista) {
                    System.out.println("  " + e);
                }
                break;
            }
            case "5": {
                int membros = biblioteca.entrarNoGrupo(token, meuCallback);
                noGrupo = true;
                System.out.println("Você entrou no grupo. Membros: " + membros);
                break;
            }
            case "6":
                System.out.println(biblioteca.sairDoGrupo(token) ? "Você saiu do grupo." : "Você não estava no grupo.");
                noGrupo = false;
                break;
            case "7":
                if (bibliotecario) {
                    cadastrarLivro(token);
                    break;
                }
                System.out.println("Opção inválida.");
                break;
            case "8":
                if (bibliotecario) {
                    enviarNotificacao(token);
                    break;
                }
                System.out.println("Opção inválida.");
                break;
            default:
                System.out.println("Opção inválida.");
        }
    }

    private static void cadastrarLivro(String token) {
        String titulo = ler("Título: ");
        String descricao = ler("Descrição: ");
        int ano = Integer.parseInt(ler("Ano de publicação: "));
        Autor autor = new Autor(ler("Autor: "), ler("Nacionalidade do autor: "));
        int exemplares = Integer.parseInt(ler("Exemplares: "));
        Livro criado = biblioteca.cadastrarLivro(token, new Livro(titulo, descricao, ano, autor, exemplares));
        System.out.println("Livro cadastrado: " + criado);
    }

    private static void enviarNotificacao(String token) {
        String tipo = ler("Tipo (1 = NOTIFICACAO, 2 = ALERTA, 3 = ATUALIZACAO): ");
        TipoNotificacao t = "2".equals(tipo) ? TipoNotificacao.ALERTA
                : "3".equals(tipo) ? TipoNotificacao.ATUALIZACAO : TipoNotificacao.NOTIFICACAO;
        String mensagem = ler("Mensagem: ");
        int destinatarios = biblioteca.enviarNotificacao(token, new Notificacao(t, mensagem));
        System.out.println("Notificação enviada a " + destinatarios + " cliente(s).");
    }

    private static void sairDoGrupoSilenciosamente() {
        if (noGrupo) {
            try {
                biblioteca.sairDoGrupo(sessao.getToken());
            } catch (Exception ignorado) {
                // servidor pode já estar fora do ar
            }
        }
    }

    private static String ler(String prompt) {
        synchronized (System.out) {
            System.out.print(prompt);
            System.out.flush();
        }
        return entrada.hasNextLine() ? entrada.nextLine().trim() : null;
    }
}
