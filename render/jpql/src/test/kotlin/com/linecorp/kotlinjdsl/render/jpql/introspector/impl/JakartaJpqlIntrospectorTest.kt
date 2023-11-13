package com.linecorp.kotlinjdsl.render.jpql.introspector.impl

import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Test

class JakartaJpqlIntrospectorTest : WithAssertions {
    private val sut = JakartaJpqlIntrospector()

    companion object {
        private const val ENTITY_NAME_1 = "MyEntity1"
    }

    @Test
    fun `introspect() returns the name of the entity annotation, when the entity annotation has name`() {
        // given
        val type = EntityClass1::class

        // when
        val actual = sut.introspect(type)

        // then
        assertThat(actual?.name).isEqualTo(ENTITY_NAME_1)
    }

    @Test
    fun `introspect() returns the name of the class, when the entity annotation does not have name`() {
        // given
        val type = EntityClass2::class

        // when
        val actual = sut.introspect(type)

        // then
        assertThat(actual?.name).isEqualTo(EntityClass2::class.simpleName)
    }

    @Test
    fun `introspect() returns null, when there is no entity annotation`() {
        // given
        val type = NonEntityCLass1::class

        // when
        val actual = sut.introspect(type)

        // then
        assertThat(actual?.name).isNull()
    }

    @jakarta.persistence.Entity(name = ENTITY_NAME_1)
    private open class EntityClass1

    @jakarta.persistence.Entity
    private open class EntityClass2

    private open class NonEntityCLass1
}
