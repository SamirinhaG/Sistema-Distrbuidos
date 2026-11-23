package broker;

import entidades.Notificacao;

public class Entrega {
    private long id;
    private String topico;
    private Notificacao notificacao;

    public Entrega() {
    }

    public Entrega(long id, String topico, Notificacao notificacao) {
        this.id = id;
        this.topico = topico;
        this.notificacao = notificacao;
    }

    public long getId() {
        return id;
    }

    public String getTopico() {
        return topico;
    }

    public Notificacao getNotificacao() {
        return notificacao;
    }
}
