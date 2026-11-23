package rr;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Transporte extends Remote {
    byte[] enviar(byte[] mensagemRequisicao) throws RemoteException;
}
