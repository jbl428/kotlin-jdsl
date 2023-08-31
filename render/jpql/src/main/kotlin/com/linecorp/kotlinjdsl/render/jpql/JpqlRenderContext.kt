package com.linecorp.kotlinjdsl.render.jpql

import com.linecorp.kotlinjdsl.SinceJdsl
import com.linecorp.kotlinjdsl.clazz.ClassUtils
import com.linecorp.kotlinjdsl.render.RenderContext
import com.linecorp.kotlinjdsl.render.jpql.introspector.CombinedJpqlIntrospector
import com.linecorp.kotlinjdsl.render.jpql.introspector.JpqlIntrospector
import com.linecorp.kotlinjdsl.render.jpql.introspector.JpqlIntrospectorModifier
import com.linecorp.kotlinjdsl.render.jpql.introspector.JpqlRenderIntrospector
import com.linecorp.kotlinjdsl.render.jpql.introspector.impl.JakartaJpqlIntrospector
import com.linecorp.kotlinjdsl.render.jpql.introspector.impl.JavaxJpqlIntrospector
import com.linecorp.kotlinjdsl.render.jpql.serializer.JpqlRenderSerializer
import com.linecorp.kotlinjdsl.render.jpql.serializer.JpqlSerializer
import com.linecorp.kotlinjdsl.render.jpql.serializer.JpqlSerializerModifier
import com.linecorp.kotlinjdsl.render.jpql.serializer.impl.*

@SinceJdsl("3.0.0")
class JpqlRenderContext private constructor(
    private val modules: Iterable<JpqlRenderModule>,
) : RenderContext {
    private val introspectors: MutableList<JpqlIntrospector> = mutableListOf()
    private val introspectorModifiers: MutableList<JpqlIntrospectorModifier> = mutableListOf()

    private val serializers: MutableList<JpqlSerializer<*>> = mutableListOf()
    private val serializerModifiers: MutableList<JpqlSerializerModifier> = mutableListOf()

    private val delegate: RenderContext

    @SinceJdsl("3.0.0")
    constructor() : this(listOf(DefaultModule()))

    init {
        modules.forEach { setup(it) }
        delegate = createRenderContext()
    }

    private fun setup(module: JpqlRenderModule) {
        val setupContext = object : JpqlRenderModule.SetupContext {
            override fun addSerializer(serializer: JpqlSerializer<*>) {
                serializers.add(serializer)
            }

            override fun addAllSerializer(vararg serializers: JpqlSerializer<*>) {
                this@JpqlRenderContext.serializers.addAll(serializers)
            }

            override fun addAllSerializer(serializers: Iterable<JpqlSerializer<*>>) {
                this@JpqlRenderContext.serializers.addAll(serializers)
            }

            override fun prependSerializerModifier(serializerModifier: JpqlSerializerModifier) {
                serializerModifiers.add(0, serializerModifier)
            }

            override fun appendSerializerModifier(serializerModifier: JpqlSerializerModifier) {
                serializerModifiers.add(serializerModifier)
            }

            override fun prependIntrospector(introspector: JpqlIntrospector) {
                introspectors.add(0, introspector)
            }

            override fun appendIntrospector(introspector: JpqlIntrospector) {
                introspectors.add(introspector)
            }

            override fun prependIntrospectorModifier(introspectorModifier: JpqlIntrospectorModifier) {
                introspectorModifiers.add(0, introspectorModifier)
            }

            override fun appendIntrospectorModifier(introspectorModifier: JpqlIntrospectorModifier) {
                introspectorModifiers.add(introspectorModifier)
            }
        }

        module.setupModule(setupContext)
    }

    private fun createRenderContext(): RenderContext {
        val renderIntrospector = createRenderIntrospector()
        val renderSerializer = createRenderSerializer()

        return renderIntrospector + renderSerializer
    }

    private fun createRenderIntrospector(): JpqlRenderIntrospector {
        var introspector = introspectors.reduce { acc, introspector ->
            CombinedJpqlIntrospector(acc, introspector)
        }

        introspectorModifiers.forEach {
            introspector = it.modifyIntrospector(introspector)
        }

        return JpqlRenderIntrospector(introspector)
    }

    private fun createRenderSerializer(): JpqlRenderSerializer {
        val serializers = this.serializers.toMutableList()

        serializers.forEach { serializer ->
            serializerModifiers.forEach { modifier ->
                val modifiedSerializer = modifier.modifySerializer(serializer)

                serializers.add(modifiedSerializer)
            }
        }

        return JpqlRenderSerializer(serializers)
    }

    @SinceJdsl("3.0.0")
    fun registerModule(module: JpqlRenderModule): JpqlRenderContext {
        return JpqlRenderContext(this.modules + module)
    }

    @SinceJdsl("3.0.0")
    fun registerModules(vararg modules: JpqlRenderModule): JpqlRenderContext {
        return JpqlRenderContext(this.modules.toList() + modules.toList())
    }

    @SinceJdsl("3.0.0")
    fun registerModules(modules: Iterable<JpqlRenderModule>): JpqlRenderContext {
        return JpqlRenderContext(this.modules.toList() + modules.toList())
    }

    override fun <E : RenderContext.Element> get(key: RenderContext.Key<E>): E? {
        return delegate[key]
    }

    override fun <R> fold(initial: R, operation: (R, RenderContext.Element) -> R): R {
        return delegate.fold(initial, operation)
    }

    override fun minusKey(key: RenderContext.Key<*>): RenderContext {
        return delegate.minusKey(key)
    }
}

private class DefaultModule : JpqlRenderModule {
    private val isJavaxPresent = ClassUtils.isPresent("javax.persistence.Entity")
    private val isJakartaPresent = ClassUtils.isPresent("jakarta.persistence.Entity")

    override fun setupModule(context: JpqlRenderModule.SetupContext) {
        if (isJavaxPresent) {
            context.appendIntrospector(JavaxJpqlIntrospector())
        }

        if (isJakartaPresent) {
            context.appendIntrospector(JakartaJpqlIntrospector())
        }

        context.addAllSerializer(
            JpqlAliasedExpressionSerializer(),
            JpqlAndSerializer(),
            JpqlAvgSerializer(),
            JpqlBetweenSerializer(),
            JpqlCaseWhenSerializer(),
            JpqlCaseValueSerializer(),
            JpqlCountSerializer(),
            JpqlCustomExpressionSerializer(),
            JpqlDeleteQuerySerializer(),
            JpqlDerivedEntitySerializer(),
            JpqlDivideSerializer(),
            JpqlEntityPropertySerializer(),
            JpqlEntitySerializer(),
            JpqlEntityTreatSerializer(),
            JpqlEntityTypeSerializer(),
            JpqlEqualAllSerializer(),
            JpqlEqualAnySerializer(),
            JpqlEqualSerializer(),
            JpqlExistsSerializer(),
            JpqlExpressionParenthesesSerializer(),
            JpqlExpressionSerializer(),
            JpqlFunctionSerializer(),
            JpqlGreaterThanAllSerializer(),
            JpqlGreaterThanAnySerializer(),
            JpqlGreaterThanOrEqualToAllSerializer(),
            JpqlGreaterThanOrEqualToAnySerializer(),
            JpqlGreaterThanOrEqualToSerializer(),
            JpqlGreaterThanSerializer(),
            JpqlInnerAssociationFetchJoinSerializer(),
            JpqlInnerAssociationJoinSerializer(),
            JpqlInnerFetchJoinSerializer(),
            JpqlInnerJoinSerializer(),
            JpqlInSerializer(),
            JpqlInSubquerySerializer(),
            JpqlIsEmptySerializer(),
            JpqlIsNotEmptySerializer(),
            JpqlIsNotNullSerializer(),
            JpqlIsNullSerializer(),
            JpqlJoinedEntitySerializer(),
            JpqlLeftAssociationFetchJoinSerializer(),
            JpqlLeftAssociationJoinSerializer(),
            JpqlLeftFetchJoinSerializer(),
            JpqlLeftJoinSerializer(),
            JpqlLessThanAllSerializer(),
            JpqlLessThanAnySerializer(),
            JpqlLessThanOrEqualToAllSerializer(),
            JpqlLessThanOrEqualToAnySerializer(),
            JpqlLikeSerializer(),
            JpqlLiteralSerializer(),
            JpqlMaxSerializer(),
            JpqlMinSerializer(),
            JpqlMinusSerializer(),
            JpqlNewSerializer(),
            JpqlNotBetweenSerializer(),
            JpqlNotEqualAllSerializer(),
            JpqlNotEqualAnySerializer(),
            JpqlNotExistsSerializer(),
            JpqlNotInSerializer(),
            JpqlNotInSubquerySerializer(),
            JpqlNotLikeSerializer(),
            JpqlNotSerializer(),
            JpqlNullIfSerializer(),
            JpqlNullSerializer(),
            JpqlOrSerializer(),
            JpqlParamSerializer(),
            JpqlPathPropertySerializer(),
            JpqlPathTreatSerializer(),
            JpqlPathTypeSerializer(),
            JpqlPlusSerializer(),
            JpqlPredicateParenthesesSerializer(),
            JpqlSelectQuerySerializer(),
            JpqlSortSerializer(),
            JpqlSubquerySerializer(),
            JpqlSumSerializer(),
            JpqlTimesSerializer(),
            JpqlUpdateQuerySerializer(),
            JpqlValueSerializer(),
        )
    }
}
