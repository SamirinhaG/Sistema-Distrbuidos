package rr;

/**
 * Referência a um objeto remoto: identifica o nó (host + porta do registro RMI)
 * e o nome do objeto dentro dele. É esta referência que trafega nas chamadas quando um objeto
 * é passado por referência (ex: o callback do cliente que o servidor usa para notificá-lo).
 */
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
