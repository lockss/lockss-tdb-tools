/*
 * LOCKSS TDB Processor
 *
 * Processes LOCKSS TDB files for ANTLR parsing and XML generation.
 */

plugins {
    id("lockss-java-conventions")
    antlr
    application
}

version = "1.9.0-SNAPSHOT"
description = "LOCKSS TDB Processor"

val antlrVersion = "4.9.3"

application {
    mainClass.set("org.lockss.tdb.TdbXml")
}

dependencies {
    // ANTLR runtime
    antlr("org.antlr:antlr4:$antlrVersion")
    implementation("org.antlr:antlr4-runtime:$antlrVersion")

    // Commons
    api(libs.commons.cli)
    api(libs.commons.io)
    api(libs.commons.text)

    // Test dependencies
    testImplementation(platform(project(":lockss-pom-bundles:lockss-junit5-bundle")))
    testImplementation(libs.junit.jupiter.engine)
}

tasks.generateGrammarSource {
    maxHeapSize = "64m"
    arguments = arguments + listOf("-visitor", "-long-messages")
    outputDirectory = file("${project.layout.buildDirectory.get()}/generated-src/antlr/main/org/lockss/tdb")
}

// Add generated ANTLR sources to source sets
sourceSets {
    main {
        java {
            srcDir(tasks.generateGrammarSource)
        }
    }
}

tasks.named("compileJava") {
    dependsOn(tasks.generateGrammarSource)
}
