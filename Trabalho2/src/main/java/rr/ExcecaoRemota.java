package rr;

/** Erro devolvido pelo objeto remoto (regra de negócio violada, objeto inexistente, etc) */
public class ExcecaoRemota extends RuntimeException {
    public ExcecaoRemota(String mensagem) {
        super(mensagem);
    }

    public ExcecaoRemota(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
