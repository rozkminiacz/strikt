package strikt.api

import strikt.api.Assertion.Builder
import strikt.assertions.throws
import strikt.internal.AssertionBuilder
import strikt.internal.AssertionStrategy
import strikt.internal.AssertionSubject

/**
 * Starts a block of assertions that will all be evaluated regardless of whether
 * earlier ones fail.
 * This is the entry-point for the assertion API.
 */
fun expect(block: ExpectationBuilder.() -> Unit) {
  val subjects = mutableListOf<AssertionSubject<*>>()
  object : ExpectationBuilder {
    override fun <T> that(subject: T): DescribeableBuilder<T> =
      AssertionSubject(subject)
        .also { subjects.add(it) }
        .let { AssertionBuilder(it, AssertionStrategy.Collecting) }

    override fun <T> that(
      subject: T,
      block: Builder<T>.() -> Unit
    ): DescribeableBuilder<T> = that(subject).apply(block)
  }
    .apply(block)
    .let {
      AssertionStrategy.Throwing.evaluate(subjects)
    }
}

/**
 * Start a chain of assertions over [subject].
 * This is the entry-point for the assertion API.
 *
 * @param subject the subject of the chain of assertions.
 * @return an assertion for [subject].
 */
fun <T> expectThat(subject: T): DescribeableBuilder<T> =
  AssertionBuilder(AssertionSubject(subject), AssertionStrategy.Throwing)

/**
 * Evaluate a block of assertions over [subject].
 * This is the entry-point for the assertion API.
 *
 * @param subject the subject of the block of assertions.
 * @param block a closure that can perform multiple assertions that will all
 * be evaluated regardless of whether preceding ones pass or fail.
 * @return an assertion for [subject].
 */
fun <T> expectThat(
  subject: T,
  block: Builder<T>.() -> Unit
): DescribeableBuilder<T> =
  AssertionSubject(subject).let { context ->
    AssertionBuilder(context, AssertionStrategy.Collecting)
      .apply {
        block()
        AssertionStrategy.Throwing.evaluate(context)
      }
  }

/**
 * Asserts that [action]throws an exception of type [E] when executed.
 *
 * @return an assertion over the thrown exception, allowing further assertions
 * about messages, root causes, etc.
 */
@Deprecated(
  "Use the catching function with expectThat",
  replaceWith = ReplaceWith("expectThat(catching(action)).throws<E>()")
)
inline fun <reified E : Throwable> expectThrows(
  noinline action: () -> Unit
): Builder<E> =
  expectThat(catching(action)).throws()
