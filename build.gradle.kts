plugins {
  kotlin("jvm") version "1.7.10"
  `maven-publish`
  signing
  id("org.jetbrains.dokka") version "1.5.31"
}

allprojects {
  repositories {
    mavenCentral()
  }
}

val ktlint: Configuration by configurations.creating

dependencies {
  ktlint("com.pinterest:ktlint:0.42.0")
}

val outputDir = "${project.buildDir}/reports/ktlint/"
val inputFiles = project.fileTree(mapOf("dir" to ".", "include" to "**/*.kt"))

val ktlintCheck by tasks.creating(JavaExec::class) {
  inputs.files(inputFiles)
  outputs.dir(outputDir)

  description = "Check Kotlin code style."
  classpath = ktlint
  group = "verification"
  main = "com.pinterest.ktlint.Main"
  args = listOf("**/*.kt")
}

val ktlintFormat by tasks.creating(JavaExec::class) {
  inputs.files(inputFiles)
  outputs.dir(outputDir)

  description = "Fix Kotlin code style deviations."
  classpath = ktlint
  group = "verification"
  main = "com.pinterest.ktlint.Main"
  args = listOf("-F", "**/*.kt")
}

subprojects {
  apply(plugin = "kotlin")
  apply(plugin = "maven-publish")
  apply(plugin = "org.jetbrains.dokka")
  apply(plugin = "signing")

  tasks {
    compileKotlin {
      kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
      kotlinOptions.jvmTarget = "1.8"
    }
  }

  val dokkaJar by tasks.creating(org.gradle.jvm.tasks.Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    archiveClassifier.set("javadoc")
    from(tasks.dokkaHtml)
  }

  val sourcesJar by tasks.creating(org.gradle.jvm.tasks.Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
  }

  val pomArtifactId: String? by project
  if (pomArtifactId != null) {
    publishing {
      publications {
        create<MavenPublication>("maven") {
          val versionName: String by project
          val pomGroupId: String by project
          groupId = pomGroupId
          artifactId = pomArtifactId
          version = versionName
          from(components["java"])

          artifact(dokkaJar)
          artifact(sourcesJar)

          pom {
            val pomDescription: String by project
            val pomUrl: String by project
            val pomName: String by project
            description.set(pomDescription)
            url.set(pomUrl)
            name.set(pomName)
            scm {
              val pomScmUrl: String by project
              val pomScmConnection: String by project
              val pomScmDevConnection: String by project
              url.set(pomScmUrl)
              connection.set(pomScmConnection)
              developerConnection.set(pomScmDevConnection)
            }
            licenses {
              license {
                val pomLicenseName: String by project
                val pomLicenseUrl: String by project
                val pomLicenseDist: String by project
                name.set(pomLicenseName)
                url.set(pomLicenseUrl)
                distribution.set(pomLicenseDist)
              }
            }
            developers {
              developer {
                val pomDeveloperId: String by project
                val pomDeveloperName: String by project
                id.set(pomDeveloperId)
                name.set(pomDeveloperName)
              }
            }
          }
        }
      }
      signing {
        sign(publishing.publications["maven"])
      }
      repositories {
        maven {
          val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
          val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
          val versionName: String by project
          url = if (versionName.endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
          credentials {
            username = project.property("NEXUS_USERNAME")?.toString()
            password = project.property("NEXUS_PASSWORD")?.toString()
          }
        }
      }
    }
  }
}
