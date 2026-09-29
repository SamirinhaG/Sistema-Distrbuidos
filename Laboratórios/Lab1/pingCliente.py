import sys
import socket
import time

TIMEOUT = 1.0  # tempo limite

def main():
    if len(sys.argv) != 3:
        print("Required arguments: host port")
        return

    host = sys.argv[1]
    port = int(sys.argv[2])

    # Cria o socket UDP e define o timeout de 1 segundo
    cliente_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    cliente_socket.settimeout(TIMEOUT)

    pacotes_transmitidos = 10
    pacotes_recebidos = 0
    rtt_list = []

    print(f"PING {host} via UDP:\n")

    for sequence in range(10):
        tempo_envio = time.time()
        
        payload = f"PING {sequence} {tempo_envio}\r\n"
        
        try:
            # Envia a requisição
            cliente_socket.sendto(payload.encode('utf-8'), (host, port))

            # Aguarda a resposta
            data, server_address = cliente_socket.recvfrom(1024)
            tempo_recebimento = time.time()
            
            # RTT em milissegundos
            rtt = (tempo_recebimento - tempo_envio) * 1000
            rtt_list.append(rtt)
            pacotes_recebidos += 1

            reply_message = data.decode('utf-8', errors='ignore').strip()
            print(f"Resposta de {server_address[0]}: seq={sequence} rtt={rtt:.2f} ms ({reply_message})")

        except socket.timeout:
            print(f"Requisição seq={sequence} expirou (Request timed out).")

        # Aguarda 1 segundo antes do próximo envio
        time.sleep(1)

    cliente_socket.close()

    print("\n--- Dados do Ping ---")
    pacotes_perdidos = pacotes_transmitidos - pacotes_recebidos
    loss_rate = (pacotes_perdidos / pacotes_transmitidos) * 100

    print(f"{pacotes_transmitidos} pacotes transmitidos, {pacotes_recebidos} recebidos, {loss_rate:.1f}% de perda de pacotes")

    if pacotes_recebidos > 0:
        rtt_min = min(rtt_list)
        rtt_max = max(rtt_list)
        rtt_medio = sum(rtt_list) / len(rtt_list)
        print(f"RTT min/avg/max = {rtt_min:.2f} / {rtt_medio:.2f} / {rtt_max:.2f} ms")

if __name__ == '__main__':
    main()