package br.com.bancolab.antifraude;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AnaliseControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ModoCaos modoCaos;

    @AfterEach
    void desligarCaos() {
        modoCaos.configurar(new ModoCaos.Configuracao(0, 0));
    }

    @Test
    void aprovaCpfQueNaoEstaNaLista() throws Exception {
        mvc.perform(get("/analises/52998224725"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value("APROVADO"));
    }

    @Test
    void reprovaCpfBloqueado() throws Exception {
        mvc.perform(get("/analises/11144477735"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value("REPROVADO"));
    }

    @Test
    void comTaxaDeErroTotalResponde503() throws Exception {
        mvc.perform(put("/caos").param("taxaErro", "1")).andExpect(status().isOk());

        mvc.perform(get("/analises/52998224725")).andExpect(status().isServiceUnavailable());
    }
}
