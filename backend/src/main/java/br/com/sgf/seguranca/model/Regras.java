package br.com.sgf.seguranca.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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

    /** RN09 — painel exclusivo do Fiscal. */
    public static boolean podeVerPainel(Trabalhador usuario) {
        return usuario != null && usuario.fiscal();
    }

    /** RN05 — EPIs exigidos = básicos da obra ∪ obrigatórios de todas as funções do trabalhador. */
    public static List<String> episExigidos(Trabalhador trabalhador, Obra obra, List<Funcao> funcoes) {
        Set<String> exigidos = new LinkedHashSet<>();
        if (obra != null && obra.episBasicos() != null) exigidos.addAll(obra.episBasicos());
        for (String funcaoId : trabalhador.funcoes()) {
            funcoes.stream().filter(f -> f.id().equals(funcaoId)).findFirst()
                .ifPresent(f -> exigidos.addAll(f.episObrigatorios()));
        }
        return List.copyOf(exigidos);
    }

    public static Conformidade conformidade(Trabalhador trabalhador, Obra obra, List<Funcao> funcoes) {
        List<String> exigidos = episExigidos(trabalhador, obra, funcoes);
        Set<String> emUso = new HashSet<>(trabalhador.episEmUso() == null ? List.of() : trabalhador.episEmUso());
        List<String> faltando = exigidos.stream().filter(e -> !emUso.contains(e)).toList();
        int percentual = exigidos.isEmpty() ? 100 : percentual(exigidos.size() - faltando.size(), exigidos.size());
        return new Conformidade(exigidos, faltando, percentual, faltando.isEmpty());
    }

    /** Indicadores do painel do Fiscal a partir dos dados de uma única obra (RN10). */
    public static Painel indicadores(List<Ocorrencia> ocorrencias, List<Trabalhador> trabalhadores,
                                     Obra obra, List<Funcao> funcoes) {
        Map<String, Integer> contagem = new LinkedHashMap<>();
        for (String t : List.of("RISCO", "QUASE_ACIDENTE", "ACIDENTE")) contagem.put(t, 0);
        Map<String, Integer> papeis = new LinkedHashMap<>();
        for (String p : List.of("faltou", "falhou", "ajudou")) papeis.put(p, 0);
        for (Ocorrencia o : ocorrencias) {
            contagem.merge(o.tipo(), 1, Integer::sum);
            for (EpiPapel e : o.epis()) papeis.merge(e.papel(), 1, Integer::sum);
        }

        List<Painel.NaoConforme> naoConformes = new ArrayList<>();
        for (Trabalhador t : trabalhadores) {
            Conformidade c = conformidade(t, obra, funcoes);
            if (!c.conforme()) naoConformes.add(new Painel.NaoConforme(t.id(), t.nome(), c.faltando()));
        }
        int conformes = trabalhadores.size() - naoConformes.size();
        int percentualConformes = trabalhadores.isEmpty() ? 100 : percentual(conformes, trabalhadores.size());
        return new Painel(ocorrencias.size(), contagem, papeis, percentualConformes, naoConformes);
    }

    /** Arredonda como Math.round do JavaScript, para o front e o servidor mostrarem o mesmo número. */
    private static int percentual(int parte, int todo) {
        return (int) Math.round(parte * 100.0 / todo);
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
