package com.pokedex;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.pokedex", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule domainMustBePureJava = noClasses()
            .that().resideInAPackage("com.pokedex.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta.persistence..",
                    "com.pokedex.application..", "com.pokedex.infrastructure..", "com.pokedex.presentation..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule applicationMustNotDependOnFrameworksOrOuterLayers = noClasses()
            .that().resideInAPackage("com.pokedex.application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta.persistence..",
                    "com.pokedex.infrastructure..", "com.pokedex.presentation..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule presentationMustNotDependOnInfrastructure = noClasses()
            .that().resideInAPackage("com.pokedex.presentation..")
            .should().dependOnClassesThat().resideInAPackage("com.pokedex.infrastructure..")
            .allowEmptyShould(true);
}
