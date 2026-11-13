package br.com.bancolab.conta.application.service;

import java.time.Clock;

import br.com.bancolab.conta.application.port.in.AbrirContaUseCase;
import br.com.bancolab.conta.application.port.out.AntifraudePort;
import br.com.bancolab.conta.application.port.out.AntifraudePort.ResultadoAntifraude;
import br.com.bancolab.conta.application.port.out.ContaRepository;
import br.com.bancolab.conta.domain.exception.AberturaReprovadaException;
import br.com.bancolab.conta.domain.exception.CpfJaCadastradoException;
import br.com.bancolab.conta.domain.model.Conta;
import br.com.bancolab.conta.domain.model.Cpf;

/**
 * CASO DE USO: orquestra o dominio e as portas de saida. Nao contem regra de
 * negocio "de verdade" (isso mora na Conta) nem detalhe tecnico (isso mora nos
 * adaptadores).
 *
 * Nao ha @Service aqui de proposito: quem registra esta classe como bean e a
 * CasosDeUsoConfig. Assim a camada de aplicacao nao depende do Spring.
 */
public class AbrirContaService implements AbrirContaUseCase {

    private final ContaRepository contaRepository;
    private final AntifraudePort antifraude;
    private final Clock clock;

    public AbrirContaService(ContaRepository contaRepository, AntifraudePort antifraude, Clock clock) {
        this.contaRepository = contaRepository;
        this.antifraude = antifraude;
        this.clock = clock;
    }

    @Override
    public Conta abrir(AbrirContaCommand command) {
        Cpf cpf = new Cpf(command.cpf());

        if (contaRepository.existePorCpf(cpf)) {
            throw new CpfJaCadastradoException();
        }

        if (antifraude.analisar(cpf) == ResultadoAntifraude.REPROVADO) {
            throw new AberturaReprovadaException();
        }

        Conta conta = Conta.abrir(command.titular(), cpf, clock.instant());
        return contaRepository.salvar(conta);
    }
}
