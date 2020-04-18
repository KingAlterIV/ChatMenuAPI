plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "5.2.0"
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
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = sourceCompatibility
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

publishing {
    publications {
        create<MavenPublication>("default") {
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Minevictus/ChatMenuAPI/")
            credentials {
                username = System.getenv("GITHUB_ACTOR") // provided by actions
                password = System.getenv("GITHUB_TOKEN") // provided by actions
            }
        }
    }
}