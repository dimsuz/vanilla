dependencies {
  implementation(kotlin("stdlib-jdk8"))
  api("com.michael-bull.kotlin-result:kotlin-result:1.1.12")

  testImplementation("com.google.truth:truth:1.0.1")
}

tasks {
  compileKotlin {
    kotlinOptions.moduleName = "vanilla-library"
  }
}
