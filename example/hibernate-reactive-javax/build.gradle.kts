plugins {
    alias(exampleLibs.plugins.kotlin.noarg)
    alias(exampleLibs.plugins.kotlin.allopen)
    alias(exampleLibs.plugins.kotlin.jpa)
}

dependencies {
    implementation(exampleLibs.test.hibernate.reactive1.core)
    implementation(exampleLibs.test.vertx.jdbc.client)
    implementation(exampleLibs.test.agroal.pool)
    implementation(exampleLibs.logback)
    implementation(projects.example)
    implementation(projects.jpqlDsl)
    implementation(projects.jpqlRender)
    implementation(projects.hibernateReactiveJavaxSupport)

    runtimeOnly(exampleLibs.test.h2)

    testFixturesImplementation(exampleLibs.test.hibernate.reactive1.core)
    testFixturesImplementation(projects.jpqlRender)
}

kotlin {
    jvmToolchain(11)
}

noArg {
    annotation("com.linecorp.kotlinjdsl.example.hibernate.reactive.javax.jpql.entity.annotation.CompositeId")
}

allOpen {
    annotation("com.linecorp.kotlinjdsl.example.hibernate.reactive.javax.jpql.entity.annotation.CompositeId")
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
