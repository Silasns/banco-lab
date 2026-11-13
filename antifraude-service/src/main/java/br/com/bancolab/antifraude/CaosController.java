package br.com.bancolab.antifraude;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Liga e desliga falhas neste servico, sem precisar reinicia-lo.
 *
 *   PUT    /caos?latenciaMs=3000            -> toda analise demora 3 segundos
 *   PUT    /caos?taxaErro=0.5               -> metade das analises responde 503
 *   PUT    /caos?latenciaMs=2000&taxaErro=1 -> demora 2s e sempre falha
 *   DELETE /caos                            -> volta ao normal
 */
@RestController
@RequestMapping("/caos")
public class CaosController {

    private final ModoCaos modoCaos;

    public CaosController(ModoCaos modoCaos) {
        this.modoCaos = modoCaos;
    }

    @GetMapping
    public ModoCaos.Configuracao consultar() {
        return modoCaos.atual();
    }

    @PutMapping
    public ResponseEntity<?> configurar(@RequestParam(defaultValue = "0") long latenciaMs,
                                        @RequestParam(defaultValue = "0") double taxaErro) {
        try {
            modoCaos.configurar(new ModoCaos.Configuracao(latenciaMs, taxaErro));
            return ResponseEntity.ok(modoCaos.atual());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping
    public ModoCaos.Configuracao desligar() {
        modoCaos.configurar(new ModoCaos.Configuracao(0, 0));
        return modoCaos.atual();
    }
}
