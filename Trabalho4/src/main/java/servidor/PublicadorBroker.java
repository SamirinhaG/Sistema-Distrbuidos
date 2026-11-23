package servidor;

import broker.BrokerCliente;
import entidades.Notificacao;
import rr.ExcecaoRemota;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PublicadorBroker {
    private static final long INTERVALO_RETENTATIVA_MS = 2000;

    private static class Pendente {
        final String topico;
        final Notificacao notificacao;

        Pendente(String topico, Notificacao notificacao) {
            this.topico = topico;
            this.notificacao = notificacao;
        }
    }

    private final BrokerCliente broker;
    private final BlockingQueue<Pendente> caixaDeSaida = new LinkedBlockingQueue<>();

    public PublicadorBroker(BrokerCliente broker) {
        this.broker = broker;
        Thread t = new Thread(this::enviarSempre, "publicador-broker");
        t.setDaemon(true);
        t.start();
    }

    public void publicar(String topico, Notificacao notificacao) {
        caixaDeSaida.add(new Pendente(topico, notificacao));
        System.out.println("[publicador] '" + topico + "' na caixa de saída: " + notificacao.getMensagem());
    }

    private void enviarSempre() {
        while (true) {
            try {
                Pendente p = caixaDeSaida.take();
                boolean enviada = false;
                boolean avisou = false;
                while (!enviada) {
                    try {
                        int filas = broker.publicar(p.topico, p.notificacao);
                        System.out.println("[publicador] entregue ao broker (" + filas + " fila(s)): " + p.notificacao.getMensagem());
                        enviada = true;
                    } catch (ExcecaoRemota e) {
                        if (e.getCause() instanceof java.rmi.RemoteException) {
                            if (!avisou) {
                                System.out.println("[publicador] broker indisponível; mantendo na caixa de saída e tentando de novo...");
                                avisou = true;
                            }
                            Thread.sleep(INTERVALO_RETENTATIVA_MS);
                        } else {
                            System.out.println("[publicador] broker recusou a mensagem: " + e.getMessage());
                            enviada = true;
                        }
                    }
                }
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
