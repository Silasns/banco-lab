package br.com.bancolab.conta.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

import br.com.bancolab.conta.application.port.out.AntifraudePort;
import br.com.bancolab.conta.application.port.out.AntifraudePort.ResultadoAntifraude;

/**
 * Teste de integracao: sobe a aplicacao inteira (web + caso de uso + JPA + H2)
 * e troca apenas o adaptador do antifraude por um mock, para nao depender do
 * outro servico estar no ar.
 *
 * Os testes marcados com @Disabled sao o criterio de aceite do Desafio 1:
 * remova o @Disabled e faca-os passar.
 */
// create-drop + DirtiesContext: cada teste comeca com o banco vazio.
@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class ContaApiTest {

    private static final String MARIA = """
            {"titular": "Maria Silva", "cpf": "529.982.247-25"}
            """;
    private static final String JOAO = """
            {"titular": "Joao Souza", "cpf": "111.444.777-35"}
            """;

    @Autowired
    MockMvc mvc;

    @MockitoBean
    AntifraudePort antifraude;

    @BeforeEach
    void antifraudeAprovaTudo() {
        given(antifraude.analisar(any())).willReturn(ResultadoAntifraude.APROVADO);
    }

    // ---------- fluxo de exemplo (ja funciona) ----------

    @Test
    void abreConta() throws Exception {
        mvc.perform(post("/contas").contentType(MediaType.APPLICATION_JSON).content(MARIA))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", Matchers.containsString("/contas/")))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.cpf").value("52998224725"))
                .andExpect(jsonPath("$.saldo").value(0))
                .andExpect(jsonPath("$.status").value("ATIVA"));
    }

    @Test
    void cpfDuplicadoResponde409() throws Exception {
        mvc.perform(post("/contas").contentType(MediaType.APPLICATION_JSON).content(MARIA))
                .andExpect(status().isCreated());

        mvc.perform(post("/contas").contentType(MediaType.APPLICATION_JSON).content(MARIA))
                .andExpect(status().isConflict());
    }

    @Test
    void cpfInvalidoResponde422() throws Exception {
        mvc.perform(post("/contas").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titular": "Maria Silva", "cpf": "123"}
                                """))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void titularEmBrancoResponde400() throws Exception {
        mvc.perform(post("/contas").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titular": "", "cpf": "52998224725"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reprovadoNoAntifraudeResponde422() throws Exception {
        given(antifraude.analisar(any())).willReturn(ResultadoAntifraude.REPROVADO);

        mvc.perform(post("/contas").contentType(MediaType.APPLICATION_JSON).content(MARIA))
                .andExpect(status().isUnprocessableEntity());
    }

    // ---------- Desafio 1: path param e query param ----------

    @Test
    @Disabled("Desafio 1")
    void buscaContaPorId() throws Exception {
        String id = abrirERetornarId(MARIA);

        mvc.perform(get("/contas/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.titular").value("Maria Silva"));
    }

    @Test
    @Disabled("Desafio 1")
    void contaInexistenteResponde404() throws Exception {
        mvc.perform(get("/contas/{id}", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Disabled("Desafio 1")
    void idQueNaoEUuidResponde400() throws Exception {
        mvc.perform(get("/contas/{id}", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Disabled("Desafio 1")
    void listaTodasAsContas() throws Exception {
        abrirERetornarId(MARIA);
        abrirERetornarId(JOAO);

        mvc.perform(get("/contas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @Disabled("Desafio 1")
    void filtraContasPorTitular() throws Exception {
        abrirERetornarId(MARIA);
        abrirERetornarId(JOAO);

        mvc.perform(get("/contas").param("titular", "maria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].titular").value("Maria Silva"));
    }

    @Test
    @Disabled("Desafio 1")
    void filtraContasPorStatus() throws Exception {
        abrirERetornarId(MARIA);

        mvc.perform(get("/contas").param("status", "ATIVA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mvc.perform(get("/contas").param("status", "BLOQUEADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @Disabled("Desafio 1")
    void statusDesconhecidoNoFiltroResponde400() throws Exception {
        mvc.perform(get("/contas").param("status", "QUALQUER"))
                .andExpect(status().isBadRequest());
    }

    private String abrirERetornarId(String json) throws Exception {
        MvcResult resultado = mvc.perform(post("/contas").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(resultado.getResponse().getContentAsString(), "$.id");
    }
}
