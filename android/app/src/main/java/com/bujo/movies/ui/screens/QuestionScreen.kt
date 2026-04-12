package com.bujo.movies.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bujo.movies.R
import com.bujo.movies.data.AudioPlayer
import com.bujo.movies.data.ContentPack
import com.bujo.movies.data.ProfileStore
import com.bujo.movies.data.Question
import com.bujo.movies.data.matches
import com.bujo.movies.ui.theme.LegacyCream
import com.bujo.movies.ui.theme.LegacyCrimson
import com.bujo.movies.ui.theme.LegacyDark
import kotlinx.coroutines.launch

// ── Palette constants for this screen ────────────────────────────────────────
private val TopBarAccent = Color(0xFFA03234)
private val CoinGold = Color(0xFFF4CF54)
private val PlayAreaBg = Color(0xFFFDFFF1)
private val SlotColor = Color(0xFFA03234)
private val HintGreen = Color(0xFFA3DB76)   // unused / available hint
private val HintUsed = Color(0xFFA03234)    // hint already invoked
private val AnswerBoxBorder = Color(0xFFEBD6CB)

/** Which kind of hint the confirmation dialog is about. */
private enum class HintKind { Letter, Dialogue, Soundtrack }

/**
 * Computes the starting nudge: one random letter revealed per word with 3+ letters.
 * Returns a Map of letter-index (0-based, spaces excluded) → correct Char.
 * Uses questionId as random seed for consistency across recompositions.
 */
private fun computeStartingNudge(title: String, questionId: Int): Map<Int, Char> {
    val rng = java.util.Random(questionId.toLong())
    val result = mutableMapOf<Int, Char>()
    val words = title.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    var letterIdx = 0
    for (word in words) {
        if (word.length >= 3) {
            val pick = rng.nextInt(word.length)
            result[letterIdx + pick] = word[pick]
        }
        letterIdx += word.length
    }
    return result
}

/**
 * Picks one random unfrozen, unfilled letter index to reveal.
 * Returns null if all positions are already frozen or filled.
 */
private fun pickRandomLetterToReveal(
    title: String,
    frozenMap: Map<Int, Char>,
    typedLetters: String,
    seed: Long,
): Int? {
    val totalLetters = title.count { it != ' ' }
    // Build list of positions that are neither frozen nor typed-into
    val filledCount = frozenMap.size + typedLetters.length
    val candidates = (0 until totalLetters).filter { it !in frozenMap && it >= typedLetters.length }
    // Also include positions that typed letters haven't reached but aren't frozen
    val allCandidates = (0 until totalLetters).filter { it !in frozenMap }
        .let { free ->
            // Among free positions, pick one that isn't already occupied by a typed letter
            // typedLetters fills non-frozen positions left-to-right
            val typedPositions = mutableSetOf<Int>()
            var ti = 0
            for (i in 0 until totalLetters) {
                if (i in frozenMap) continue
                if (ti < typedLetters.length) {
                    typedPositions.add(i)
                    ti++
                }
            }
            free.filter { it !in typedPositions }
        }
    if (allCandidates.isEmpty()) return null
    val rng = java.util.Random(seed)
    return allCandidates[rng.nextInt(allCandidates.size)]
}

/**
 * Given frozen positions and the movie title, returns the correct character
 * at the given letter-index (spaces excluded).
 */
private fun correctLetterAt(title: String, letterIndex: Int): Char {
    var idx = 0
    for (ch in title) {
        if (ch == ' ') continue
        if (idx == letterIndex) return ch
        idx++
    }
    return '?'
}

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

    var typedLetters by remember(next?.id) { mutableStateOf("") }
    var pendingHint by remember(next?.id) { mutableStateOf<HintKind?>(null) }
    var dialogueUnlocked by remember(next?.id) { mutableStateOf(false) }
    var soundtrackUnlocked by remember(next?.id) { mutableStateOf(false) }
    var letterHintCount by remember(next?.id) { mutableStateOf(0) }
    var expandedSnapshot by remember(next?.id) { mutableStateOf<String?>(null) }

    if (next == null) return

    // Total letter slots (excluding spaces in the title)
    val totalLetters = remember(next) {
        next.movieTitle.count { it != ' ' }
    }

    // Frozen letters: starting nudge + any letter hints purchased
    val startingFrozen = remember(next) {
        computeStartingNudge(next.movieTitle, next.id)
    }
    var frozenMap by remember(next?.id) { mutableStateOf(startingFrozen) }

    // Number of user-typeable slots = total minus frozen
    val typeableSlots = totalLetters - frozenMap.size

    // Auto-validate once all typeable blanks are filled
    LaunchedEffect(typedLetters, typeableSlots, frozenMap) {
        if (typedLetters.length >= typeableSlots) {
            val fullGuess = reconstructFromFrozenAndTyped(next.movieTitle, frozenMap, typedLetters)
            if (next.matches(fullGuess)) {
                ProfileStore.awardCoins(context, ProfileStore.COINS_PER_CORRECT)
                ProfileStore.markAnswered(context, next.id)
                onCorrect(next.id)
            }
            // On mismatch: nothing happens — player can backspace and retry
        }
    }

    // Compute question number (1-based) for the badge
    val questionNumber = remember(next, content) {
        content.questions.indexOf(next) + 1
    }

    // Focus requester for the hidden input
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PlayAreaBg)
            .imePadding(),
    ) {
        // ── White top strip: settings | level badge | coins ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(1f)
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(0.dp))
                .background(Color.White)
                .drawBehind {
                    val borderColor = Color(0xFFF7F9EB)
                    drawLine(
                        color = borderColor,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx(),
                    )
                }
                .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 5.dp),
        ) {
            // Left: settings gear
            IconButton(
                onClick = onSettings,
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.CenterStart),
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = stringResource(R.string.settings),
                    tint = TopBarAccent,
                    modifier = Modifier.size(30.dp),
                )
            }

            // Center: seal badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.align(Alignment.Center).offset(y = 5.dp),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_level_badge),
                    contentDescription = "Level badge",
                    modifier = Modifier.size(62.dp),
                )
                Text(
                    text = "1x$questionNumber",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                )
            }

            // Right: coins icon + count
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.CenterEnd),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_coins),
                    contentDescription = "Coins",
                    modifier = Modifier.size(34.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "${profile?.coins ?: 0}",
                    color = TopBarAccent,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // ── Content area ──
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
        ) {

            Spacer(Modifier.height(40.dp))

            // 2×2 snapshot container (645:380 proportions, border radius 20)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(645f / 380f)
                    .clip(RoundedCornerShape(20.dp)),
            ) {
                // 2×2 grid (shown when no snapshot is expanded)
                if (expandedSnapshot == null) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            SnapshotTile(
                                filename = next.snapshotFilenames.getOrElse(0) { "" },
                                onClick = { expandedSnapshot = next.snapshotFilenames.getOrNull(0) },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 6.dp, bottom = 6.dp),
                            )
                            SnapshotTile(
                                filename = next.snapshotFilenames.getOrElse(1) { "" },
                                onClick = { expandedSnapshot = next.snapshotFilenames.getOrNull(1) },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 6.dp, bottom = 6.dp),
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            SnapshotTile(
                                filename = next.snapshotFilenames.getOrElse(2) { "" },
                                onClick = { expandedSnapshot = next.snapshotFilenames.getOrNull(2) },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 6.dp, top = 6.dp),
                            )
                            SnapshotTile(
                                filename = next.snapshotFilenames.getOrElse(3) { "" },
                                onClick = { expandedSnapshot = next.snapshotFilenames.getOrNull(3) },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 6.dp, top = 6.dp),
                            )
                        }
                    }
                } else {
                    // Expanded: single snapshot fills the entire container
                    SnapshotTile(
                        filename = expandedSnapshot!!,
                        onClick = { expandedSnapshot = null },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Answer box: fills remaining space down to the bottom ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 20.dp)
                    .border(
                        width = 1.dp,
                        color = AnswerBoxBorder,
                        shape = RoundedCornerShape(16.dp),
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        focusRequester.requestFocus()
                        keyboard?.show()
                    }
                    .padding(20.dp),
            ) {
                // Word slots centred vertically inside the box
                LetterSlots(
                    title = next.movieTitle,
                    typedLetters = typedLetters,
                    frozenMap = frozenMap,
                    modifier = Modifier.align(Alignment.Center),
                )

                // Hint icons pinned to bottom-right
                Row(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Letter hint icon (leftmost)
                    IconButton(
                        onClick = { pendingHint = HintKind.Letter },
                        modifier = Modifier.size(44.dp),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_hint_letter),
                            contentDescription = stringResource(R.string.hint_letter),
                            modifier = Modifier.size(38.dp),
                            colorFilter = ColorFilter.tint(
                                if (letterHintCount > 0) HintUsed else HintGreen,
                            ),
                        )
                    }

                    // Dialogue hint icon
                    IconButton(
                        onClick = { pendingHint = HintKind.Dialogue },
                        modifier = Modifier.size(44.dp),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_hint_dialogue),
                            contentDescription = stringResource(R.string.hint_dialogue),
                            modifier = Modifier.size(38.dp),
                            colorFilter = ColorFilter.tint(
                                if (dialogueUnlocked) HintUsed else HintGreen,
                            ),
                        )
                    }

                    // Music hint icon
                    IconButton(
                        onClick = { pendingHint = HintKind.Soundtrack },
                        enabled = next.soundtrackFilename.isNotBlank(),
                        modifier = Modifier.size(44.dp),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_hint_music),
                            contentDescription = stringResource(R.string.hint_soundtrack),
                            modifier = Modifier.size(38.dp),
                            colorFilter = ColorFilter.tint(
                                if (soundtrackUnlocked) HintUsed else HintGreen,
                            ),
                        )
                    }
                }

                // Hidden text field to capture keyboard input
                BasicTextField(
                    value = typedLetters,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { it.isLetter() }
                        if (filtered.length <= typeableSlots) {
                            typedLetters = filtered
                        }
                    },
                    modifier = Modifier
                        .size(1.dp)
                        .alpha(0f)
                        .focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        keyboardType = KeyboardType.Text,
                    ),
                    cursorBrush = SolidColor(Color.Transparent),
                )
            } // end answer Box
        } // end content Column
    }

    pendingHint?.let { kind ->
        when (kind) {
            HintKind.Letter -> {
                // Check if there are still letters to reveal
                val hasUnrevealed = frozenMap.size < totalLetters - typedLetters.length
                val cost = ProfileStore.COINS_PER_LETTER_HINT
                val canAfford = (profile?.coins ?: 0) >= cost
                AlertDialog(
                    onDismissRequest = { pendingHint = null },
                    containerColor = LegacyCream,
                    titleContentColor = LegacyCrimson,
                    textContentColor = LegacyDark,
                    title = { Text(stringResource(R.string.hint_letter), color = LegacyCrimson) },
                    text = {
                        Text(
                            if (!hasUnrevealed)
                                "No more letters to reveal!"
                            else if (canAfford)
                                "Spend $cost coins to reveal a letter?"
                            else
                                stringResource(R.string.not_enough_coins),
                            color = LegacyDark,
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    val ok = ProfileStore.spendCoins(context, cost)
                                    if (ok) {
                                        letterHintCount++
                                        val seed = next.id.toLong() * 1000 + letterHintCount
                                        val idx = pickRandomLetterToReveal(
                                            next.movieTitle, frozenMap, typedLetters, seed,
                                        )
                                        if (idx != null) {
                                            val ch = correctLetterAt(next.movieTitle, idx)
                                            frozenMap = frozenMap + (idx to ch)
                                        }
                                    }
                                    pendingHint = null
                                }
                            },
                            enabled = canAfford && hasUnrevealed,
                        ) {
                            Text("Confirm", color = LegacyCrimson)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { pendingHint = null }) {
                            Text("Cancel", color = LegacyDark)
                        }
                    },
                )
            }
            else -> {
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
                    else -> return@let // unreachable
                }
                val canAfford = (profile?.coins ?: 0) >= cost
                AlertDialog(
                    onDismissRequest = { pendingHint = null },
                    containerColor = LegacyCream,
                    titleContentColor = LegacyCrimson,
                    textContentColor = LegacyDark,
                    title = { Text(stringResource(titleRes), color = LegacyCrimson) },
                    text = {
                        Text(
                            if (unlocked)
                                "Play the clip again?"
                            else if (canAfford)
                                "Spend $cost coins to hear the clip?"
                            else
                                stringResource(R.string.not_enough_coins),
                            color = LegacyDark,
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
                                                else -> {}
                                            }
                                        }
                                    }
                                    val nowUnlocked = when (kind) {
                                        HintKind.Dialogue -> dialogueUnlocked || alreadyUnlocked
                                        HintKind.Soundtrack -> soundtrackUnlocked || alreadyUnlocked
                                        else -> false
                                    }
                                    if (nowUnlocked && filename.isNotBlank()) {
                                        audio.playFromAssets(context, "audio/$filename")
                                    }
                                    pendingHint = null
                                }
                            },
                            enabled = unlocked || canAfford,
                        ) {
                            Text(if (unlocked) "Play" else "Confirm", color = LegacyCrimson)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { pendingHint = null }) {
                            Text("Cancel", color = LegacyDark)
                        }
                    },
                )
            }
        }
    }
}

private data class HintContext(
    val cost: Int,
    val unlocked: Boolean,
    val filename: String,
    val titleRes: Int,
)

/**
 * Reconstructs a full guess string from typed letters by re-inserting spaces
 * where the original title has them.
 * e.g. title="Pulp Fiction", letters="pulpfiction" → "pulp fiction"
 */
/**
 * Reconstructs a full guess by merging frozen letters and user-typed letters.
 * Frozen positions use the frozen character; non-frozen positions consume
 * typed letters left-to-right. Spaces are re-inserted from the original title.
 */
private fun reconstructFromFrozenAndTyped(
    title: String,
    frozenMap: Map<Int, Char>,
    typedLetters: String,
): String {
    val result = StringBuilder()
    var letterIdx = 0  // index into letter positions (spaces excluded)
    var typedIdx = 0   // index into typedLetters
    for (ch in title) {
        if (ch == ' ') {
            result.append(' ')
        } else {
            if (letterIdx in frozenMap) {
                result.append(frozenMap[letterIdx])
            } else if (typedIdx < typedLetters.length) {
                result.append(typedLetters[typedIdx])
                typedIdx++
            }
            letterIdx++
        }
    }
    return result.toString()
}

/**
 * Renders letter blanks grouped by word with "/" separators.
 * Frozen letters (from starting nudge or letter hint) are shown in [SlotColor].
 * Typed letters fill the remaining non-frozen blanks left-to-right.
 * Unfilled positions show an underscore bar.
 *
 * Font size adapts to the total letter count so movies with ≤15 letters
 * comfortably fit on a single line.
 */
@Composable
private fun LetterSlots(
    title: String,
    typedLetters: String,
    frozenMap: Map<Int, Char>,
    modifier: Modifier = Modifier,
) {
    val words = remember(title) {
        title.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    }
    val totalLetters = remember(title) { title.count { it != ' ' } }

    // Adaptive sizing
    val slotWidth = when {
        totalLetters <= 8 -> 24.dp
        totalLetters <= 15 -> 18.dp
        totalLetters <= 20 -> 14.dp
        else -> 11.dp
    }
    val letterFontSize = when {
        totalLetters <= 8 -> 22.sp
        totalLetters <= 15 -> 16.sp
        totalLetters <= 20 -> 14.sp
        else -> 11.sp
    }
    val slashFontSize = when {
        totalLetters <= 15 -> 28.sp
        else -> 22.sp
    }
    val gap = when {
        totalLetters <= 8 -> 6.dp
        totalLetters <= 15 -> 3.dp
        else -> 2.dp
    }
    val slashPadding = when {
        totalLetters <= 15 -> 6.dp
        else -> 4.dp
    }

    // Iterate through positions, consuming typed letters for non-frozen slots
    var letterIdx = 0   // global letter index (spaces excluded)
    var typedIdx = 0    // index into typedLetters
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        words.forEachIndexed { wordIndex, word ->
            if (wordIndex > 0) {
                Text(
                    text = "/",
                    color = SlotColor,
                    fontSize = slashFontSize,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = slashPadding),
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(gap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(word.length) {
                    val currentIdx = letterIdx
                    letterIdx++

                    // Determine what to show at this position
                    val displayChar: Char? = if (currentIdx in frozenMap) {
                        frozenMap[currentIdx]
                    } else if (typedIdx < typedLetters.length) {
                        val ch = typedLetters[typedIdx]
                        typedIdx++
                        ch
                    } else {
                        null
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.width(slotWidth),
                    ) {
                        if (displayChar != null) {
                            Text(
                                text = displayChar.uppercase(),
                                color = SlotColor,
                                fontSize = letterFontSize,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                            )
                        } else {
                            // Underscore bar
                            Box(
                                modifier = Modifier
                                    .width(slotWidth)
                                    .height(4.dp)
                                    .background(SlotColor),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SnapshotTile(
    filename: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    if (filename.isBlank()) return
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(LegacyDark)
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data("file:///android_asset/images/$filename")
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    }
}

