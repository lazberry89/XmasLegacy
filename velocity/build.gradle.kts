
plugins {
    id("java-library")
    id("xyz.jpenilla.run-velocity")
    id("io.github.goooler.shadow") version "8.1.8"
}

dependencies {
    implementation(project(":common"))
    implementation("redis.clients:jedis:8.0.2")

    val velocityVersion = "3.5.0-SNAPSHOT"
    compileOnly("com.velocitypowered:velocity-api:$velocityVersion")
    annotationProcessor("com.velocitypowered:velocity-api:$velocityVersion")

    compileOnly("com.google.inject:guice:5.1.0")
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
