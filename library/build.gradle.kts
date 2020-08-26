dependencies {
  implementation(kotlin("stdlib-jdk8"))
  api(project(":annotations"))

  testImplementation("com.google.truth:truth:1.0.1")
}

tasks {
  compileKotlin {
    kotlinOptions.freeCompilerArgs += listOf("-module-name", "vanilla-library")
  }
}
