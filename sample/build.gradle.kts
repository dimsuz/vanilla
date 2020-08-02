plugins {
  application
  kotlin("jvm")
  kotlin("kapt")
}

repositories {
  mavenCentral()
}

application {
  mainClassName = "ru.dimsuz.vanilla.sample.MainKt"
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation(project(":library"))
  kapt(project(":processor"))
}

tasks {
  compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
  }
  compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
  }
}
