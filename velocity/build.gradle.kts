
plugins {
    id("java-library")
    id("xyz.jpenilla.run-velocity")
    id("io.github.goooler.shadow") version "8.1.8"
}

dependencies {
    implementation(project(":common"))

    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("org.geysermc.floodgate:api:2.2.5-SNAPSHOT")
    compileOnly("io.th0rgal:oraxen:1.213.0")
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
