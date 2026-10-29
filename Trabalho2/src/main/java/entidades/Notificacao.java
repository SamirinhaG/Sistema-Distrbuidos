package entidades;

/**
 * Mensagem enviada a todos os clientes do grupo
 */
public class Notificacao {
    private TipoNotificacao tipo;
    private String mensagem;
    private long timestamp;

    public Notificacao() {
    }

    public Notificacao(TipoNotificacao tipo, String mensagem) {
        this.tipo = tipo;
        this.mensagem = mensagem;
        this.timestamp = System.currentTimeMillis();
    }

    public TipoNotificacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoNotificacao tipo) {
        this.tipo = tipo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
