package br.com.bancolab.conta.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import br.com.bancolab.conta.domain.exception.CpfInvalidoException;

class CpfTest {

    @Test
    void aceitaCpfValidoEGuardaSomenteDigitos() {
        assertThat(new Cpf("529.982.247-25").numero()).isEqualTo("52998224725");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "123", "52998224724", "11111111111", "abc.def.ghi-jk"})
    void rejeitaCpfInvalido(String valor) {
        assertThatThrownBy(() -> new Cpf(valor)).isInstanceOf(CpfInvalidoException.class);
    }

    @Test
    void doisCpfsComMesmoNumeroSaoIguais() {
        assertThat(new Cpf("529.982.247-25")).isEqualTo(new Cpf("52998224725"));
    }
}
