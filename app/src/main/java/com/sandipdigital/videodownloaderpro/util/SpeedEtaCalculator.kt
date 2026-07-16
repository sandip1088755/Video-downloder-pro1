package com.sandipdigital.videodownloaderpro.util

/**
 * Tracks a rolling window of (timestampMs, totalBytesDownloaded) samples
 * to produce a smoothed download speed and an ETA estimate.
 */
class SpeedEtaCalculator(private val windowSize: Int = 5) {
    private data class Sample(val timeMs: Long, val bytes: Long)

    private val samples = ArrayDeque<Sample>()

    fun addSample(timeMs: Long, totalBytes: Long) {
        samples.addLast(Sample(timeMs, totalBytes))
        while (samples.size > windowSize) samples.removeFirst()
    }

    fun currentSpeedBps(): Long {
        if (samples.size < 2) return 0
        val first = samples.first()
        val last = samples.last()
        val dt = (last.timeMs - first.timeMs).coerceAtLeast(1)
        val db = (last.bytes - first.bytes).coerceAtLeast(0)
        return (db * 1000L) / dt
    }

    fun etaSeconds(totalBytes: Long, downloadedBytes: Long): Long {
        val speed = currentSpeedBps()
        if (speed <= 0 || totalBytes <= 0) return -1
        val remaining = (totalBytes - downloadedBytes).coerceAtLeast(0)
        return remaining / speed
    }
}
