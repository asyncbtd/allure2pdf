plugins {
    id("java")
    id("maven-publish")
    id("org.jreleaser") version "1.19.0"
}

version = "0.0.0"

allprojects {
    group = "io.github.asyncbtd.allure2pdf"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")

    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.38")
        annotationProcessor("org.projectlombok:lombok:1.18.38")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}

configure(subprojects.filter { it.name in listOf("common", "gradle-plugin", "maven-plugin") }) {
    apply(plugin = "maven-publish")

    java {
        withJavadocJar()
        withSourcesJar()
    }

    publishing {
        repositories {
            maven {
                name = "staging"
                url = uri(rootProject.layout.buildDirectory.dir("staging-deploy"))
            }
        }

        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])

                pom {
                    name.set("allure2pdf")
                    description.set("Converts html allure reports into more easily distributed pdf reports")
                    url.set("https://github.com/asyncbtd/allure2pdf")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    developers {
                        developer {
                            name.set("Timofey Brukhanchik")
                            email.set("asyncbtd@gmail.com")
                        }
                    }

                    scm {
                        connection.set("scm:git:ssh://github.com/asyncbtd/allure2pdf.git")
                        developerConnection.set("scm:git:ssh://github.com:asyncbtd/allure2pdf.git")
                        url.set("https://github.com/asyncbtd/allure2pdf")
                    }
                }
            }
        }
    }
}

val centralPortalUsername: String? by project
val centralPortalPassword: String? by project

val jreleaserSigningPublicPath: String? by project
val jreleaserSigningSecretPath: String? by project
val jreleaserSigningPassphrase: String? by project

val jreleaserGithubToken: String? by project

jreleaser {
    signing {
        active = org.jreleaser.model.Active.ALWAYS
        armored = true
        mode = org.jreleaser.model.Signing.Mode.FILE
        publicKey = jreleaserSigningPublicPath
        secretKey = jreleaserSigningSecretPath
        passphrase = jreleaserSigningPassphrase
    }

    project {
        inceptionYear = "2025"
        author("Timofey Brukhanchik")
    }
    
    release {
        github {
            token.set(jreleaserGithubToken)
            repoOwner.set("asyncbtd")
            name.set("allure2pdf")
            tagName.set("v{{projectVersion}}")
            releaseName.set("Release {{projectVersion}}")
            draft.set(false)
            prerelease {
                enabled.set(false)
            }
            discussionCategoryName.set("Announcements")
            skipTag.set(false)
            skipRelease.set(false)
            overwrite.set(false)
            update {
                enabled.set(false)
            }
            sign.set(true)
            
            changelog {
                formatted.set(org.jreleaser.model.Active.ALWAYS)
                preset.set("conventional-commits")
                contributors {
                    enabled.set(true)
                }
            }
        }
    }
    
    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    active = org.jreleaser.model.Active.ALWAYS
                    url = "https://central.sonatype.com/api/v1/publisher?name=allure2pdf&publishingType=USER_MANAGED"
                    stagingRepository(layout.buildDirectory.dir("staging-deploy").get().toString())
                    setAuthorization("Basic")
                    username = centralPortalUsername
                    password = centralPortalPassword
                }
            }
        }
    }
}
