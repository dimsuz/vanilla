plugins {
  kotlin("jvm")
}

group = "ru.dimsuz.vanilla.processor"
version = "1.0-SNAPSHOT"

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation(project(":annotations"))
}

tasks {
  compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
  }
  compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
  }
}
