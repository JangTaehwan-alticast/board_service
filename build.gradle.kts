import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.3.2.RELEASE"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.5.31"
	kotlin("plugin.spring") version "1.5.31"
}

group = "com.msp"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11


val springBootVersion: String by extra
//var springCloudVersion: String by extra
//val springProfile: String by project
//val dockerPluginVersion: String by extra
val gsonVersion: String by extra
var kotlinLoggingVersion: String by extra
//val okhttp4: String by project

//val jdkVersion: String by extra
//val jUnitVersion: String by extra
//val dockerEcrVersion: String by extra
//val kotlinVersion: String by extra
//var kotlinxCoroutinesReactorVersion: String by extra



repositories {
	mavenCentral()
}

dependencies {
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

	//spring boot version 문제인지..
//	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
//	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

	//parking-service 참고
	implementation("com.google.code.gson:gson:${gsonVersion}")
	implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")



	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.boot:spring-boot-dependencies:${springBootVersion}")
//		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}")
	}
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}


