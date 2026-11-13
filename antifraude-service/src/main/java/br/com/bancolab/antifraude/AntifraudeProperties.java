package br.com.bancolab.antifraude;

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "antifraude")
public record AntifraudeProperties(Set<String> cpfsBloqueados) {

    public AntifraudeProperties {
        cpfsBloqueados = cpfsBloqueados == null ? Set.of() : Set.copyOf(cpfsBloqueados);
    }
}
