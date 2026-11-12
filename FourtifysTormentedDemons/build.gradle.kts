plugins {
    id("java")
}

group = "com.fourtifys.tormenteddemons"
version = "1.2.0"

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.runelite.net")
        content {
            includeGroupByRegex("net\\.runelite.*")
        }
    }
    mavenCentral()
}

val vitaliteClasspath = providers.gradleProperty("vitaliteClasspath")
    .orElse(providers.environmentVariable("VITALITE_CLASSPATH"))
    .orNull
val vitalite: List<Any> = if (vitaliteClasspath != null) {
    listOf(files(vitaliteClasspath.split(File.pathSeparator).filter { it.isNotBlank() }))
} else {
    listOf("net.runelite:client", "com.tonic:base-api", "com.tonic:api").map { "$it:latest.release" }
}
val lombok = "org.projectlombok:lombok:1.18.24"

dependencies {
    vitalite.forEach {
        compileOnly(it)
        testImplementation(it)
    }
    compileOnly(lombok)
    annotationProcessor(lombok)
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(11)
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("previewOverlay") {
    dependsOn(tasks.testClasses)
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("com.fourtifys.tormenteddemons.TormentedDemonsOverlayPreview")
    args(providers.gradleProperty("overlayOutput").getOrElse("build/overlay-preview.png"))
}
