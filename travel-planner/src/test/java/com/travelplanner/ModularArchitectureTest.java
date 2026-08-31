package com.travelplanner;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.travelplanner", importOptions = ImportOption.DoNotIncludeTests.class)
public class ModularArchitectureTest {

    @ArchTest
    static final ArchRule tripModuleEncapsulation =
        noClasses().that().resideInAPackage("com.travelplanner.trip..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "com.travelplanner.finance.repository..",
                "com.travelplanner.finance.entity..",
                "com.travelplanner.auth.repository..",
                "com.travelplanner.auth.entity.."
            )
            .because("Module 'trip' must communicate via services, not repositories/entities of other modules")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule financeModuleEncapsulation =
        noClasses().that().resideInAPackage("com.travelplanner.finance..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "com.travelplanner.trip.repository..",
                "com.travelplanner.trip.entity..",
                "com.travelplanner.auth.repository..",
                "com.travelplanner.auth.entity.."
            )
            .because("Module 'finance' must communicate via services, not repositories/entities of other modules")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule itineraryModuleEncapsulation =
        noClasses().that().resideInAPackage("com.travelplanner.itinerary..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "com.travelplanner.finance.repository..",
                "com.travelplanner.finance.entity..",
                "com.travelplanner.auth.repository..",
                "com.travelplanner.auth.entity.."
            )
            .because("Module 'itinerary' must communicate via services, not repositories/entities of other modules")
            .allowEmptyShould(true);
}
