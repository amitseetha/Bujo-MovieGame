package com.bujo.movies.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bujo.movies.R
import com.bujo.movies.data.AudioPlayer
import com.bujo.movies.data.ContentPack
import com.bujo.movies.data.ProfileStore
import com.bujo.movies.data.Question
import com.bujo.movies.data.matches
import com.bujo.movies.ui.theme.BrandCream
import com.bujo.movies.ui.theme.BrandDark
import com.bujo.movies.ui.theme.BrandRed
import kotlinx.coroutines.launch

/** Which kind of audio hint the confirmation dialog is about. */
private enum class HintKind { Dialogue, Soundtrack }

@Composable
fun QuestionScreen(
    content: ContentPack,
    onCorrect: (Int) -> Unit,
    onAllDone: () -> Unit,
    onSettings: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val profile by ProfileStore.profileFlow(context).collectAsState(initial = null)
    val audio = remember { AudioPlayer() }

    val answered = profile?.answeredIds ?: emptySet()
    val next: Question? = remember(answered, content) {
        content.questions.firstOrNull { it.id !in answered }
    }

    LaunchedEffect(next) {
        if (next == null && profile != null) onAllDone()
    }

    var guess by remember(next?.id) { mutableStateOf("") }
    var wrong by remember(next?.id) { mutableStateOf(false) }
    var pendingHint by remember(next?.id) { mutableStateOf<HintKind?>(null) }
    var dialogueUnlocked by remember(next?.id) { mutableStateOf(false) }
    var soundtrackUnlocked by remember(next?.id) { mutableStateOf(false) }

    if (next == null) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        // Top bar: coins + settings
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "${stringResource(R.string.coins)}: ${profile?.coins ?: 0}",
                color = BrandDark,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onSettings) {
                Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings))
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = next.prompt,
            color = BrandRed,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(12.dp))

        // 2x2 snapshot grid.
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(0.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(next.snapshotFilenames) { filename ->
                SnapshotTile(filename)
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = guess,
            onValueChange = { guess = it; wrong = false },
            label = { Text(stringResource(R.string.guess_hint)) },
            singleLine = true,
            isError = wrong,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))

        // Row 1: two hint buttons side by side.
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { pendingHint = HintKind.Dialogue },
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = "${stringResource(R.string.hint_dialogue)}\n${stringResource(R.string.hint_cost)}",
                    fontSize = 12.sp,
                )
            }
            Spacer(Modifier.padding(horizontal = 4.dp))
            Button(
                onClick = { pendingHint = HintKind.Soundtrack },
                modifier = Modifier.weight(1f),
                enabled = next.soundtrackFilename.isNotBlank(),
            ) {
                Text(
                    text = "${stringResource(R.string.hint_soundtrack)}\n${stringResource(R.string.hint_cost)}",
                    fontSize = 12.sp,
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Row 2: full-width Guess button.
        Button(
            onClick = {
                if (next.matches(guess)) {
                    scope.launch {
                        ProfileStore.awardCoins(context, ProfileStore.COINS_PER_CORRECT)
                        ProfileStore.markAnswered(context, next.id)
                        onCorrect(next.id)
                    }
                } else {
                    wrong = true
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.submit))
        }
    }

    pendingHint?.let { kind ->
        val (cost, unlocked, filename, titleRes) = when (kind) {
            HintKind.Dialogue -> HintContext(
                cost = ProfileStore.COINS_PER_DIALOGUE_HINT,
                unlocked = dialogueUnlocked,
                filename = next.dialogueFilename,
                titleRes = R.string.hint_dialogue,
            )
            HintKind.Soundtrack -> HintContext(
                cost = ProfileStore.COINS_PER_SOUNDTRACK_HINT,
                unlocked = soundtrackUnlocked,
                filename = next.soundtrackFilename,
                titleRes = R.string.hint_soundtrack,
            )
        }
        val canAfford = (profile?.coins ?: 0) >= cost
        AlertDialog(
            onDismissRequest = { pendingHint = null },
            title = { Text(stringResource(titleRes)) },
            text = {
                Text(
                    if (unlocked)
                        "Play the clip again?"
                    else if (canAfford)
                        "Spend $cost coins to hear the clip?"
                    else
                        stringResource(R.string.not_enough_coins)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val alreadyUnlocked = unlocked
                            if (!alreadyUnlocked) {
                                val ok = ProfileStore.spendCoins(context, cost)
                                if (ok) {
                                    when (kind) {
                                        HintKind.Dialogue -> dialogueUnlocked = true
                                        HintKind.Soundtrack -> soundtrackUnlocked = true
                                    }
                                }
                            }
                            // Play the clip if now unlocked (or was already).
                            val nowUnlocked = when (kind) {
                                HintKind.Dialogue -> dialogueUnlocked || alreadyUnlocked
                                HintKind.Soundtrack -> soundtrackUnlocked || alreadyUnlocked
                            }
                            if (nowUnlocked && filename.isNotBlank()) {
                                audio.playFromAssets(context, "audio/$filename")
                            }
                            pendingHint = null
                        }
                    },
                    enabled = unlocked || canAfford,
                ) {
                    Text(if (unlocked) "Play" else "Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingHint = null }) { Text("Cancel") }
            },
        )
    }
}

private data class HintContext(
    val cost: Int,
    val unlocked: Boolean,
    val filename: String,
    val titleRes: Int,
)

@Composable
private fun SnapshotTile(filename: String) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(BrandCream),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data("file:///android_asset/images/$filename")
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
