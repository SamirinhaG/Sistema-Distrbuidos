package cliente;

import broker.BrokerCliente;
import broker.Entrega;
import broker.Topicos;
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
import rr.ExcecaoRemota;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class ClienteMain {
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static Scanner entrada;
    private static BibliotecaCliente biblioteca;
    private static BrokerCliente broker;
    private static Sessao sessao;

    public static void main(String[] args) throws Exception {
        String hostServidor = args.length > 0 ? args[0] : "127.0.0.1";
        int portaServidor = args.length > 1 ? Integer.parseInt(args[1]) : 1099;
        String hostBroker = args.length > 2 ? args[2] : "127.0.0.1";
        int portaBroker = args.length > 3 ? Integer.parseInt(args[3]) : 2000;

        entrada = new Scanner(System.in);
        ClienteRR rr = new ClienteRR();
        biblioteca = new BibliotecaCliente(rr, hostServidor, portaServidor);
        broker = new BrokerCliente(rr, hostBroker, portaBroker);

        System.out.println("=== Biblioteca (RMI + Pub/Sub) ===");
        if (!autenticarOuCadastrar()) {
            return;
        }
        iniciarConsumidor(sessao.getUsuario().getLogin());
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

    private static void iniciarConsumidor(String login) {
        Thread t = new Thread(() -> {
            boolean offline = false;
            while (true) {
                try {
                    List<Entrega> pendentes = broker.consumir(login);
                    if (offline) {
                        sincrono("  [broker] conexão restabelecida");
                        offline = false;
                    }
                    for (Entrega e : pendentes) {
                        exibir(e);
                    }
                    if (!pendentes.isEmpty()) {
                        broker.confirmar(login, pendentes.get(pendentes.size() - 1).getId());
                    }
                } catch (ExcecaoRemota e) {
                    if (!offline) {
                        sincrono("  [broker] indisponível; o menu continua funcionando, tentando reconectar...");
                        offline = true;
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }, "consumidor-broker");
        t.setDaemon(true);
        t.start();
    }

    private static void exibir(Entrega e) {
        Notificacao n = e.getNotificacao();
        String hora = HORA.format(Instant.ofEpochMilli(n.getTimestamp()).atZone(ZoneId.systemDefault()));
        sincrono("  >>> [" + hora + "] (" + e.getTopico() + ") " + n.getTipo() + ": " + n.getMensagem());
    }

    private static void sincrono(String linha) {
        synchronized (System.out) {
            System.out.println();
            System.out.println(linha);
            System.out.print("> ");
            System.out.flush();
        }
    }

    private static void menu() {
        boolean bibliotecario = sessao.getUsuario().getLimiteEmprestimos() == 0;
        while (true) {
            System.out.println("\n--- Menu ---");
            System.out.println("1) Listar livros");
            System.out.println("2) Emprestar livro");
            System.out.println("3) Devolver livro");
            System.out.println("4) Meus empréstimos");
            System.out.println("5) Assinar tópico (" + String.join(", ", Topicos.TODOS) + ")");
            System.out.println("6) Cancelar assinatura de tópico");
            if (bibliotecario) {
                System.out.println("7) Cadastrar livro");
                System.out.println("8) Publicar aviso (tópico avisos)");
                System.out.println("9) Situação das filas no broker");
            }
            System.out.println("0) Sair");
            String opcao = ler("> ");
            if (opcao == null || opcao.equals("0")) {
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
                String topico = ler("Tópico: ");
                boolean novo = broker.assinar(sessao.getUsuario().getLogin(), topico);
                System.out.println(novo ? "Assinatura criada. Mesmo offline, você receberá as mensagens ao voltar."
                        : "Você já assinava esse tópico.");
                break;
            }
            case "6": {
                String topico = ler("Tópico: ");
                System.out.println(broker.cancelar(sessao.getUsuario().getLogin(), topico) ? "Assinatura cancelada." : "Você não assinava esse tópico.");
                break;
            }
            case "7":
                if (bibliotecario) {
                    cadastrarLivro(token);
                    break;
                }
                System.out.println("Opção inválida.");
                break;
            case "8":
                if (bibliotecario) {
                    publicarAviso(token);
                    break;
                }
                System.out.println("Opção inválida.");
                break;
            case "9":
                System.out.println(broker.resumo());
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

    private static void publicarAviso(String token) {
        String tipo = ler("Tipo (1 = NOTIFICACAO, 2 = ALERTA, 3 = ATUALIZACAO): ");
        TipoNotificacao t = "2".equals(tipo) ? TipoNotificacao.ALERTA
                : "3".equals(tipo) ? TipoNotificacao.ATUALIZACAO : TipoNotificacao.NOTIFICACAO;
        String mensagem = ler("Mensagem: ");
        biblioteca.publicarAviso(token, new Notificacao(t, mensagem));
        System.out.println("Aviso publicado no tópico '" + Topicos.AVISOS + "'.");
    }

    private static String ler(String prompt) {
        synchronized (System.out) {
            System.out.print(prompt);
            System.out.flush();
        }
        return entrada.hasNextLine() ? entrada.nextLine().trim() : null;
    }
}
