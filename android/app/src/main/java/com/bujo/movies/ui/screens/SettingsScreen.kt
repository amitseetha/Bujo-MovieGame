package com.bujo.movies.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bujo.movies.R
import com.bujo.movies.data.ProfileStore
import com.bujo.movies.ui.theme.BrandBrown
import com.bujo.movies.ui.theme.BrandDark
import com.bujo.movies.ui.theme.BrandRed
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(
            text = stringResource(R.string.settings),
            color = BrandRed,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.sound), color = BrandDark)
            Spacer(Modifier.weight(1f))
            Switch(
                checked = profile?.soundOn ?: true,
                onCheckedChange = { on ->
                    scope.launch { ProfileStore.setSoundOn(context, on) }
                },
            )
        }
        Spacer(Modifier.height(8.dp))
        Divider()

        TextButton(onClick = onAbout, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.about), color = BrandDark)
        }
        TextButton(onClick = onPrivacy, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.privacy), color = BrandDark)
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.version_label),
            color = BrandBrown,
            fontSize = 12.sp,
        )

        Spacer(Modifier.weight(1f))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}
