package com.bujo.movies.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bujo.movies.R
import com.bujo.movies.ui.theme.LegacyCream
import com.bujo.movies.ui.theme.LegacyCrimson
import com.bujo.movies.ui.theme.LegacyDark

@Composable
fun AboutScreen(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LegacyCream),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
        ) {
            Text(
                text = "BUJO",
                color = LegacyCrimson,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.tagline),
                color = LegacyDark,
                fontSize = 16.sp,
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.about_body_1),
                color = LegacyDark,
                fontSize = 16.sp,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.about_body_2),
                color = LegacyDark,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.weight(1f))
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, LegacyCrimson),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LegacyCrimson),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Text(
                    "Back",
                    color = LegacyCrimson,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
