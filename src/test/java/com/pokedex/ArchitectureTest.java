package com.pokedex;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Guards the Clean Architecture dependency rule: dependencies point inwards only.
 * presentation / infrastructure -> application -> domain
 */
@AnalyzeClasses(packages = "com.pokedex", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule domain_must_be_pure_java = noClasses()
            .that().resideInAPackage("com.pokedex.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta.persistence..",
                    "com.pokedex.application..", "com.pokedex.infrastructure..", "com.pokedex.presentation..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule application_must_not_depend_on_frameworks_or_outer_layers = noClasses()
            .that().resideInAPackage("com.pokedex.application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta.persistence..",
                    "com.pokedex.infrastructure..", "com.pokedex.presentation..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule presentation_must_not_depend_on_infrastructure = noClasses()
            .that().resideInAPackage("com.pokedex.presentation..")
            .should().dependOnClassesThat().resideInAPackage("com.pokedex.infrastructure..")
            .allowEmptyShould(true);
}
