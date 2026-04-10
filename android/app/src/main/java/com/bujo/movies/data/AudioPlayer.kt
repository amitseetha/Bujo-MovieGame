package com.bujo.movies.data

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer

/**
 * Thin wrapper over MediaPlayer for playing bundled dialogue clips from
 * assets/. Keeps one player alive so clips can be replayed.
 */
class AudioPlayer {
    private var player: MediaPlayer? = null

    fun playFromAssets(context: Context, assetPath: String) {
        stop()
        val afd: AssetFileDescriptor = try {
            context.assets.openFd(assetPath)
        } catch (e: Exception) {
            return
        }
        afd.use {
            player = MediaPlayer().apply {
                setDataSource(it.fileDescriptor, it.startOffset, it.length)
                prepare()
                start()
            }
        }
    }

    fun stop() {
        try {
            player?.stop()
        } catch (_: IllegalStateException) {
        }
        player?.release()
        player = null
    }
}
