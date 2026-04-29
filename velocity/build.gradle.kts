
plugins {
    id("java-library")
    id("xyz.jpenilla.run-velocity")
    id("io.github.goooler.shadow") version "8.1.8"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(project(":common"))
    compileOnly("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks {
  runVelocity {
    // Configure the Velocity version for our task.
    // This is the only required configuration besides applying the plugin.
    // Your plugin's jar (or shadowJar if present) will be used automatically.
    velocityVersion("3.5.0-SNAPSHOT")

  }
//    shadowJar {
//        archiveClassifier.set("")
//    }
//
//    build {
//        dependsOn(shadowJar)
//    }


    processResources {
        val props = mapOf("version" to version )
        filesMatching("velocity-plugin.json") {
            expand(props)
        }
    }
}
