package com.bujo.movies.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bujo.movies.R
import com.bujo.movies.ui.theme.LegacyCream
import com.bujo.movies.ui.theme.LegacyCrimson
import com.bujo.movies.ui.theme.LegacyDark

const val FEEDBACK_EMAIL = "amitseetha@gmail.com"

@Composable
fun AllCaughtUpScreen(
    onSettings: () -> Unit,
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LegacyCream),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.all_caught_up),
                color = LegacyCrimson,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.all_caught_up_body),
                color = LegacyDark,
                fontSize = 18.sp,
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:$FEEDBACK_EMAIL")
                        putExtra(Intent.EXTRA_SUBJECT, "Bujo feedback")
                        putExtra(Intent.EXTRA_TEXT, "")
                    }
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {
                    }
                },
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LegacyCrimson,
                    contentColor = Color.White,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Text(
                    stringResource(R.string.send_feedback),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onSettings,
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, LegacyCrimson),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LegacyCrimson),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Text(
                    stringResource(R.string.settings),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
