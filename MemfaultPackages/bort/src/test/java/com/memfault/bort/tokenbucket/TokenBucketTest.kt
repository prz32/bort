package com.memfault.bort.tokenbucket

import kotlin.time.Duration
import kotlin.time.milliseconds
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TokenBucketTest {
    var now: Duration = 0.milliseconds
    val mockElapsedRealtime = { now }

    @BeforeEach
    fun setUp() {
        now = 0.milliseconds
    }

    @Test
    fun testFeed() {
        val bucket = TokenBucket(
            capacity = 3,
            period = 2.milliseconds,
            _count = 0,
            _periodStartElapsedRealtime = 0.milliseconds,
            elapsedRealtime = mockElapsedRealtime
        )
        now = 1.milliseconds
        bucket.feed()
        assertEquals(0, bucket.count)
        now = 2.milliseconds
        bucket.feed()
        assertEquals(1, bucket.count)
        now = 10.milliseconds
        bucket.feed()
        assertEquals(3, bucket.count)
    }

    @Test
    fun testTake() {
        val bucket = TokenBucket(
            capacity = 3,
            period = 2.milliseconds,
            _count = 3,
            _periodStartElapsedRealtime = 0.milliseconds,
            elapsedRealtime = mockElapsedRealtime
        )
        assertEquals(false, bucket.take(n = 4))
        assertEquals(3, bucket.count)
        assertEquals(true, bucket.take(n = 2))
        assertEquals(1, bucket.count)
        assertEquals(true, bucket.take())
        assertEquals(0, bucket.count)
        assertEquals(false, bucket.take())
        assertEquals(0, bucket.count)

        // For convenience, take() also feeds():
        now = 10.milliseconds
        assertEquals(true, bucket.take())
        assertEquals(2, bucket.count)
    }

    @Test
    fun testTakeWithLongPeriod() {
        val bucket = TokenBucket(
            capacity = 1,
            period = 1.milliseconds,
            _count = 0,
            _periodStartElapsedRealtime = 0.milliseconds,
            elapsedRealtime = mockElapsedRealtime
        )

        now = 5.milliseconds
        assertEquals(true, bucket.take(1))
        assertEquals(false, bucket.take(1))

        // Check that the externally observable value did change
        assertEquals(5.milliseconds, bucket.periodStartElapsedRealtime)
    }

    @Test
    fun testFeedRate() {
        val bucket = TokenBucket(
            capacity = 10,
            period = 1.milliseconds,
            _count = 0,
            _periodStartElapsedRealtime = 0.milliseconds,
            elapsedRealtime = mockElapsedRealtime
        )

        now = 10.milliseconds
        bucket.feed()
        assertEquals(10, bucket.count)

        bucket.take(10)
        assertEquals(0, bucket.count)

        repeat(10) {
            now += 1.milliseconds
            bucket.feed()
            assertEquals(it + 1, bucket.count)
        }
    }
}
