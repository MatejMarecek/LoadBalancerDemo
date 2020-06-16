import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.moowork.gradle.node.npm.NpmTask

plugins {
	id("org.springframework.boot") version "2.3.0.RELEASE"
	id("io.spring.dependency-management") version "1.0.9.RELEASE"
	id("com.github.node-gradle.node") version "2.2.2"
	kotlin("jvm") version "1.3.72"
	kotlin("plugin.spring") version "1.3.72"
}

group = "matejmarecek.demo"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-websocket")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.0")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	dependsOn("copyFrontend")
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}


tasks.register<NpmTask>("appNpmInstall") {
	description = "Installs all dependencies from package.json"
	workingDir = file("${project.projectDir}/src/main/frontend")
	args = listOf("install")
}

tasks.register<NpmTask>("appNpmBuild") {
	dependsOn("appNpmInstall")
	description = "Builds project"
	workingDir = file("${project.projectDir}/src/main/frontend")
	args = listOf("run", "build")
}

tasks.register<Copy>("copyFrontend") {
	dependsOn("appNpmBuild")
	description = "Copies built project to where it will be served"
	from("src/main/frontend/build")
	into("build/resources/main/static/.")
}

node {
	download = true
	version = "10.15.3"
	npmVersion = "6.4.1"

	// Set the work directory for unpacking node
	workDir = file("${project.buildDir}/nodejs")

	// Set the work directory for NPM
	npmWorkDir = file("${project.buildDir}/npm")
}