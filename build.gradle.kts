plugins {
    java
    application
    id("org.graalvm.buildtools.native") version "1.1.4"
    id("com.gradleup.shadow") version "9.5.1"
}

group = "dev.danvega"
version = "0.3.1"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

application {
    mainClass.set("dev.danvega.initializr.SpringInitializrTui")
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://central.sonatype.com/repository/maven-snapshots/")
        mavenContent {
            snapshotsOnly()
        }
    }
}

val tambouiVersion = "0.2.0-SNAPSHOT"
val jacksonVersion = "3.0.4"
val junitVersion = "6.0.3"
val assertjVersion = "3.27.7"

dependencies {
    implementation("dev.tamboui:tamboui-toolkit:$tambouiVersion")
    implementation("dev.tamboui:tamboui-jline3-backend:$tambouiVersion")
    implementation("dev.tamboui:tamboui-css:$tambouiVersion")
    implementation("tools.jackson.core:jackson-databind:$jacksonVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("org.assertj:assertj-core:$assertjVersion")
}

graalvmNative {
    binaries {
        named("main") {
            imageName.set("spring-initializr-tui")
            buildArgs.add("--initialize-at-build-time=dev.tamboui")
            buildArgs.add("--enable-preview")
            buildArgs.add("-H:+ReportExceptionStackTraces")
        }
    }
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = application.mainClass
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("--enable-preview")
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("--enable-preview")
}

tasks.withType<JavaExec> {
    jvmArgs("--enable-preview")
}

tasks.named<Copy>("processResources") {
    filesMatching("application.properties") {
        expand(mapOf(
            "project" to mapOf(
                "version" to project.version
            )
        ))
    }
}

val osName: String = System.getProperty("os.name").lowercase()
val homeDir: File = file(System.getProperty("user.home"))
val installBinDir: File = file("$homeDir/.local/bin")

tasks.register("installLocalNative") {
    dependsOn("nativeCompile")
    description = "Install native binary to $installBinDir"
    group = "distribution"
    doLast {
        installBinDir.mkdirs()
        val exeSourceName = if (osName.contains("win")) "spring-initializr-tui.exe" else "spring-initializr-tui"
        val exeSource = layout.buildDirectory.file("native/nativeCompile/$exeSourceName").get().asFile
        val exeTargetName = if (osName.contains("win")) "spring.exe" else "spring"
        val exeTarget = file("$installBinDir/$exeTargetName")
        exeSource.copyTo(exeTarget, overwrite = true)
        exeTarget.setExecutable(/*executable = */true, /*ownerOnly = */false)
        logger.lifecycle("Native binary installed: {}", exeTarget.absolutePath)
    }
}
