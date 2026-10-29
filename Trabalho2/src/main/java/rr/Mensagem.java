package rr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Mensagem de requisição/resposta:
 * messageType | requestId | objectReference | methodId | arguments
 */
public class Mensagem {
    public static final int REQUEST = 0;
    public static final int REPLY = 1;

    private final int messageType;
    private final long requestId;
    private final String objectReference;
    private final String methodId;
    private final byte[] arguments;

    public Mensagem(int messageType, long requestId, String objectReference, String methodId, byte[] arguments) {
        this.messageType = messageType;
        this.requestId = requestId;
        this.objectReference = objectReference;
        this.methodId = methodId;
        this.arguments = arguments;
    }

    public byte[] empacotar() {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(bytes)) {
            out.writeInt(messageType);
            out.writeLong(requestId);
            out.writeUTF(objectReference);
            out.writeUTF(methodId);
            out.writeInt(arguments.length);
            out.write(arguments);
            out.flush();
            return bytes.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao empacotar mensagem", e);
        }
    }

    public static Mensagem desempacotar(byte[] dados) {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(dados))) {
            int tipo = in.readInt();
            long id = in.readLong();
            String objeto = in.readUTF();
            String metodo = in.readUTF();
            byte[] args = new byte[in.readInt()];
            in.readFully(args);
            return new Mensagem(tipo, id, objeto, metodo, args);
        } catch (IOException e) {
            throw new IllegalArgumentException("Mensagem malformada", e);
        }
    }

    public int getMessageType() {
        return messageType;
    }

    public long getRequestId() {
        return requestId;
    }

    public String getObjectReference() {
        return objectReference;
    }

    public String getMethodId() {
        return methodId;
    }

    public byte[] getArguments() {
        return arguments;
    }
}
