package com.bujo.movies.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.keyframes
import android.media.SoundPool
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxHeight
import android.os.Build
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bujo.movies.R
import com.bujo.movies.data.ProfileStore
import com.bujo.movies.data.Question

// ── Palette for Success screen ──────────────────────────────────────────
private val SuccessBgTop = Color(0xCCB76460)         // gradient top (~80% opaque)
private val SuccessBgBottom = Color(0xCCB86667)      // gradient bottom (~80% opaque)
private val BannerBg = Color(0xDDD07E7F)             // warm pink banner (~87% opaque)
private val CorrectGreen = Color(0xFF7EC850)        // "CORRECT" text + stars
private val ButtonGreen = Color(0xFFA3DB76)         // Continue button
private val TitleGold = Color(0xFFF4CF54)           // coin reward text
private val CorrectTextColor = Color(0xFFE8C44A)    // CORRECT text (gold)
private val MovieTitleColor = Color(0xFF7EC850)     // movie title (green)
private val TriviaColor = Color(0xFFE9D2D1)         // trivia quote
private val TextWhite = Color(0xFFFFFFFF)
private val TextLight = Color(0xFFF5E8E8)           // lighter body text

@Composable
fun SuccessScreen(
    question: Question?,
    onContinue: () -> Unit,
) {
    if (question == null) {
        Button(onClick = onContinue) { Text(stringResource(R.string.continue_label)) }
        return
    }
    // ── Coin reward sound ──
    val context = LocalContext.current
    val soundPool = remember {
        SoundPool.Builder().setMaxStreams(1).build()
    }
    val soundId = remember { soundPool.load(context, R.raw.mixkit_clinking_coins_1993, 1) }
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { soundPool.release() }
    }

    // ── Beat 1: Coins bounce-drop ──
    // Coins start 30dp above and drop into place with a bounce
    val coinOffsetY = remember { Animatable(-30f) }  // dp offset
    val coinAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        coinAlpha.animateTo(1f, animationSpec = tween(150))
    }
    LaunchedEffect(Unit) {
        coinOffsetY.animateTo(
            targetValue = 0f,
            animationSpec = keyframes {
                durationMillis = 500
                -30f at 0       // start above
                2f at 250       // overshoot past resting (slight bounce down)
                -3f at 375      // bounce back up
                0f at 500       // settle
            },
        )
    }

    // ── Beat 2: "+4" scale-pop with upward drift (200ms after coins land) ──
    val plusFourScale = remember { Animatable(0f) }
    val plusFourAlpha = remember { Animatable(0f) }
    val plusFourDriftY = remember { Animatable(14f) }  // starts 14dp below, drifts up

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(500L)  // wait for coins to land
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)  // play coin sound
        plusFourAlpha.animateTo(1f, animationSpec = tween(150))
    }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(500L)
        plusFourScale.animateTo(
            targetValue = 1f,
            animationSpec = keyframes {
                durationMillis = 500
                0f at 0         // invisible
                1.35f at 200    // big overshoot pop
                0.92f at 350    // undershoot bounce
                1f at 500       // settle
            },
        )
    }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(500L)
        plusFourDriftY.animateTo(0f, animationSpec = tween(500))
    }

    // ── Beat 3: "CORRECT" text shake & settle ──
    val correctShakeX = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(300L)
        correctShakeX.animateTo(
            targetValue = 0f,
            animationSpec = keyframes {
                durationMillis = 400
                0f at 0
                5f at 50       // right
                -5f at 120     // left
                4f at 190      // right (smaller)
                -3f at 260     // left (smaller)
                2f at 320      // right (tiny)
                0f at 400      // settle
            },
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Layer 1: Blurred snapshot mosaic background
        if (question.snapshotFilenames.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                            Modifier.blur(20.dp)
                        else Modifier.alpha(0.3f)  // fallback for older devices
                    ),
            ) {
                val snapshots = question.snapshotFilenames
                // Fill the screen with snapshot images in a 2×2 grid, each taking half
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    snapshots.getOrNull(0)?.let { fname ->
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data("file:///android_asset/images/$fname")
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                    snapshots.getOrNull(1)?.let { fname ->
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data("file:///android_asset/images/$fname")
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                }
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    snapshots.getOrNull(2)?.let { fname ->
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data("file:///android_asset/images/$fname")
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                    snapshots.getOrNull(3)?.let { fname ->
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data("file:///android_asset/images/$fname")
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                }
            }
        }

        // Layer 2: Semi-transparent gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(SuccessBgTop, SuccessBgBottom))),
        )

        // Layer 3: Content — positioned by screen-height percentages
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
        ) {
            val screenHeight = maxHeight

            // ── Coin stack image + reward text (above the banner) ──
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight * 0.165f)
                    .align(Alignment.TopCenter),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.coin_stack),
                    contentDescription = "Coins earned",
                    modifier = Modifier
                        .width(150.dp)
                        .height(100.dp)
                        .offset(y = coinOffsetY.value.dp)
                        .graphicsLayer { alpha = coinAlpha.value },
                    contentScale = ContentScale.Fit,
                )
                Image(
                    painter = painterResource(id = R.drawable.plus_four),
                    contentDescription = "+${ProfileStore.COINS_PER_CORRECT}",
                    modifier = Modifier
                        .width(120.dp)
                        .height(88.dp)
                        .align(Alignment.BottomCenter)
                        .padding(start = 30.dp, bottom = 0.dp)
                        .offset(y = plusFourDriftY.value.dp)
                        .graphicsLayer {
                            scaleX = plusFourScale.value
                            scaleY = plusFourScale.value
                            alpha = plusFourAlpha.value
                        },
                    contentScale = ContentScale.Fit,
                )
            }

            // ── "CORRECT" banner: 16.5% → 33.8% ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = screenHeight * 0.165f)
                    .height(screenHeight * (0.338f - 0.165f))
                    .background(BannerBg),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        // Left star
                        Image(
                            painter = painterResource(id = R.drawable.ic_star_green),
                            contentDescription = null,
                            modifier = Modifier
                                .size(79.dp)
                                .align(Alignment.CenterStart),
                        )
                        // CORRECT text (shake & settle)
                        Text(
                            text = "CORRECT",
                            color = CorrectTextColor,
                            fontSize = 55.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.offset(x = correctShakeX.value.dp),
                        )
                        // Right star
                        Image(
                            painter = painterResource(id = R.drawable.ic_star_green),
                            contentDescription = null,
                            modifier = Modifier
                                .size(79.dp)
                                .align(Alignment.CenterEnd),
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = question.successLine.uppercase(),
                        color = TextWhite,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )
                }
            }

            // ── Movie Poster: 37.4% → 56.9% ──
            if (question.posterFilename.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = screenHeight * 0.374f)
                        .height(screenHeight * (0.569f - 0.374f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 80.dp, vertical = 8.dp)
                            .shadow(12.dp, RoundedCornerShape(12.dp))
                            .border(
                                width = 9.dp,
                                color = Color(0xFFC48081),
                                shape = RoundedCornerShape(12.dp),
                            )
                            .clip(RoundedCornerShape(12.dp)),
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data("file:///android_asset/images/${question.posterFilename}")
                                .build(),
                            contentDescription = question.movieTitle,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }

            // ── Movie title + trivia with wreath watermark ──
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = screenHeight * 0.58f)
                    .padding(horizontal = 32.dp),
            ) {
                // Column drives the layout (title + trivia determine the Box size)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = question.movieTitle.uppercase(),
                        color = MovieTitleColor,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.sp,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = question.triviaQuote,
                        color = TriviaColor,
                        fontSize = 19.sp,
                        textAlign = TextAlign.Center,
                        fontStyle = FontStyle.Normal,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                }
                // Wreath watermark: matchParentSize so it doesn't affect layout,
                // then graphicsLayer scale 3× to enlarge around its centre
                Image(
                    painter = painterResource(id = R.drawable.wreath_watermark_img),
                    contentDescription = null,
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer {
                            scaleX = 3f
                            scaleY = 3f
                        }
                        .alpha(0.25f),
                    contentScale = ContentScale.Fit,
                )
            }

            // ── Continue button: 80% → 90% ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = screenHeight * 0.80f)
                    .height(screenHeight * (0.90f - 0.80f)),
                contentAlignment = Alignment.Center,
            ) {
                Button(
                    onClick = onContinue,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ButtonGreen,
                        contentColor = TextWhite,
                    ),
                    modifier = Modifier
                        .width(200.dp)
                        .height(70.dp),
                ) {
                    Text(
                        stringResource(R.string.continue_label),
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Normal,
                    )
                }
            }
        }
    }
}
