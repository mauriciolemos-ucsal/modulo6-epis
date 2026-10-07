package br.com.sgf.seguranca.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class RegrasTest {
    private final Trabalhador carlos = trabalhador("t1", "alfa", false);
    private final Trabalhador maria = trabalhador("t3", "alfa", true);
    private final Trabalhador paulo = trabalhador("t5", "beta", false);

    private static Trabalhador trabalhador(String id, String obraId, boolean fiscal) {
        return new Trabalhador(id, "000" + id, id, obraId, List.of(), fiscal, List.of());
    }

    @Test
    void rn07SoQuaseAcidenteEAcidentePermitemEpi() {
        assertFalse(Regras.permiteAssociarEpi("RISCO"));
        assertTrue(Regras.permiteAssociarEpi("QUASE_ACIDENTE"));
        assertTrue(Regras.permiteAssociarEpi("ACIDENTE"));
    }

    @Test
    void rn07DescartaEpisDeRiscoEDeduplica() {
        var epis = List.of(new EpiPapel("luva", "faltou"));
        assertTrue(Regras.episEfetivos("RISCO", epis).isEmpty());
        var duplicados = List.of(new EpiPapel("luva", "faltou"), new EpiPapel("luva", "falhou"));
        assertEquals(List.of(new EpiPapel("luva", "falhou")), Regras.episEfetivos("ACIDENTE", duplicados));
    }

    @Test
    void rn06ExigeClassificacaoEFoto() {
        assertEquals(2, Regras.validarOcorrencia("", false, "", List.of()).size());
        assertEquals(1, Regras.validarOcorrencia("QUALQUER", true, "", List.of()).size());
        assertTrue(Regras.validarOcorrencia("RISCO", true, "ok", List.of()).isEmpty());
    }

    @Test
    void exigePapelValidoParaCadaEpi() {
        var semPapel = List.of(new EpiPapel("luva", null));
        assertEquals(1, Regras.validarOcorrencia("ACIDENTE", true, "", semPapel).size());
        var invalido = List.of(new EpiPapel("luva", "quebrou"));
        assertEquals(1, Regras.validarOcorrencia("ACIDENTE", true, "", invalido).size());
        // Em Risco os EPIs são descartados, então não geram erro.
        assertTrue(Regras.validarOcorrencia("RISCO", true, "", semPapel).isEmpty());
    }

    @Test
    void limitaTamanhoDaDescricao() {
        String longa = "x".repeat(Regras.TAMANHO_MAX_DESCRICAO + 1);
        assertEquals(1, Regras.validarOcorrencia("RISCO", true, longa, List.of()).size());
    }

    @Test
    void fiscalRegistraEmNomeDeColegaDaMesmaObraMasNaoDeOutra() {
        assertTrue(Regras.podeRegistrarEmNomeDe(maria, carlos));
        assertFalse(Regras.podeRegistrarEmNomeDe(maria, paulo));
        assertFalse(Regras.podeRegistrarEmNomeDe(carlos, maria));
        assertTrue(Regras.podeRegistrarEmNomeDe(carlos, carlos));
    }
}
