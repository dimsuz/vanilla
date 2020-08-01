plugins {
  kotlin("jvm")
}

group = "ru.dimsuz.vanilla"
version = "1.0-SNAPSHOT"

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  api(project(":annotations"))
}

tasks {
  compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
  }
  compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
  }
}
