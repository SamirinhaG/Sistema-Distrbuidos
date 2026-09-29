import socket
import random
import time
import sys

LOSS_RATE = 0.3
AVERAGE_DELAY = 100  # milissegundos

def print_data(data, address):
    """
    Imprimir o dado de Ping para o trecho de saída padrão.
    """
    # O dado da mensagem está contido numa única linha, então leia esta linha.
    line = data.decode(errors="ignore").splitlines()[0] if data else ""

    # Imprimir o endereço do hospedeiro e o dado recebido dele.
    print(f"Received from {address[0]}:{line}")


def main():
    if len(sys.argv) != 2:
        print("Required arguments: port")
        return

    port = int(sys.argv[1])

    # Criar o socket UDP e vincula a porta informada.
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.bind(("", port))

    while True:
        # Bloquear até que o hospedeiro receba o pacote UDP.
        data, address = sock.recvfrom(1024)

        # Imprimir os dados recebidos.
        print_data(data, address)

        # Decidir se responde, ou simula perda de pacotes.
        if random.random() < LOSS_RATE:
            print("Reply not sent.")
            continue

        # Simular o atraso da rede.
        time.sleep((random.random() * 2 * AVERAGE_DELAY) / 1000)

        # Enviar resposta.
        sock.sendto(data, address)
        print("Reply sent.")


if __name__ == "__main__":
    main()