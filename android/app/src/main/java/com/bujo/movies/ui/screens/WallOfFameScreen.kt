package com.bujo.movies.ui.screens

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bujo.movies.R
import com.bujo.movies.data.ProfileStore

// ── Palette (matches SuccessScreen translucent style) ──────────────────────
private val WofBgTop = Color(0xAAB76460)        // more transparent (~67%)
private val WofBgBottom = Color(0xAAB86667)
private val HeaderText = Color(0xFFFFFFFF)
private val NameText = Color(0xBBFFFFFF)         // softer white for non-player rows
private val PlayerText = Color(0xFFFFFFFF)       // full bright white for player
private val DividerColor = Color(0x44FFFFFF)     // subtle white line separator
private val CoinColor = Color(0xFFF4CF54)

// ── Fake leaderboard entries (from reference image) ────────────────────────
private data class FakeEntry(val name: String, val rank: Int)

private val FAKE_ENTRIES = listOf(
    FakeEntry("BanksyKrane", 1),
    FakeEntry("Klandstein", 2),
    FakeEntry("Jacksparrow", 3),
    FakeEntry("RuthlessSlayer", 4),
    FakeEntry("Bond007", 5),
    FakeEntry("InfernalHeir", 6),
    FakeEntry("TheSilentBang", 7),
    FakeEntry("DarkLord", 8),
    FakeEntry("NoTolerance", 9),
    FakeEntry("DextersProtege", 10),
    // rank 11 is the player's slot by default (may shift based on coins)
    FakeEntry("LoneSurvivor", 12),
    FakeEntry("SavageHorseman", 13),
    FakeEntry("GunnerBomb", 14),
)

/**
 * Map the player's coin balance to a rank between 1 and 14.
 * >=700 coins → #1, <=30 coins → #14, linearly interpolated in between.
 */
private fun playerRank(coins: Int): Int {
    if (coins >= 700) return 1
    if (coins <= 30) return 14
    // Linear: rank goes from 1 (at 700) to 14 (at 30)
    val fraction = (coins - 30).toFloat() / (700 - 30).toFloat()   // 0..1
    return (14 - (fraction * 13).toInt()).coerceIn(1, 14)
}

/**
 * Build the full leaderboard list by inserting the player at the computed rank
 * and shifting the fake entries around.
 */
private fun buildLeaderboard(playerName: String, coins: Int): List<Pair<String, Int>> {
    val pRank = playerRank(coins)
    val result = mutableListOf<Pair<String, Int>>()
    var fakeIdx = 0
    for (rank in 1..14) {
        if (rank == pRank) {
            result.add(playerName to rank)
        } else {
            if (fakeIdx < FAKE_ENTRIES.size) {
                result.add(FAKE_ENTRIES[fakeIdx].name to rank)
                fakeIdx++
            }
        }
    }
    return result
}

@Composable
fun WallOfFameScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val profile by ProfileStore.profileFlow(context).collectAsState(initial = null)

    val playerName = profile?.username ?: "You"
    val coins = profile?.coins ?: ProfileStore.SEED_COINS
    val pRank = remember(coins) { playerRank(coins) }
    val leaderboard = remember(playerName, coins) { buildLeaderboard(playerName, coins) }

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Layer 1: Splash background (no username input / start button) ──
        Image(
            painter = painterResource(id = R.drawable.bujo_splash_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        Modifier.blur(8.dp)
                    else Modifier.alpha(0.4f)
                ),
        )

        // ── Layer 2: Translucent gradient overlay (same as SuccessScreen) ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(WofBgTop, WofBgBottom))),
        )

        // ── Layer 3: Content ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ── Top bar: back arrow + title + coin count ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "High Scores",
                    color = HeaderText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.weight(1f))
                // Coin icon + count
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.coin_stack),
                        contentDescription = "Coins",
                        modifier = Modifier.size(30.dp),
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "$coins",
                        color = CoinColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Scrollable leaderboard rows with single-line separators ──
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
            ) {
                leaderboard.forEachIndexed { index, (name, rank) ->
                    val isPlayer = rank == pRank
                    val rowColor = if (isPlayer) PlayerText else NameText
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 18.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = name,
                            color = rowColor,
                            fontSize = if (isPlayer) 17.sp else 16.sp,
                            fontWeight = if (isPlayer) FontWeight.Black else FontWeight.Light,
                        )
                        Text(
                            text = "#$rank",
                            color = rowColor,
                            fontSize = if (isPlayer) 17.sp else 16.sp,
                            fontWeight = if (isPlayer) FontWeight.Black else FontWeight.Light,
                        )
                    }
                    // Divider line after each row (except the last)
                    if (index < leaderboard.size - 1) {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = DividerColor,
                        )
                    }
                }
            }
        }
    }
}
