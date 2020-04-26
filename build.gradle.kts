import java.util.*

plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("com.jfrog.bintray") version "1.8.5"
}

group = "me.tom.sparse"
version = "1.1.1"

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
    compileOnly("org.apache.logging.log4j:log4j-api:2.13.2")
    compileOnly("org.apache.logging.log4j:log4j-core:2.13.2")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = sourceCompatibility
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

publishing.publications.create<MavenPublication> ("mavenJava") {
    from(components["java"])
}

bintray {
    user = System.getenv("BINTRAY_USER")
    key = System.getenv("BINTRAY_API_KEY")
    setPublications("mavenJava")

    with(pkg) {
        repo = "minevictus"
        userOrg = user
        name = "ChatMenuAPI"
        setLicenses("GPL-3.0")
        vcsUrl = "https://github.com/Minevictus/ChatMenuAPI.git"

        with(version) {
            name = project.version.toString()
            desc = "An API for making menus inside Minecraft's chat. $name"
            released = Date().toString()
        }
    }

    override = true // just in case
    publish = true
}

// Foot fungus // TODO (RMS): Eat this.
