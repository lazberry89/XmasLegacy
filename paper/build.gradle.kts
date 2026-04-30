plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("io.github.goooler.shadow") version "8.1.8"
}

dependencies {
    implementation(project(":common"))
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("org.geysermc.geyser:api:2.9.5-SNAPSHOT")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.21.11")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    shadowJar {
        // 빌드된 파일 이름 뒤에 -all이 붙지 않도록 설정
        archiveClassifier.set("")
    }

    build {
        // build 태스크 실행 시 기본 jar 대신 shadowJar가 실행되게 덮어씌움
        dependsOn(shadowJar)
    }
}
