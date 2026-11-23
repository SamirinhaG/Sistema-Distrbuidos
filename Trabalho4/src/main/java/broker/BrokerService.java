package broker;

import com.google.gson.reflect.TypeToken;
import entidades.Notificacao;
import rr.ExcecaoRemota;
import rr.MetodoRemoto;
import util.Json;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BrokerService {
    public static final String NOME = "Broker";

    private static class Estado {
        long proximoId = 1;
        Map<String, Set<String>> assinaturas = new HashMap<>();
        Map<String, List<Entrega>> filas = new HashMap<>();
    }

    private final Path arquivo;
    private Estado estado = new Estado();

    public BrokerService(Path arquivo) {
        this.arquivo = arquivo;
        carregar();
    }

    @MetodoRemoto
    public synchronized boolean assinar(String assinante, String topico) {
        validar(assinante, topico);
        boolean novo = estado.assinaturas.computeIfAbsent(assinante, k -> new HashSet<>()).add(topico);
        estado.filas.computeIfAbsent(assinante, k -> new ArrayList<>());
        salvar();
        if (novo) {
            System.out.println("[broker] " + assinante + " assinou '" + topico + "'");
        }
        return novo;
    }

    @MetodoRemoto
    public synchronized boolean cancelar(String assinante, String topico) {
        Set<String> topicos = estado.assinaturas.get(assinante);
        boolean removido = topicos != null && topicos.remove(topico);
        if (removido) {
            salvar();
            System.out.println("[broker] " + assinante + " cancelou '" + topico + "'");
        }
        return removido;
    }

    @MetodoRemoto
    public synchronized List<String> assinaturas(String assinante) {
        return new ArrayList<>(estado.assinaturas.getOrDefault(assinante, Set.of()));
    }

    @MetodoRemoto
    public synchronized int publicar(String topico, Notificacao notificacao) {
        if (topico == null || !Topicos.TODOS.contains(topico)) {
            throw new ExcecaoRemota("Tópico inexistente: " + topico);
        }
        if (notificacao == null) {
            throw new ExcecaoRemota("Notificação vazia");
        }
        int copias = 0;
        for (Map.Entry<String, Set<String>> e : estado.assinaturas.entrySet()) {
            if (e.getValue().contains(topico)) {
                estado.filas.computeIfAbsent(e.getKey(), k -> new ArrayList<>())
                        .add(new Entrega(estado.proximoId++, topico, notificacao));
                copias++;
            }
        }
        salvar();
        System.out.println("[broker] publicado em '" + topico + "' -> " + copias + " fila(s): " + notificacao.getMensagem());
        return copias;
    }

    @MetodoRemoto
    public synchronized List<Entrega> consumir(String assinante) {
        return new ArrayList<>(estado.filas.getOrDefault(assinante, List.of()));
    }

    @MetodoRemoto
    public synchronized int confirmar(String assinante, long ateId) {
        List<Entrega> fila = estado.filas.get(assinante);
        if (fila == null) {
            return 0;
        }
        int antes = fila.size();
        fila.removeIf(m -> m.getId() <= ateId);
        int removidas = antes - fila.size();
        if (removidas > 0) {
            salvar();
        }
        return removidas;
    }

    @MetodoRemoto
    public synchronized String resumo() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Set<String>> e : estado.assinaturas.entrySet()) {
            sb.append(e.getKey()).append(" assina ").append(e.getValue()).append(" | pendentes: ")
                    .append(estado.filas.getOrDefault(e.getKey(), List.of()).size()).append('\n');
        }
        return sb.length() == 0 ? "(sem assinantes)" : sb.toString().trim();
    }

    private static void validar(String assinante, String topico) {
        if (assinante == null || assinante.isBlank()) {
            throw new ExcecaoRemota("Assinante inválido");
        }
        if (topico == null || !Topicos.TODOS.contains(topico)) {
            throw new ExcecaoRemota("Tópico inexistente: " + topico + ". Use: " + Topicos.TODOS);
        }
    }

    private void salvar() {
        try {
            Path temp = arquivo.resolveSibling(arquivo.getFileName() + ".tmp");
            Files.writeString(temp, Json.GSON.toJson(estado), StandardCharsets.UTF_8);
            Files.move(temp, arquivo, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            System.out.println("[broker] AVISO: não foi possível gravar em disco: " + e.getMessage());
        }
    }

    private void carregar() {
        if (!Files.exists(arquivo)) {
            return;
        }
        try {
            Type tipo = new TypeToken<Estado>() {
            }.getType();
            Estado lido = Json.GSON.fromJson(Files.readString(arquivo, StandardCharsets.UTF_8), tipo);
            if (lido != null) {
                estado = lido;
                int pendentes = estado.filas.values().stream().mapToInt(List::size).sum();
                System.out.println("[broker] estado recuperado de " + arquivo + ": " + estado.assinaturas.size()
                        + " assinante(s), " + pendentes + " mensagem(ns) pendente(s)");
            }
        } catch (IOException | RuntimeException e) {
            System.out.println("[broker] não foi possível ler " + arquivo + ", começando vazio: " + e.getMessage());
        }
    }
}
