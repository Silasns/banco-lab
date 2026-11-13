package br.com.bancolab.conta.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.bancolab.conta.application.port.in.AbrirContaUseCase;
import br.com.bancolab.conta.application.port.out.AntifraudePort;
import br.com.bancolab.conta.application.port.out.ContaRepository;
import br.com.bancolab.conta.application.service.AbrirContaService;

/**
 * Aqui o hexagono e "montado": o Spring entrega os adaptadores de saida e nos
 * devolvemos os casos de uso prontos. E o unico lugar que conhece os dois lados.
 */
@Configuration
public class CasosDeUsoConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    AbrirContaUseCase abrirContaUseCase(ContaRepository contaRepository, AntifraudePort antifraude, Clock clock) {
        return new AbrirContaService(contaRepository, antifraude, clock);
    }
}
