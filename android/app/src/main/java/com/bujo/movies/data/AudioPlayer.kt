package com.bujo.movies.data

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import java.io.File

/**
 * Thin wrapper over MediaPlayer for playing bundled audio clips from
 * the assets/ folder. Keeps a single player alive so clips can be
 * replayed. Tries the efficient uncompressed-asset path first and
 * falls back to copying the asset into the app cache directory when
 * that path fails (e.g. if the asset ends up compressed in the APK).
 */
class AudioPlayer {
    private var player: MediaPlayer? = null

    fun playFromAssets(context: Context, assetPath: String) {
        stop()

        // Path 1: try openFd (works when the asset is stored uncompressed).
        try {
            val afd = context.assets.openFd(assetPath)
            afd.use {
                player = MediaPlayer().apply {
                    setDataSource(it.fileDescriptor, it.startOffset, it.length)
                    setOnErrorListener { _, what, extra ->
                        Log.e(TAG, "MediaPlayer error for $assetPath what=$what extra=$extra")
                        false
                    }
                    prepare()
                    start()
                }
            }
            Log.d(TAG, "Playing $assetPath via openFd path")
            return
        } catch (e: Exception) {
            Log.w(TAG, "openFd failed for $assetPath (${e.message}); falling back to cache copy")
        }

        // Path 2: copy the asset into cache and play from file path.
        try {
            val safeName = assetPath.replace('/', '_')
            val cacheFile = File(context.cacheDir, safeName)
            if (!cacheFile.exists() || cacheFile.length() == 0L) {
                context.assets.open(assetPath).use { input ->
                    cacheFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
            player = MediaPlayer().apply {
                setDataSource(cacheFile.absolutePath)
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error (fallback) for $assetPath what=$what extra=$extra")
                    false
                }
                prepare()
                start()
            }
            Log.d(TAG, "Playing $assetPath via cache copy at ${cacheFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Both playback paths failed for $assetPath: ${e.message}", e)
        }
    }

    fun stop() {
        try {
            player?.stop()
        } catch (_: IllegalStateException) {
        }
        try {
            player?.release()
        } catch (_: Exception) {
        }
        player = null
    }

    companion object {
        private const val TAG = "Bujo/AudioPlayer"
    }
}
