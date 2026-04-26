plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("com.github.johnrengelman.shadow")
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.opencollab.dev/main/")          // releases
    maven("https://repo.opencollab.dev/maven-snapshots/") // snapshots
    maven("https://repo.opencollab.dev/#/main")
    maven("https://repo.opencollab.dev/#/maven-snapshots/org/geysermc")
    maven {
        name = "opencollabRepositoryMavenSnapshots"
        url = uri("https://repo.opencollab.dev/maven-snapshots")
    }
}



dependencies {
    implementation(project(":common"))
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    //compileOnly("org.geysermc.floodgate:api:2.2.3-SNAPSHOT")
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
        // build 태스크 실행 시 자동으로 shadowJar가 실행되게 합니다.
        dependsOn(shadowJar)
    }

    processResources {
        // 1. 중복 파일 처리 전략 명시 (ShadowJar 사용 시 필수)
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        val props = mapOf("version" to project.version)
        inputs.properties(props)

        filesMatching("plugin.yml") {
            // expand(props) 대신 아래 방식을 사용하면 'mode' 에러가 절대 나지 않습니다.
            filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to mapOf("version" to project.version.toString()))
        }
    }
}
