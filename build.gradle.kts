plugins {
    java
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "me.nahu"
version = "1.0.9"

repositories {
    mavenCentral()

    maven {
        name = "papermc"
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }

    maven {
        name = "aikar-repo"
        url = uri("https://repo.aikar.co/content/groups/aikar/")
    }

    maven {
        name = "minebench"
        url = uri("https://repo.minebench.de/")
    }

    maven {
        name = "proxi-nexus"
        url = uri("https://nexus.proximyst.com/repository/maven-public/")
    }

    maven {
        name = "dmulloy2-repo"
        url = uri("https://repo.dmulloy2.net/nexus/repository/public/")
    }
}

dependencies {
    compileOnly("com.destroystokyo.paper:paper-api:1.15-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.5.0")
    compileOnly("org.jetbrains:annotations:19.0.0")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = sourceCompatibility
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}