package rr;

public class ExcecaoRemota extends RuntimeException {
    public ExcecaoRemota(String mensagem) {
        super(mensagem);
    }

    public ExcecaoRemota(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
