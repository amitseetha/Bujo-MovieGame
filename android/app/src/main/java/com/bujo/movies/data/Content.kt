package com.bujo.movies.data

import android.content.Context
import org.json.JSONObject
import java.nio.charset.Charset

/** One question in the Bujo content pack. */
data class Question(
    val id: Int,
    val prompt: String,
    val movieTitle: String,
    val acceptedAnswers: List<String>,
    val snapshotFilenames: List<String>,
    val dialogueFilename: String,
    val soundtrackFilename: String,
    val triviaQuote: String,
    val successLine: String,
    val posterFilename: String,
)

/** Top-level bundled content loaded once at app start. */
data class ContentPack(
    val version: String,
    val questions: List<Question>,
)

object ContentLoader {
    private const val ASSET_PATH = "bundled_content.json"

    fun load(context: Context): ContentPack {
        val raw = context.assets.open(ASSET_PATH).use { input ->
            input.readBytes().toString(Charset.forName("UTF-8"))
        }
        val root = JSONObject(raw)
        val version = root.optString("version", "0.0.0")
        val qArr = root.getJSONArray("questions")
        val questions = buildList {
            for (i in 0 until qArr.length()) {
                val q = qArr.getJSONObject(i)
                val snaps = q.getJSONArray("snapshot_filenames")
                    .let { arr -> List(arr.length()) { arr.getString(it) } }
                val answers = q.getJSONArray("accepted_answers")
                    .let { arr -> List(arr.length()) { arr.getString(it) } }
                add(
                    Question(
                        id = q.getInt("question_id"),
                        prompt = q.optString("prompt", "Which Movie"),
                        movieTitle = q.getString("movie_title"),
                        acceptedAnswers = answers,
                        snapshotFilenames = snaps,
                        dialogueFilename = q.getString("dialogue_filename"),
                        soundtrackFilename = q.optString("soundtrack_filename", ""),
                        triviaQuote = q.optString("trivia_quote", ""),
                        successLine = q.optString("success_line", "Nice!"),
                        posterFilename = q.optString("poster_filename", ""),
                    )
                )
            }
        }
        return ContentPack(version = version, questions = questions)
    }
}

/** Case-insensitive, trimmed string match against any accepted answer. */
fun Question.matches(guess: String): Boolean {
    val normalized = guess.trim().lowercase()
    if (normalized.isEmpty()) return false
    return acceptedAnswers.any { it.trim().lowercase() == normalized }
}
