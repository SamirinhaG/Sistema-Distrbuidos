package servidor;

import entidades.Aluno;
import entidades.Autor;
import entidades.Bibliotecario;
import entidades.Livro;
import entidades.Professor;
import rr.ClienteRR;
import rr.Despachante;
import rr.ServidorRR;

/**
 * Servidor da biblioteca.
 * Uso: java servidor.ServidorMain [porta=1099] [enderecoPublico=127.0.0.1]
 */
public class ServidorMain {
    public static void main(String[] args) throws Exception {
        int porta = args.length > 0 ? Integer.parseInt(args[0]) : 1099;
        String enderecoPublico = args.length > 1 ? args[1] : "127.0.0.1";
        System.setProperty("java.rmi.server.hostname", enderecoPublico);

        Biblioteca biblioteca = new Biblioteca();
        povoar(biblioteca);

        GrupoNotificacao grupo = new GrupoNotificacao(new ClienteRR());

        Despachante despachante = new Despachante();
        despachante.registrar(UsuarioService.NOME, new UsuarioService(biblioteca));
        despachante.registrar(LivroService.NOME, new LivroService(biblioteca, grupo));
        despachante.registrar(EmprestimoService.NOME, new EmprestimoService(biblioteca, grupo));
        despachante.registrar(NotificacaoService.NOME, new NotificacaoService(biblioteca, grupo));

        ServidorRR servidor = new ServidorRR(porta, despachante);
        servidor.iniciar();

        System.out.println("Servidor da biblioteca no ar em " + enderecoPublico + ":" + porta);
        System.out.println("Objetos remotos: " + UsuarioService.NOME + ", " + LivroService.NOME + ", "
                + EmprestimoService.NOME + ", " + NotificacaoService.NOME);
        System.out.println("Usuários de teste: admin/admin123 (bibliotecário), maria/123 (aluna), joao/123 (professor)");
        Thread.currentThread().join();
    }

    private static void povoar(Biblioteca b) {
        b.registrarUsuario(new Bibliotecario(null, "Administrador", "admin", "admin123", "admin@ufc.br", "BIB-001"));
        b.registrarUsuario(new Aluno(null, "Maria Silva", "maria", "123", "maria@alu.ufc.br", "400001", "Sistemas de Informação"));
        b.registrarUsuario(new Professor(null, "João Souza", "joao", "123", "joao@ufc.br", "Computação"));

        b.registrarLivro(new Livro("Sistemas Distribuídos: Conceitos e Projeto",
                "Texto adotado na disciplina QXD0043", 2013, new Autor("George Coulouris", "Britânica"), 2));
        b.registrarLivro(new Livro("Código Limpo", "Habilidades práticas do Agile Software", 2009,
                new Autor("Robert C. Martin", "Americana"), 3));
        b.registrarLivro(new Livro("Dom Casmurro", "Romance", 1899, new Autor("Machado de Assis", "Brasileira"), 1));
    }
}
