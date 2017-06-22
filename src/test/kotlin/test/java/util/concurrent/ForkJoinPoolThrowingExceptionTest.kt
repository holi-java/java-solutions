package test.java.util.concurrent

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.sameInstance
import org.junit.Test
import java.util.concurrent.ExecutionException
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinPool.commonPool

class ForkJoinPoolThrowingExceptionTest {
    val exception: Throwable = IllegalStateException();

    @Test
    fun `throws the exception directly by commonPool`() {
        commonPool().throwing(exception) { actual ->
            assert.that(actual, sameInstance(exception));
        };
    }

    @Test
    fun `rethrows the exception by parallelism ForkJoinPool`() {
        ForkJoinPool(1).throwing(exception) { actual ->
            assert.that(actual::class, equalTo(exception::class));
            assert.that(actual.cause!!, sameInstance(exception));
        };
    }

    @Test
    fun `avoiding rethrows the exception in parallelism ForkJoinPool by makes the Exception constructor non-public`() {
        val nonPublicException: Throwable = object : IllegalStateException() {/**/ };

        ForkJoinPool(1).throwing(nonPublicException) { actual ->
            assert.that(actual, sameInstance(nonPublicException));
        };
    }

    fun ForkJoinPool.throwing(exception: Throwable, exceptionally: (actual: Throwable) -> Unit): Unit {
        val task = submit { throw exception; };
        try {
            task.get();
        } catch(ex: ExecutionException) {
            exceptionally(ex.cause ?: ex);
        }
    };
}


