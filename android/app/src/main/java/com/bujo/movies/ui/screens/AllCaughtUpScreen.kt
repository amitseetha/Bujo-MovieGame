package com.bujo.movies.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bujo.movies.R
import com.bujo.movies.ui.theme.BrandDark
import com.bujo.movies.ui.theme.BrandRed

const val FEEDBACK_EMAIL = "amitseetha@gmail.com"

@Composable
fun AllCaughtUpScreen(
    onSettings: () -> Unit,
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.all_caught_up),
            color = BrandRed,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.all_caught_up_body),
            color = BrandDark,
            fontSize = 16.sp,
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
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.send_feedback))
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onSettings,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.settings))
        }
    }
}
