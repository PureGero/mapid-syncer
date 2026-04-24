plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
}

dependencies {
    paperweight.paperDevBundle(providers.gradleProperty("paperVersion"))
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.withType<JavaCompile> {
    options.release = 25
}

tasks.processResources {
    expand(
        "group" to project.group,
        "version" to project.version
    )
}