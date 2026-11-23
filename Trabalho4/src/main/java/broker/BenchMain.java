package broker;

import cliente.BibliotecaCliente;
import entidades.Notificacao;
import entidades.TipoNotificacao;
import rr.ClienteRR;

import java.util.List;

public class BenchMain {
    public static void main(String[] args) throws Exception {
        int n = args.length > 0 ? Integer.parseInt(args[0]) : 200;
        ClienteRR rr = new ClienteRR();
        BibliotecaCliente biblioteca = new BibliotecaCliente(rr, "127.0.0.1", 1099);
        BrokerCliente broker = new BrokerCliente(rr, "127.0.0.1", 2000);
        String assinante = "bench";
        broker.assinar(assinante, Topicos.AVISOS);

        biblioteca.listarLivros();
        long t0 = System.nanoTime();
        for (int i = 0; i < n; i++) {
            biblioteca.listarLivros();
        }
        double direto = (System.nanoTime() - t0) / 1e6 / n;

        t0 = System.nanoTime();
        for (int i = 0; i < n; i++) {
            broker.publicar(Topicos.AVISOS, new Notificacao(TipoNotificacao.NOTIFICACAO, "bench " + i));
        }
        double publicar = (System.nanoTime() - t0) / 1e6 / n;

        t0 = System.nanoTime();
        List<Entrega> lote = broker.consumir(assinante);
        broker.confirmar(assinante, lote.get(lote.size() - 1).getId());
        double consumirLote = (System.nanoTime() - t0) / 1e6;

        System.out.printf("Chamada direta ao servidor (listarLivros): %.2f ms por chamada%n", direto);
        System.out.printf("Publicação no broker (inclui gravar em disco): %.2f ms por mensagem%n", publicar);
        System.out.printf("Consumo + ack de um lote de %d mensagens: %.2f ms%n", lote.size(), consumirLote);
        System.out.println("Latência extra do consumo por polling: até 1000 ms (intervalo de consulta do cliente)");
        broker.cancelar(assinante, Topicos.AVISOS);
    }
}
