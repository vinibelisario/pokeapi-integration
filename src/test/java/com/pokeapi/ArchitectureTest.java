package com.pokeapi;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.pokeapi", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule domainMustBePureJava = noClasses()
            .that().resideInAPackage("com.pokeapi.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta.persistence..",
                    "com.pokeapi.application..", "com.pokeapi.infrastructure..", "com.pokeapi.presentation..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule applicationMustNotDependOnFrameworksOrOuterLayers = noClasses()
            .that().resideInAPackage("com.pokeapi.application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta.persistence..",
                    "com.pokeapi.infrastructure..", "com.pokeapi.presentation..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule presentationMustNotDependOnInfrastructure = noClasses()
            .that().resideInAPackage("com.pokeapi.presentation..")
            .should().dependOnClassesThat().resideInAPackage("com.pokeapi.infrastructure..")
            .allowEmptyShould(true);
}
