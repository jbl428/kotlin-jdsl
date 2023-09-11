@file:Suppress("JpaDataSourceORMInspection")

package com.linecorp.kotlinjdsl.example.eclipselink.javax.entity.book

import com.linecorp.kotlinjdsl.example.eclipselink.javax.annotation.CompositeId
import java.io.Serializable
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.IdClass
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "book_publisher")
@IdClass(BookPublisher.BookPublisherId::class)
class BookPublisher(
    @Id
    @Column(name = "publisher_id")
    val publisherId: Long,
) {
    @Id
    @OneToOne
    @JoinColumn(name = "isbn")
    lateinit var book: Book

    private val bookPublisherId get() = BookPublisherId(book.isbn, publisherId)

    override fun equals(other: Any?): Boolean =
        Objects.equals(bookPublisherId, (other as? BookPublisher)?.bookPublisherId)

    override fun hashCode(): Int =
        Objects.hashCode(bookPublisherId)

    override fun toString(): String =
        "BookPublisher(bookPublisherId=$bookPublisherId)"

    @CompositeId
    data class BookPublisherId(
        val book: String,
        val publisherId: Long,
    ) : Serializable
}
