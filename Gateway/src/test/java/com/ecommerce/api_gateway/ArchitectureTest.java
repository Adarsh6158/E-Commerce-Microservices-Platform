package com.ecommerce.api_gateway;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.ecommerce.api_gateway")
public class ArchitectureTest {

    @ArchTest
    public static final ArchRule configLayerCannotDependOnFilterLayer =
            noClasses().that().resideInAPackage("..Config..")
                    .should().dependOnClassesThat().resideInAPackage("..Filter..");

    @ArchTest
    public static final ArchRule filterLayerCannotDependOnConfigImpl =
            noClasses().that().resideInAPackage("..Filter..")
                    .should().dependOnClassesThat().resideInAPackage("..Config.impl..");

    @ArchTest
    public static final ArchRule filtersMustResideInFilterPackage =
            classes().that().implement("org.springframework.cloud.gateway.filter.GlobalFilter")
                    .should().resideInAPackage("..Filter..");
}
