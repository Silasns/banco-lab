package br.com.bancolab.antifraude;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Simulador de um servico de antifraude de terceiros.
 *
 * Este servico ja esta pronto: voce nao precisa mexer nele. Ele existe para o
 * conta-service ter "outro sistema" com quem conversar pela rede, e para voce
 * poder quebra-lo de proposito (veja CaosController) nos desafios de resiliencia.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class AntifraudeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AntifraudeServiceApplication.class, args);
    }
}
