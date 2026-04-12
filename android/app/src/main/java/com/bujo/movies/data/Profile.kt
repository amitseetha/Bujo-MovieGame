package com.bujo.movies.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "bujo_profile")

/**
 * Single-profile-per-install local storage for v0.1.
 *
 * Username has no format/length restrictions. Coins start at 500. Sound is ON
 * by default. Answered question IDs are tracked so the game resumes correctly
 * and never shows the same question twice.
 */
object ProfileStore {

    private val KEY_USERNAME = stringPreferencesKey("username")
    private val KEY_COINS = intPreferencesKey("coins")
    private val KEY_SOUND_ON = booleanPreferencesKey("sound_on")
    private val KEY_ANSWERED = stringSetPreferencesKey("answered_ids")

    const val SEED_COINS = 500
    const val COINS_PER_CORRECT = 50
    const val COINS_PER_DIALOGUE_HINT = 30
    const val COINS_PER_SOUNDTRACK_HINT = 30
    const val COINS_PER_LETTER_HINT = 60

    data class Profile(
        val username: String?,
        val coins: Int,
        val soundOn: Boolean,
        val answeredIds: Set<Int>,
    )

    fun profileFlow(context: Context): Flow<Profile> =
        context.dataStore.data.map { prefs ->
            Profile(
                username = prefs[KEY_USERNAME],
                coins = prefs[KEY_COINS] ?: SEED_COINS,
                soundOn = prefs[KEY_SOUND_ON] ?: true,
                answeredIds = prefs[KEY_ANSWERED]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet(),
            )
        }

    suspend fun setUsername(context: Context, username: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USERNAME] = username
            if (!prefs.contains(KEY_COINS)) prefs[KEY_COINS] = SEED_COINS
            if (!prefs.contains(KEY_SOUND_ON)) prefs[KEY_SOUND_ON] = true
        }
    }

    suspend fun awardCoins(context: Context, amount: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_COINS] = (prefs[KEY_COINS] ?: SEED_COINS) + amount
        }
    }

    suspend fun spendCoins(context: Context, amount: Int): Boolean {
        var success = false
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_COINS] ?: SEED_COINS
            if (current >= amount) {
                prefs[KEY_COINS] = current - amount
                success = true
            }
        }
        return success
    }

    suspend fun markAnswered(context: Context, questionId: Int) {
        context.dataStore.edit { prefs ->
            val cur = prefs[KEY_ANSWERED] ?: emptySet()
            prefs[KEY_ANSWERED] = cur + questionId.toString()
        }
    }

    suspend fun setSoundOn(context: Context, on: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_SOUND_ON] = on }
    }

    suspend fun clear(context: Context) {
        context.dataStore.edit { it.clear() }
    }
}
