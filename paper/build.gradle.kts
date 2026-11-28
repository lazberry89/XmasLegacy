@file:Suppress("VulnerableLibrariesLocal")

plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("io.github.goooler.shadow") version "8.1.8"
}

dependencies {
    implementation(project(":common"))
    implementation("redis.clients:jedis:8.0.2")
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("net.kyori:adventure-text-minimessage:4.18.0")
    compileOnly("net.kyori:adventure-api:4.18.0")
    implementation("net.kyori:adventure-text-serializer-legacy:4.18.0")
    compileOnly("org.geysermc.floodgate:api:2.2.5-SNAPSHOT")
    compileOnly("org.geysermc.geyser:api:2.4.2-SNAPSHOT")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.0")
    compileOnly("io.th0rgal:oraxen:1.213.0")
    compileOnly("com.frengor:ultimateadvancementapi:2.8.2")
    compileOnly("net.luckperms:api:5.5")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks {
    test {
        useJUnitPlatform()
    }

    runServer {
        minecraftVersion("1.21.11")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    shadowJar {
        archiveFileName.set("XmasLegacy.jar")
    }

    build {
        dependsOn(shadowJar)
    }
}