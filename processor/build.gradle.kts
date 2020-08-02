plugins {
  kotlin("jvm")
}

group = "ru.dimsuz.vanilla.processor"
version = "1.0-SNAPSHOT"

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation(project(":annotations"))

  testImplementation("com.google.truth:truth:1.0.1")
  testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.2.9")
}

tasks {
  compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
  }
  compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
  }
}
