dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation(project(":library"))
  implementation("com.squareup:kotlinpoet:1.6.0")
  implementation("com.squareup:kotlinpoet-metadata:1.6.0")
  implementation("com.squareup:kotlinpoet-metadata-specs:1.6.0")

  testImplementation("com.google.truth:truth:1.0.1")
  testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.2.9")
}

tasks {
  compileKotlin {
    kotlinOptions.freeCompilerArgs += listOf("-module-name", "vanilla-processor")
    kotlinOptions.freeCompilerArgs += "-Xopt-in=com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview"
  }
  compileTestKotlin {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview"
  }
}
