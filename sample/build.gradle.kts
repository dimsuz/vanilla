plugins {
  application
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

  testImplementation("com.google.truth:truth:1.0.1")
}
