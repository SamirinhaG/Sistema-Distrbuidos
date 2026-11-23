package broker;

import rr.Despachante;
import rr.ServidorRR;

import java.nio.file.Path;

public class BrokerMain {
    public static void main(String[] args) throws Exception {
        int porta = args.length > 0 ? Integer.parseInt(args[0]) : 2000;
        String enderecoPublico = args.length > 1 ? args[1] : "127.0.0.1";
        Path arquivo = Path.of(args.length > 2 ? args[2] : "broker-dados.json");
        System.setProperty("java.rmi.server.hostname", enderecoPublico);

        Despachante despachante = new Despachante();
        despachante.registrar(BrokerService.NOME, new BrokerService(arquivo));
        new ServidorRR(porta, despachante).iniciar();

        System.out.println("Broker no ar em " + enderecoPublico + ":" + porta + " (dados em " + arquivo + ")");
        System.out.println("Tópicos: " + Topicos.TODOS);
        Thread.currentThread().join();
    }
}
