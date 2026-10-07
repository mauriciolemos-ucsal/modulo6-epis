package br.com.sgf.seguranca.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Regras de negócio do Módulo 6 aplicadas no servidor. Funções puras (sem Spring, sem banco),
 * espelhando src/domain/regras.js do front — o servidor é quem realmente garante as regras.
 */
public final class Regras {
    public static final Set<String> TIPOS = Set.of("RISCO", "QUASE_ACIDENTE", "ACIDENTE");
    public static final Set<String> PAPEIS_EPI = Set.of("faltou", "falhou", "ajudou");
    public static final int TAMANHO_MAX_DESCRICAO = 2000;

    private Regras() {}

    /** RN07 — só Quase-Acidente e Acidente admitem associação com o papel do EPI. */
    public static boolean permiteAssociarEpi(String tipo) {
        return "QUASE_ACIDENTE".equals(tipo) || "ACIDENTE".equals(tipo);
    }

    /** Fiscal pode registrar em nome de outro trabalhador, desde que da mesma obra (RN09/RN10). */
    public static boolean podeRegistrarEmNomeDe(Trabalhador usuario, Trabalhador autor) {
        if (usuario == null || autor == null) return false;
        if (usuario.id().equals(autor.id())) return true;
        return usuario.fiscal() && usuario.obraId().equals(autor.obraId());
    }

    /** RN07 — EPIs descartados em Risco Identificado; duplicados no mesmo EPI contam uma vez. */
    public static List<EpiPapel> episEfetivos(String tipo, List<EpiPapel> epis) {
        if (!permiteAssociarEpi(tipo) || epis == null) return List.of();
        Map<String, EpiPapel> porEpi = new LinkedHashMap<>();
        for (EpiPapel e : epis) porEpi.put(e.epiId(), e);
        return List.copyOf(porEpi.values());
    }

    /** Valida o rascunho de uma ocorrência. Lista vazia = válido. */
    public static List<String> validarOcorrencia(String tipo, boolean temFoto, String descricao, List<EpiPapel> epis) {
        List<String> erros = new ArrayList<>();
        // RN06 — classificação obrigatória.
        if (tipo == null || !TIPOS.contains(tipo)) erros.add("Selecione a classificação da ocorrência.");
        if (!temFoto) erros.add("Anexe uma foto da ocorrência.");
        if (descricao != null && descricao.length() > TAMANHO_MAX_DESCRICAO) {
            erros.add("A descrição deve ter no máximo " + TAMANHO_MAX_DESCRICAO + " caracteres.");
        }
        if (permiteAssociarEpi(tipo)) {
            boolean semPapel = episEfetivos(tipo, epis).stream()
                .anyMatch(e -> e.papel() == null || !PAPEIS_EPI.contains(e.papel()));
            if (semPapel) erros.add("Informe o papel (faltou / falhou / ajudou) de cada EPI marcado.");
        }
        return erros;
    }
}
