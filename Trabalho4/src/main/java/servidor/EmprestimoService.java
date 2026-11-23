package servidor;

import broker.Topicos;
import entidades.Emprestimo;
import entidades.Livro;
import entidades.Notificacao;
import entidades.StatusEmprestimo;
import entidades.TipoNotificacao;
import entidades.Usuario;
import rr.ExcecaoRemota;
import rr.MetodoRemoto;

import java.util.List;

public class EmprestimoService {
    public static final String NOME = "EmprestimoService";

    private final Biblioteca biblioteca;
    private final PublicadorBroker publicador;

    public EmprestimoService(Biblioteca biblioteca, PublicadorBroker publicador) {
        this.biblioteca = biblioteca;
        this.publicador = publicador;
    }

    @MetodoRemoto
    public Emprestimo emprestarLivro(String token, int idLivro) {
        Usuario usuario = biblioteca.exigirSessao(token);
        Livro livro = biblioteca.buscarLivro(idLivro);
        Emprestimo emprestimo;
        boolean acabaram;
        synchronized (biblioteca) {
            if (usuario.getLimiteEmprestimos() == 0) {
                throw new ExcecaoRemota("O perfil " + usuario.getTipo() + " não realiza empréstimos");
            }
            if (biblioteca.contarEmprestimosAtivos(usuario.getIdUsuario()) >= usuario.getLimiteEmprestimos()) {
                throw new ExcecaoRemota("Limite de " + usuario.getLimiteEmprestimos()
                        + " empréstimos ativos atingido");
            }
            if (!livro.emprestar()) {
                throw new ExcecaoRemota("Nenhum exemplar disponível de '" + livro.getTitulo() + "'");
            }
            emprestimo = biblioteca.registrarEmprestimo(usuario, livro);
            acabaram = livro.getExemplaresDisponiveis() == 0;
        }
        if (acabaram) {
            publicador.publicar(Topicos.DISPONIBILIDADE, new Notificacao(TipoNotificacao.ALERTA,
                    "Último exemplar de '" + livro.getTitulo() + "' foi emprestado"));
        }
        return emprestimo;
    }

    @MetodoRemoto
    public Emprestimo devolverLivro(String token, int idEmprestimo) {
        Usuario usuario = biblioteca.exigirSessao(token);
        Emprestimo emprestimo = biblioteca.buscarEmprestimo(idEmprestimo);
        boolean voltouADisponivel;
        synchronized (biblioteca) {
            if (!emprestimo.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
                throw new ExcecaoRemota("Este empréstimo pertence a outro usuário");
            }
            if (emprestimo.getStatus() == StatusEmprestimo.DEVOLVIDO) {
                throw new ExcecaoRemota("Este empréstimo já foi devolvido");
            }
            Livro livro = emprestimo.getLivro();
            voltouADisponivel = livro.getExemplaresDisponiveis() == 0;
            livro.devolver();
            emprestimo.registrarDevolucao();
        }
        if (voltouADisponivel) {
            publicador.publicar(Topicos.DISPONIBILIDADE, new Notificacao(TipoNotificacao.ATUALIZACAO,
                    "'" + emprestimo.getLivro().getTitulo() + "' voltou a ficar disponível"));
        }
        return emprestimo;
    }

    @MetodoRemoto
    public List<Emprestimo> listarMeusEmprestimos(String token) {
        Usuario usuario = biblioteca.exigirSessao(token);
        return biblioteca.emprestimosDoUsuario(usuario.getIdUsuario());
    }
}
