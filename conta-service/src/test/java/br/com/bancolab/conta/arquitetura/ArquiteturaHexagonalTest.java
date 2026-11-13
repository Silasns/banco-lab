package br.com.bancolab.conta.arquitetura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * As regras da arquitetura hexagonal escritas como teste.
 *
 * Se voce, durante um desafio, importar uma classe do Spring dentro do dominio
 * ou fizer o controller chamar o repositorio JPA direto, este teste quebra e
 * te diz exatamente qual linha furou o hexagono. Nunca o desabilite: entenda
 * por que ele quebrou.
 */
@AnalyzeClasses(packages = "br.com.bancolab.conta", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquiteturaHexagonalTest {

    @ArchTest
    static final ArchRule dominioSoConheceJavaEOProprioDominio =
            classes().that().resideInAPackage("..domain..")
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage("java..", "..domain..");

    @ArchTest
    static final ArchRule aplicacaoSoConheceODominioEAsPortas =
            classes().that().resideInAPackage("..application..")
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage("java..", "..domain..", "..application..");

    @ArchTest
    static final ArchRule adaptadorDeEntradaNaoFalaComAdaptadorDeSaida =
            noClasses().that().resideInAPackage("..adapter.in..")
                    .should().dependOnClassesThat().resideInAPackage("..adapter.out..");

    @ArchTest
    static final ArchRule adaptadorDeSaidaNaoFalaComAdaptadorDeEntrada =
            noClasses().that().resideInAPackage("..adapter.out..")
                    .should().dependOnClassesThat().resideInAPackage("..adapter.in..");

    @ArchTest
    static final ArchRule adaptadorDeEntradaSoUsaPortasDeEntrada =
            noClasses().that().resideInAPackage("..adapter.in..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..application.service..", "..application.port.out..");
}
