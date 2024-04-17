import java.net.URI

plugins {
    id("idea")
    id("java")
    id("maven-publish")
    id("com.google.protobuf") version "0.9.4"
    id("com.github.ben-manes.versions") version "0.49.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.tencent.bk.sdk"
version = "1.0-SNAPSHOT"

sourceCompatibility = JavaVersion.VERSION_1_8
targetCompatibility = JavaVersion.VERSION_1_8

repositories {
    google()
    mavenCentral()
}

dependencies {

    implementation "io.grpc:grpc-netty-shaded:$grpc_version"
    implementation "io.grpc:grpc-protobuf:$grpc_version"
    implementation "io.grpc:grpc-stub:$grpc_version"
    implementation "io.grpc:grpc-services:$grpc_version"

    compileOnly "javax.annotation:javax.annotation-api:$annotation_version"

    implementation "ch.qos.logback:logback-classic:$logback_version"
    // https://mvnrepository.com/artifact/commons-io/commons-io
    implementation "commons-io:commons-io:$commons_io_version"
    implementation "com.fasterxml.jackson.core:jackson-databind:$jackson_version"
    // https://mvnrepository.com/artifact/com.fasterxml.jackson.dataformat/jackson-dataformat-protobuf
    implementation "com.fasterxml.jackson.dataformat:jackson-dataformat-protobuf:$jackson_version"
    // https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp
    implementation "com.squareup.okhttp3:okhttp:$okhttp3_version"
    implementation "com.squareup.okhttp3:okhttp-tls:$okhttp3_version"
    implementation "io.github.hakky54:sslcontext-kickstart:$sslcontext_version"
    implementation "io.prometheus:prometheus-metrics-core:$prometheus_version"
    implementation "io.prometheus:prometheus-metrics-instrumentation-jvm:$prometheus_version"
    implementation "io.prometheus:prometheus-metrics-exporter-httpserver:$prometheus_version"

    testImplementation "org.yaml:snakeyaml:$snakeYaml_version"
    testImplementation "io.grpc:grpc-testing:$grpc_version"
    testImplementation "org.mockito:mockito-core:$mockito_version"
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit_version")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junit_version")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junit_version")

}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protoc_version"
    }
    plugins {
        grpc {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpc_version"
        }
    }
    generateProtoTasks {
        all().each { task ->
            task.plugins {
                grpc {}     // Generate Java gRPC classes
            }
        }
    }
}
test {
    useJUnitPlatform()
}

// Required for multiple uberjar targets
shadowJar {
    mergeServiceFiles()
}

val sourceJar = tasks.register<Jar>("sourceJar") {
    archiveClassifier.set("sources")
    from(project.the<SourceSetContainer>()["main"].allSource)
}

val javadocJar = tasks.register<Jar>("javadocJar") {
    val javadoc = tasks.getByName("javadoc")
    dependsOn(javadoc)
    archiveClassifier.set("javadoc")
    from(javadoc)
}

tasks.getByName<Javadoc>("javadoc") {
    description = "javadoc for Tencentblueking/bscp-java-sdk"
    val options: StandardJavadocDocletOptions = options as StandardJavadocDocletOptions
    options.memberLevel = JavadocMemberLevel.PROTECTED
    options.header = project.name
    options.isAuthor = true
    options.isVersion = true
    // 不检查：非标的javadoc注解不报错
    options.addStringOption("Xdoclint:none", "-quiet")
    options.addStringOption("charset", "UTF-8")
    logging.captureStandardError(LogLevel.INFO)
    logging.captureStandardOutput(LogLevel.INFO)
    options.encoding = "UTF-8"
    options.charSet = "UTF-8"
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            artifact(sourceJar)
            artifact(javadocJar)

            pom {
                name.set("bscp-java-sdk")
                description.set("Tencent blueking ci project")
                url.set("https://github.com/Tencentblueking/bscp-java-sdk")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://github.com/Tencentblueking/bscp-java-sdk/blob/master/LICENSE.txt")
                        distribution.set("repo")
                        comments.set("A business-friendly OSS license")
                    }
                }

                developers {
                    developer {
                        name.set("bscp-java-sdk")
                        email.set("devops@tencent.com")
                        roles.set(listOf("Manager"))
                        url.set("https://bk.tencent.com")
                    }
                }

                scm {
                    url.set("https://github.com/Tencentblueking/bscp-java-sdk")
                    connection.set("scm:git:https://github.com/Tencentblueking/bscp-java-sdk.git")
                    developerConnection.set("scm:git:git@github.com:Tencentblueking/bscp-java-sdk.git")
                }
            }
        }
    }
    repositories {
        maven {
            name = "oss"

            // 正式包
            var mavenRepoDeployUrl = System.getProperty("mavenRepoDeployUrl")
            var mavenRepoUsername = System.getProperty("mavenRepoUsername")
            var mavenRepoPassword = System.getProperty("mavenRepoPassword")

            if (mavenRepoDeployUrl == null) {
                mavenRepoDeployUrl = System.getenv("build_mavenRepoDeployUrl")
            }

            if (mavenRepoUsername == null) {
                mavenRepoUsername = System.getenv("build_mavenRepoUsername")
            }

            if (mavenRepoPassword == null) {
                mavenRepoPassword = System.getenv("build_mavenRepoPassword")
            }

            if (mavenRepoDeployUrl == null) {
                mavenRepoDeployUrl = project.extra["MAVEN_REPO_DEPLOY_URL"]?.toString()
            }

            if (mavenRepoUsername == null) {
                mavenRepoUsername = project.extra["MAVEN_REPO_USERNAME"]?.toString()
            }

            if (mavenRepoPassword == null) {
                mavenRepoPassword = project.extra["MAVEN_REPO_PASSWORD"]?.toString()
            }

            // 快照包
            var snapshotMavenRepoDeployUrl = System.getProperty("snapshotMavenRepoDeployUrl")
            var snapshotMavenRepoUsername = System.getProperty("snapshotMavenRepoUsername")
            var snapshotMavenRepoPassword = System.getProperty("snapshotMavenRepoPassword")

            if (snapshotMavenRepoDeployUrl == null) {
                snapshotMavenRepoDeployUrl = System.getenv("build_snapshotMavenRepoDeployUrl")
            }

            if (snapshotMavenRepoUsername == null) {
                snapshotMavenRepoUsername = System.getenv("build_snapshotMavenRepoUsername")
            }

            if (snapshotMavenRepoPassword == null) {
                snapshotMavenRepoPassword = System.getenv("build_snapshotMavenRepoPassword")
            }

            if (snapshotMavenRepoDeployUrl == null) {
                snapshotMavenRepoDeployUrl = project.extra["MAVEN_REPO_SNAPSHOT_DEPLOY_URL"]?.toString()
            }

            if (snapshotMavenRepoUsername == null) {
                snapshotMavenRepoUsername = project.extra["MAVEN_REPO_SNAPSHOT_USERNAME"]?.toString()
            }

            if (snapshotMavenRepoPassword == null) {
                snapshotMavenRepoPassword = project.extra["MAVEN_REPO_SNAPSHOT_PASSWORD"]?.toString()
            }

            url = URI(if (System.getProperty("snapshot") == "true") snapshotMavenRepoDeployUrl else mavenRepoDeployUrl)
            credentials {
                username =
                    if (System.getProperty("snapshot") == "true") snapshotMavenRepoUsername else mavenRepoUsername
                password =
                    if (System.getProperty("snapshot") == "true") snapshotMavenRepoPassword else mavenRepoPassword
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

tasks.getByName("publish") {
    onlyIf {
        project.the<SourceSetContainer>()["main"].allSource.files.isNotEmpty()
    }
}

tasks.getByName("generateMetadataFileForMavenJavaPublication") {
    onlyIf {
        project.the<SourceSetContainer>()["main"].allSource.files.isNotEmpty()
    }
}

tasks.getByName("generatePomFileForMavenJavaPublication") {
    onlyIf {
        project.the<SourceSetContainer>()["main"].allSource.files.isNotEmpty()
    }
}

tasks.getByName("publishMavenJavaPublicationToOssRepository") {
    onlyIf {
        project.the<SourceSetContainer>()["main"].allSource.files.isNotEmpty()
    }
}

tasks.getByName("publishMavenJavaPublicationToMavenLocal") {
    onlyIf {
        project.the<SourceSetContainer>()["main"].allSource.files.isNotEmpty()
    }
}

tasks.getByName("publishToMavenLocal") {
    onlyIf {
        project.the<SourceSetContainer>()["main"].allSource.files.isNotEmpty()
    }
}

tasks.getByName("signMavenJavaPublication") {
    onlyIf {
        project.the<SourceSetContainer>()["main"].allSource.files.isNotEmpty()
    }
}

tasks.getByName<Upload>("uploadArchives") {
    var mavenRepoDeployUrl: String? = System.getProperty("mavenRepoDeployUrl")
    var mavenRepoUsername = System.getProperty("mavenRepoUsername")
    var mavenRepoPassword = System.getProperty("mavenRepoPassword")

    if (mavenRepoDeployUrl == null) {
        mavenRepoDeployUrl = System.getenv("mavenRepoDeployUrl")
    }

    if (mavenRepoUsername == null) {
        mavenRepoUsername = System.getenv("mavenRepoUsername")
    }

    if (mavenRepoPassword == null) {
        mavenRepoPassword = System.getenv("mavenRepoPassword")
    }

    if (mavenRepoDeployUrl == null) {
        mavenRepoDeployUrl = project.extra["MAVEN_REPO_DEPLOY_URL"]?.toString()
    }

    if (mavenRepoUsername == null) {
        mavenRepoUsername = project.extra["MAVEN_REPO_USERNAME"]?.toString()
    }

    if (mavenRepoPassword == null) {
        mavenRepoPassword = project.extra["MAVEN_REPO_PASSWORD"]?.toString()
    }

    // if snapshot repository is null
    var snapshotRepositoryUrl = project.extra["MAVEN_REPO_SNAPSHOT_DEPLOY_URL"]?.toString()
    var snapshotRepositoryUsername = project.extra["MAVEN_REPO_SNAPSHOT_USERNAME"]?.toString()
    var snapshotRepositoryPassword = project.extra["MAVEN_REPO_SNAPSHOT_PASSWORD"]?.toString()

    if (snapshotRepositoryUrl == null || snapshotRepositoryUrl.isEmpty()) {
        snapshotRepositoryUrl = mavenRepoDeployUrl
    }
    if (snapshotRepositoryUsername == null || snapshotRepositoryUsername.isEmpty()) {
        snapshotRepositoryUsername = mavenRepoUsername
    }
    if (snapshotRepositoryPassword == null || snapshotRepositoryPassword.isEmpty()) {
        snapshotRepositoryPassword = mavenRepoPassword
    }

    repositories.withGroovyBuilder {
        "mavenDeployer" {
            "repository"("url" to mavenRepoDeployUrl) {
                "authentication"("userName" to mavenRepoUsername, "password" to mavenRepoPassword)
            }

            "snapshotRepository"("url" to snapshotRepositoryUrl) {
                "authentication"("userName" to snapshotRepositoryUsername, "password" to snapshotRepositoryPassword)
            }
        }
    }

    onlyIf {
        project.the<SourceSetContainer>()["main"].allSource.files.isNotEmpty()
    }
}

tasks.getByName("install") {
    onlyIf {
        project.the<SourceSetContainer>()["main"].allSource.files.isNotEmpty()
    }
}