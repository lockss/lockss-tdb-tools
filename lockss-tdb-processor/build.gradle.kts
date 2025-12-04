/*
 * LOCKSS TDB Processor
 *
 * Processes LOCKSS TDB files for ANTLR parsing and XML generation.
 */

plugins {
    id("lockss-java-conventions")
    application
}

version = "1.9.0-SNAPSHOT"
description = "LOCKSS TDB Processor"

val antlrVersion = "4.9.3"

application {
    mainClass.set("org.lockss.tdb.TdbXml")
}

// ANTLR tool configuration for code generation
val antlrTool: Configuration by configurations.creating

dependencies {
    // ANTLR tool for code generation
    antlrTool("org.antlr:antlr4:$antlrVersion")

    // ANTLR runtime
    implementation("org.antlr:antlr4-runtime:$antlrVersion")

    // Commons
    api(libs.commons.cli)
    api(libs.commons.io)
    api(libs.commons.text)

    // Test dependencies
    testImplementation(platform(project(":lockss-pom-bundles:lockss-junit5-bundle")))
    testImplementation(libs.junit.jupiter.engine)
}

// Custom ANTLR generation tasks to handle lexer/parser dependencies
val antlrSourceDir = file("src/main/antlr4/org/lockss/tdb")
val antlrOutputBase = file("${project.layout.buildDirectory.get()}/generated-src/antlr/main")
val antlrOutputDir = file("$antlrOutputBase/org/lockss/tdb")

// Task to generate lexers first (they produce .tokens files needed by parsers)
val generateAntlrLexers by tasks.registering(JavaExec::class) {
    group = "build"
    description = "Generate ANTLR lexer sources"

    mainClass.set("org.antlr.v4.Tool")
    classpath = antlrTool

    inputs.files(fileTree(antlrSourceDir) { include("*Lexer.g4") })
    outputs.dir(antlrOutputDir)

    doFirst {
        antlrOutputDir.mkdirs()
    }

    args = listOf(
        "-visitor",
        "-long-messages",
        "-package", "org.lockss.tdb",
        "-o", antlrOutputDir.absolutePath,
        "$antlrSourceDir/TdbLexer.g4",
        "$antlrSourceDir/TdbQueryLexer.g4"
    )
}

// Task to generate parsers (depends on lexers for .tokens files)
val generateAntlrParsers by tasks.registering(JavaExec::class) {
    group = "build"
    description = "Generate ANTLR parser sources"
    dependsOn(generateAntlrLexers)

    mainClass.set("org.antlr.v4.Tool")
    classpath = antlrTool

    inputs.files(fileTree(antlrSourceDir) { include("*Parser.g4") })
    inputs.dir(antlrOutputDir) // Depends on tokens files from lexers
    outputs.dir(antlrOutputDir)

    // -lib points to where tokens files are located
    args = listOf(
        "-visitor",
        "-long-messages",
        "-package", "org.lockss.tdb",
        "-lib", antlrOutputDir.absolutePath,
        "-o", antlrOutputDir.absolutePath,
        "$antlrSourceDir/TdbParser.g4",
        "$antlrSourceDir/TdbQueryParser.g4"
    )
}

// Combined task for convenience
val generateAntlrSources by tasks.registering {
    group = "build"
    description = "Generate all ANTLR sources"
    dependsOn(generateAntlrLexers, generateAntlrParsers)
}

// Add generated ANTLR sources to Java source sets
sourceSets.main {
    java {
        srcDir(antlrOutputDir.parentFile) // Point to build/generated-src/antlr/main
    }
}

tasks.named("compileJava") {
    dependsOn(generateAntlrSources)
}

// Ensure ANTLR sources are generated before sourcesJar
tasks.named("sourcesJar") {
    dependsOn(generateAntlrSources)
}
