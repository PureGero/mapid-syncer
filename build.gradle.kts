plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.11"
}

dependencies {
    paperweight.paperDevBundle("1.21.5-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.withType<JavaCompile> {
    options.release = 21
}

tasks.processResources {
    expand(
        "group" to project.group,
        "version" to project.version
    )
}