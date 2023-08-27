plugins {
  application
  id("com.google.devtools.ksp")
}

repositories {
  mavenCentral()
}

application {
  mainClass.set("ru.dimsuz.vanilla.sample.MainKt")
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation(project(":library"))
  ksp(project(":processor"))

  testImplementation("com.google.truth:truth:1.0.1")
}
