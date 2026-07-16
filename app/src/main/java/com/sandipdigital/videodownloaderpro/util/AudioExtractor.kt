package com.sandipdigital.videodownloaderpro.util

import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import java.io.File

/**
 * Extracts the audio track from a downloaded video file into a standalone .m4a file.
 * This performs a fast stream-copy (remux) of the existing AAC audio track — it does not
 * re-encode — so it works entirely offline with no third-party codec dependency.
 */
object AudioExtractor {

    fun extractAudioTrack(sourceFile: File): Result<File> {
        return try {
            val extractor = MediaExtractor()
            extractor.setDataSource(sourceFile.absolutePath)

            var audioTrackIndex = -1
            var audioFormat: MediaFormat? = null
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    audioFormat = format
                    break
                }
            }

            if (audioTrackIndex == -1 || audioFormat == null) {
                extractor.release()
                return Result.failure(IllegalStateException("No audio track found in this file"))
            }

            extractor.selectTrack(audioTrackIndex)

            val outputFile = File(
                sourceFile.parentFile,
                FileUtils.sanitizeFileName(sourceFile.nameWithoutExtension + "_audio.m4a")
            )
            val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val outTrackIndex = muxer.addTrack(audioFormat)
            muxer.start()

            val bufferSize = 1 * 1024 * 1024
            val buffer = java.nio.ByteBuffer.allocate(bufferSize)
            val bufferInfo = android.media.MediaCodec.BufferInfo()

            while (true) {
                val sampleSize = extractor.readSampleData(buffer, 0)
                if (sampleSize < 0) break
                bufferInfo.offset = 0
                bufferInfo.size = sampleSize
                bufferInfo.presentationTimeUs = extractor.sampleTime
                bufferInfo.flags = extractor.sampleFlags
                muxer.writeSampleData(outTrackIndex, buffer, bufferInfo)
                extractor.advance()
            }

            muxer.stop()
            muxer.release()
            extractor.release()

            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
