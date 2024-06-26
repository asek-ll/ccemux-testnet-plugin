plugins {
    id("java")
}

group = "asek-ll.ccemux-testnet-plugin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "github"
        url = uri("https://maven.pkg.github.com/asek-ll/ccemux-fork")
        credentials {
            username = "asek-ll"
            password = System.getenv("GITHUB_TOKEN")
        }
    }

    exclusiveContent {
        forRepository {
            maven("https://squiddev.cc/maven")
        }
        filter {
            includeGroup("cc.tweaked")
        }
    }
}

val ccVersion: String by extra

dependencies {
    compileOnly("com.google.auto.service:auto-service:1.0.1")

    compileOnly("net.clgd:plugin-api:1.1.1-$ccVersion")
    implementation("cc.tweaked:cc-tweaked-1.20.1-core:$ccVersion")

    annotationProcessor("com.google.auto.service:auto-service:1.0.1")
}

tasks.test {
    useJUnitPlatform()
}
