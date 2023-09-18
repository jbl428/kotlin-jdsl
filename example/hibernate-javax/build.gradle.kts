plugins {
    alias(exampleLibs.plugins.kotlin.noarg)
    alias(exampleLibs.plugins.kotlin.allopen)
    alias(exampleLibs.plugins.kotlin.jpa)
}

dependencies {
    implementation(exampleLibs.test.hibernate5.core)
    implementation(exampleLibs.logback)
    implementation(projects.example)
    implementation(projects.jpqlDsl)
    implementation(projects.jpqlRender)
    implementation(projects.hibernateJavaxSupport)

    runtimeOnly(exampleLibs.test.h2)

    testFixturesImplementation(exampleLibs.test.hibernate5.core)
    testFixturesImplementation(projects.jpqlRender)
}

kotlin {
    jvmToolchain(8)
}

noArg {
    annotation("com.linecorp.kotlinjdsl.example.jpql.hibernate.javax.annotation.CompositeId")
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.Embeddable")
}

allOpen {
    annotation("com.linecorp.kotlinjdsl.example.jpql.hibernate.javax.annotation.CompositeId")
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.Embeddable")
}

kover {
    excludeInstrumentation {
        packages("org.hibernate.*")
    }
}

tasks.withType<PublishToMavenRepository>().configureEach { enabled = false }
tasks.withType<PublishToMavenLocal>().configureEach { enabled = false }
