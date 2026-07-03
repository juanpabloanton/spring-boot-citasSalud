package com.citassalud.architecture;

import com.citassalud.SpringBootCitasSaludApplication;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;

class ArquitecturaLimpiaTest {

    /**
     * Se importa desde el directorio real de clases compiladas (en lugar de
     * {@code importPackages}, que resuelve vía classpath scanning) porque el proyecto
     * vive bajo una ruta de Windows con espacios, lo que confunde la codificación de
     * URLs usada internamente por ArchUnit al escanear el classpath.
     */
    private static JavaClasses importarClasesDeProduccion() {
        try {
            Path directorioClasesPrincipales = Path.of(
                    SpringBootCitasSaludApplication.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return new ClassFileImporter().importPath(directorioClasesPrincipales);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("No se pudo resolver el directorio de clases compiladas", e);
        }
    }

    @Test
    void elDominioNoDependeDeFrameworksNiDeCapasExternas() {
        var clases = importarClasesDeProduccion();

        ArchRuleDefinition.noClasses()
                .that().resideInAPackage("com.citassalud.domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "com.citassalud.infrastructure..",
                        "com.citassalud.interfaces..")
                .check(clases);
    }

    @Test
    void laAplicacionNoDependeDeInfraestructuraNiDeInterfaces() {
        var clases = importarClasesDeProduccion();

        ArchRuleDefinition.noClasses()
                .that().resideInAPackage("com.citassalud.application..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "com.citassalud.infrastructure..",
                        "com.citassalud.interfaces..")
                .check(clases);
    }
}
