dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation(project(":library"))
  implementation("com.squareup:kotlinpoet:1.7.2")
  implementation("com.squareup:kotlinpoet-metadata:1.7.2")
  implementation("com.squareup:kotlinpoet-metadata-specs:1.7.2")

  testImplementation("com.google.truth:truth:1.0.1")
  testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.3.5")
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
