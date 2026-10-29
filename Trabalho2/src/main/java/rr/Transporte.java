package rr;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Único objeto remoto RMI do sistema: é o canal por onde passam as mensagens do protocolo
 * requisição-resposta
 */
public interface Transporte extends Remote {
    byte[] enviar(byte[] mensagemRequisicao) throws RemoteException;
}
