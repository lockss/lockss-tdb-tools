/*
 * LOCKSS TDB XML Gradle Plugin
 *
 * Gradle plugin to convert .tdb files to .xml format.
 * This is the Gradle equivalent of the lockss-tdbxml-maven-plugin.
 */

plugins {
    `java-gradle-plugin`
    `maven-publish`
}

group = "org.lockss.laaws"
version = "1.10.0-SNAPSHOT"
description = "Convert .tdb to .xml Gradle Plugin"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    // Internal dependencies
    implementation(project(":lockss-tdb-tools:lockss-tdb-processor"))

    // Test dependencies
    testImplementation(platform(project(":lockss-pom-bundles:lockss-junit4-bundle")))
    testImplementation(libs.junit4)
    testImplementation(libs.mockito.core)
}

gradlePlugin {
    plugins {
        create("tdbxml") {
            id = "org.lockss.tdbxml"
            implementationClass = "org.lockss.tdb.TdbXmlPlugin"
            displayName = "LOCKSS TDB XML Plugin"
            description = "Converts .tdb files to .xml format"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name.set("LOCKSS TDB XML Gradle Plugin")
                description.set("Gradle plugin to convert .tdb files to .xml format")
                url.set("https://www.lockss.org/")
            }
        }
    }
}
