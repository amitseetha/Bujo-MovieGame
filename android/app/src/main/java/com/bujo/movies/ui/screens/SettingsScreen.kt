package com.bujo.movies.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bujo.movies.R
import com.bujo.movies.data.ProfileStore
import com.bujo.movies.ui.theme.LegacyCream
import com.bujo.movies.ui.theme.LegacyCrimson
import com.bujo.movies.ui.theme.LegacyDark
import com.bujo.movies.ui.theme.LegacyMuted
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onAbout: () -> Unit,
    onPrivacy: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val profile by ProfileStore.profileFlow(context).collectAsState(initial = null)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LegacyCream),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
        ) {
            Text(
                text = stringResource(R.string.settings),
                color = LegacyCrimson,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
            )
            Spacer(Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.sound),
                    color = LegacyDark,
                    fontSize = 18.sp,
                )
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = profile?.soundOn ?: true,
                    onCheckedChange = { on ->
                        scope.launch { ProfileStore.setSoundOn(context, on) }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = LegacyCrimson,
                        checkedBorderColor = LegacyCrimson,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = LegacyMuted.copy(alpha = 0.5f),
                        uncheckedBorderColor = LegacyMuted,
                    ),
                )
            }
            Spacer(Modifier.height(8.dp))
            Divider(color = LegacyMuted.copy(alpha = 0.4f))

            TextButton(
                onClick = onAbout,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    stringResource(R.string.about),
                    color = LegacyDark,
                    fontSize = 16.sp,
                )
            }
            TextButton(
                onClick = onPrivacy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    stringResource(R.string.privacy),
                    color = LegacyDark,
                    fontSize = 16.sp,
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.version_label),
                color = LegacyMuted,
                fontSize = 14.sp,
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
