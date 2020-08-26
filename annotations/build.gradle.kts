dependencies {
  implementation(kotlin("stdlib-jdk8"))
}

tasks {
  compileKotlin {
    kotlinOptions.freeCompilerArgs += listOf("-module-name", "vanilla-annotations")
  }
}
