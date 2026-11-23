package rr;

public class RemoteObjectRef {
    private String host;
    private int porta;
    private String nome;

    public RemoteObjectRef() {
    }

    public RemoteObjectRef(String host, int porta, String nome) {
        this.host = host;
        this.porta = porta;
        this.nome = nome;
    }

    public String getHost() {
        return host;
    }

    public int getPorta() {
        return porta;
    }

    public String getNome() {
        return nome;
    }

    @Override
    public String toString() {
        return nome + "@" + host + ":" + porta;
    }
}
