plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
}

dependencies {
    paperweight.paperDevBundle(providers.gradleProperty("minecraftVersion"))
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