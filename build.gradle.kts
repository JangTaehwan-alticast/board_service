import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
	id("org.springframework.boot") version "2.3.6.RELEASE"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"

	base
	kotlin("jvm") version "1.3.70"
	kotlin("plugin.spring") version "1.3.70"
	id("org.jetbrains.kotlin.kapt") version "1.3.70"

	id("com.bmuschko.docker-remote-api") version "6.1.3"
	id("com.bmuschko.docker-spring-boot-application") version "6.1.3"

}

apply(plugin = "org.jetbrains.kotlin.plugin.spring")
apply(plugin = "org.springframework.boot")
apply(plugin = "io.spring.dependency-management")
apply(plugin = "com.bmuschko.docker-remote-api")
apply(plugin = "com.bmuschko.docker-spring-boot-application")

buildscript {
	repositories {
		gradlePluginPortal()
		mavenCentral()
		maven { url = uri("https://repo.spring.io/plugins-snapshot") }
		maven { url = uri("https://plugins.gradle.org/m2/") }
		maven { url = uri("https://repo.spring.io/milestone") }
	}

	dependencies {
		classpath("io.spring.gradle:dependency-management-plugin:1.0.11.RELEASE")
	}

	val dockerPluginVersion: String by extra
	dependencies {
		classpath("com.bmuschko:gradle-docker-plugin:6.1.3")
	}
}


group = "com.msp"
version = System.getenv("APP_VERSION") ?: "0.0.1-SNAPSHOT"
var registry = System.getenv("APP_REGISTRY") ?: ""
if (registry.isNotEmpty()) registry += "/"

java.sourceCompatibility = JavaVersion.VERSION_11


val springBootVersion: String by extra
val springCloudVersion: String by extra
val kotlinVersion: String by extra
val kotlinxCoroutinesReactorVersion: String by extra

repositories {
	gradlePluginPortal()
	mavenCentral()
	maven { url = uri("https://repo.spring.io/plugins-snapshot") }
	maven { url = uri("https://plugins.gradle.org/m2/") }
	maven { url = uri("https://repo.spring.io/milestone") }
}

dependencies {
	implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
	implementation("org.jetbrains.kotlin:kotlin-stdlib:${kotlinVersion}")
	implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${kotlinVersion}")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.3.2")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

	implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-aop")
	implementation("org.apache.commons:commons-lang3:3.12.0")

//	implementation("org.springframework.cloud:spring-cloud-config-client:$springCloudVersion")
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:2.2.5.RELEASE")
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-hystrix:2.2.7.RELEASE")


	//spring redis
	implementation("org.springframework.boot:spring-boot-starter-data-redis")

	testImplementation("org.springframework.boot:spring-boot-starter-test:2.3.6.RELEASE")

	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
	testImplementation("org.junit.jupiter:junit-jupiter-api")
	testImplementation("org.junit.jupiter:junit-jupiter-params")
}



tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict -Xemit-jvm-type-annotations")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

val jar: Jar by tasks
val bootJar: BootJar by tasks

bootJar.enabled = true
jar.enabled = false

tasks.bootJar {
	println("Start bootJar...")
	manifest {
		attributes["Title"] = "Mobility Service Platform"
		attributes["Module"] = project.name
		attributes["Built-By"] = System.getProperty("user.name")
		attributes["Created-By"] = "Gradle ${gradle.gradleVersion}"
		attributes["Build-JDK"] = "${System.getProperty("java.version")} ${System.getProperty("java.vendor")} ${System.getProperty("java.vm.version")}"
		attributes["Build-OS"] = "${System.getProperty("os.name")} ${System.getProperty("os.arch")} ${System.getProperty("os.version")}"
	}
}

val springProfile: String by project
val createDockerfile by tasks.creating(Dockerfile::class) {
	destFile.set(project.file("./build/docker/Dockerfile"))
	var javaOpts = ""
	var execJar = "${project.name}-${version}.jar"
	var profile = if(springProfile.isNullOrEmpty()) "default" else springProfile // gradle.properties에 정의됨
	var RAIDEA_PROFILE = profile

	from("openjdk:11-slim")
	exposePort(41100)
	workingDir("/opt/${project.name}/libs/")
	runCommand("pwd")
	copyFile ("./build/libs/${execJar}", "/opt/${project.name}/libs/${execJar}")
	runCommand("touch /opt/${project.name}/libs/${execJar}")

	environmentVariable("RAIDEA_PROFILE", "default")
	entryPoint ("sh", "-c", "java ${javaOpts} -Dspring.profiles.active=${RAIDEA_PROFILE} -Djava.security.egd=file:/dev/./urandom -jar ${execJar}")
}

tasks.create("buildDockerImage", DockerBuildImage::class) {
	println("Start buildDockerImage...")
	dependsOn(createDockerfile)
	dockerFile.set(createDockerfile.destFile)
	println("Set created Dockerfile...")
	inputDir.set(project.projectDir)
	println("Set Input ProjectDir...${project.projectDir}")
	println("image name : ${registry}${project.name}:${version}")
	println("image name : ${registry}${project.name}:latest")
	images.set(setOf("${registry}${project.name}:${version}", "${registry}${project.name}:latest"))
}

