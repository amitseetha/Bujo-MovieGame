package com.bujo.movies.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bujo.movies.R
import com.bujo.movies.data.ProfileStore
import com.bujo.movies.ui.theme.BrandOffWhite
import com.bujo.movies.ui.theme.BrandPink
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    ready: Boolean,
    initialUsername: String,
    onContinue: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf(initialUsername) }

    // Prefill once the profile flow resolves with an existing username.
    LaunchedEffect(initialUsername) {
        if (initialUsername.isNotBlank() && username.isBlank()) {
            username = initialUsername
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandPink),
    ) {
        // Fullscreen background with baked-in BUJO wordmark + wreath.
        Image(
            painter = painterResource(id = R.drawable.bujo_splash_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // Username input + Start button overlaid on the lower half of the splash.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 96.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(stringResource(R.string.username_hint)) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = BrandOffWhite,
                    unfocusedTextColor = BrandOffWhite,
                    focusedBorderColor = BrandOffWhite,
                    unfocusedBorderColor = BrandOffWhite.copy(alpha = 0.6f),
                    focusedLabelColor = BrandOffWhite,
                    unfocusedLabelColor = BrandOffWhite.copy(alpha = 0.75f),
                    cursorColor = BrandOffWhite,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    val name = username.trim()
                    if (name.isNotEmpty()) {
                        scope.launch {
                            ProfileStore.setUsername(context, name)
                            onContinue()
                        }
                    }
                },
                enabled = ready && username.trim().isNotEmpty(),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandOffWhite,
                    contentColor = BrandPink,
                    disabledContainerColor = BrandOffWhite.copy(alpha = 0.5f),
                    disabledContentColor = BrandPink.copy(alpha = 0.6f),
                ),
                modifier = Modifier.size(width = 240.dp, height = 56.dp),
            ) {
                Text(
                    text = stringResource(R.string.start),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
