package br.com.bancolab.conta.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.bancolab.conta.application.port.in.AbrirContaUseCase.AbrirContaCommand;
import br.com.bancolab.conta.application.port.out.AntifraudePort;
import br.com.bancolab.conta.application.port.out.AntifraudePort.ResultadoAntifraude;
import br.com.bancolab.conta.application.port.out.ContaRepository;
import br.com.bancolab.conta.domain.exception.AberturaReprovadaException;
import br.com.bancolab.conta.domain.exception.CpfInvalidoException;
import br.com.bancolab.conta.domain.exception.CpfJaCadastradoException;
import br.com.bancolab.conta.domain.model.Conta;
import br.com.bancolab.conta.domain.model.Cpf;

/**
 * Teste do caso de uso SEM Spring, SEM banco e SEM rede.
 *
 * Este e o maior ganho pratico da arquitetura hexagonal: como o caso de uso
 * so conhece portas (interfaces), aqui plugamos adaptadores falsos escritos
 * em poucas linhas, no lugar do JPA e do REST.
 */
class AbrirContaServiceTest {

    private static final Clock RELOGIO_FIXO =
            Clock.fixed(Instant.parse("2026-01-10T12:00:00Z"), ZoneOffset.UTC);

    private final ContaRepositoryEmMemoria repositorio = new ContaRepositoryEmMemoria();

    @Test
    void abreContaQuandoAntifraudeAprova() {
        var service = new AbrirContaService(repositorio, cpf -> ResultadoAntifraude.APROVADO, RELOGIO_FIXO);

        Conta conta = service.abrir(new AbrirContaCommand("Maria Silva", "529.982.247-25"));

        assertThat(conta.getCpf()).isEqualTo(new Cpf("52998224725"));
        assertThat(conta.getCriadaEm()).isEqualTo(RELOGIO_FIXO.instant());
        assertThat(repositorio.contas).containsExactly(conta);
    }

    @Test
    void naoAbreContaQuandoAntifraudeReprova() {
        var service = new AbrirContaService(repositorio, cpf -> ResultadoAntifraude.REPROVADO, RELOGIO_FIXO);

        assertThatThrownBy(() -> service.abrir(new AbrirContaCommand("Maria Silva", "52998224725")))
                .isInstanceOf(AberturaReprovadaException.class);
        assertThat(repositorio.contas).isEmpty();
    }

    @Test
    void naoAbreSegundaContaParaOMesmoCpf() {
        var service = new AbrirContaService(repositorio, cpf -> ResultadoAntifraude.APROVADO, RELOGIO_FIXO);
        service.abrir(new AbrirContaCommand("Maria Silva", "52998224725"));

        assertThatThrownBy(() -> service.abrir(new AbrirContaCommand("Maria S.", "529.982.247-25")))
                .isInstanceOf(CpfJaCadastradoException.class);
    }

    @Test
    void cpfInvalidoNemChegaAConsultarOAntifraude() {
        AntifraudePort antifraudeQueNaoDeveSerChamado = cpf -> {
            throw new AssertionError("o antifraude nao deveria ser consultado");
        };
        var service = new AbrirContaService(repositorio, antifraudeQueNaoDeveSerChamado, RELOGIO_FIXO);

        assertThatThrownBy(() -> service.abrir(new AbrirContaCommand("Maria Silva", "123")))
                .isInstanceOf(CpfInvalidoException.class);
    }

    /** Adaptador de saida falso: uma lista no lugar do banco de dados. */
    static class ContaRepositoryEmMemoria implements ContaRepository {

        final List<Conta> contas = new ArrayList<>();

        @Override
        public Conta salvar(Conta conta) {
            contas.add(conta);
            return conta;
        }

        @Override
        public boolean existePorCpf(Cpf cpf) {
            return contas.stream().anyMatch(conta -> conta.getCpf().equals(cpf));
        }
    }
}
