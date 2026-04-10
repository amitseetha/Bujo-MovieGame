package com.bujo.movies.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.bujo.movies.R
import com.bujo.movies.data.Question
import com.bujo.movies.ui.theme.BrandCream
import com.bujo.movies.ui.theme.BrandDark
import com.bujo.movies.ui.theme.BrandRed

@Composable
fun SuccessScreen(
    question: Question?,
    onContinue: () -> Unit,
) {
    if (question == null) {
        Button(onClick = onContinue) { Text(stringResource(R.string.continue_label)) }
        return
    }
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("lottie/success.json"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        LottieAnimation(
            composition = composition,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            iterations = 1,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = question.successLine,
            color = BrandRed,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = question.movieTitle,
            color = BrandDark,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(20.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = BrandCream),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = question.triviaQuote,
                    color = BrandDark,
                    fontSize = 14.sp,
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        if (question.posterFilename.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("file:///android_asset/images/${question.posterFilename}")
                    .build(),
                contentDescription = question.movieTitle,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "+50 coins",
            color = BrandRed,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onContinue) {
            Text(stringResource(R.string.continue_label))
        }
    }
}
