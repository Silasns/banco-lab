package br.com.bancolab.antifraude;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analises")
public class AnaliseController {

    private static final Logger log = LoggerFactory.getLogger(AnaliseController.class);

    private final AntifraudeProperties properties;
    private final ModoCaos modoCaos;

    public AnaliseController(AntifraudeProperties properties, ModoCaos modoCaos) {
        this.properties = properties;
        this.modoCaos = modoCaos;
    }

    @GetMapping("/{cpf}")
    public AnaliseResponse analisar(@PathVariable String cpf) {
        modoCaos.aplicar();

        boolean bloqueado = properties.cpfsBloqueados().contains(cpf);
        log.info("Analise do CPF {} -> {}", cpf, bloqueado ? "REPROVADO" : "APROVADO");

        return bloqueado
                ? new AnaliseResponse(cpf, "REPROVADO", "CPF consta na lista de restricoes")
                : new AnaliseResponse(cpf, "APROVADO", null);
    }

    public record AnaliseResponse(String cpf, String resultado, String motivo) {
    }
}
