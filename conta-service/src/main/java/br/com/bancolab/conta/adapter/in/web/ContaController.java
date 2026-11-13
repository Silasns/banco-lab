package br.com.bancolab.conta.adapter.in.web;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.bancolab.conta.application.port.in.AbrirContaUseCase;
import br.com.bancolab.conta.application.port.in.AbrirContaUseCase.AbrirContaCommand;
import br.com.bancolab.conta.domain.model.Conta;
import jakarta.validation.Valid;

/**
 * ADAPTADOR DE ENTRADA: traduz HTTP para uma chamada de caso de uso, e a
 * resposta do caso de uso de volta para HTTP. Nao tem "if" de negocio.
 *
 * Repare que ele depende da PORTA (AbrirContaUseCase), nao do AbrirContaService.
 */
@RestController
@RequestMapping("/contas")
public class ContaController {

    private final AbrirContaUseCase abrirConta;

    public ContaController(AbrirContaUseCase abrirConta) {
        this.abrirConta = abrirConta;
    }

    @PostMapping
    public ResponseEntity<ContaResponse> abrir(@Valid @RequestBody AbrirContaRequest request) {
        Conta conta = abrirConta.abrir(new AbrirContaCommand(request.titular(), request.cpf()));

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(conta.getId())
                .toUri();

        return ResponseEntity.created(location).body(ContaResponse.de(conta));
    }
}
