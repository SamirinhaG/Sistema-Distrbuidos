package broker;

import java.util.List;

public final class Topicos {
    public static final String ACERVO = "acervo";
    public static final String DISPONIBILIDADE = "disponibilidade";
    public static final String AVISOS = "avisos";

    public static final List<String> TODOS = List.of(ACERVO, DISPONIBILIDADE, AVISOS);

    private Topicos() {
    }
}
