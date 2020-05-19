plugins {
    java
    kotlin("jvm") version "1.3.72"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("net.dv8tion:JDA:4.1.1_153")
    implementation("org.apache.logging.log4j", "log4j-api", "2.13.3")
    implementation("org.apache.logging.log4j", "log4j-core", "2.13.3")
    implementation("org.apache.logging.log4j", "log4j-slf4j-impl", "2.13.3")
    implementation("org.mozilla:rhino:1.7.12")
    testImplementation("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    jar {
        manifest {
            attributes["Main-Class"] = "BotKt"
        }
        from(configurations.compileClasspath.map { config -> config.filter { it.extension != "pom" }.map { if (it.isDirectory) it else zipTree(it) } })
    }
}