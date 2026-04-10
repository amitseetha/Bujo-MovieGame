package com.bujo.movies.nav

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bujo.movies.data.ContentLoader
import com.bujo.movies.data.ContentPack
import com.bujo.movies.data.ProfileStore
import com.bujo.movies.ui.screens.AboutScreen
import com.bujo.movies.ui.screens.AllCaughtUpScreen
import com.bujo.movies.ui.screens.PrivacyScreen
import com.bujo.movies.ui.screens.QuestionScreen
import com.bujo.movies.ui.screens.SettingsScreen
import com.bujo.movies.ui.screens.SplashScreen
import com.bujo.movies.ui.screens.SuccessScreen
import com.bujo.movies.ui.screens.UsernameScreen
import kotlinx.coroutines.flow.firstOrNull

object Routes {
    const val SPLASH = "splash"
    const val USERNAME = "username"
    const val QUESTION = "question"
    const val SUCCESS = "success/{qid}"
    const val ALL_CAUGHT_UP = "all_caught_up"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val PRIVACY = "privacy"
    fun success(qid: Int) = "success/$qid"
}

@Composable
fun BujoApp() {
    val context = LocalContext.current
    val nav = rememberNavController()

    // Load bundled content once.
    val content: ContentPack = remember { ContentLoader.load(context) }

    // Resolve initial screen: splash → (username or question or all-caught-up).
    var initialResolved by remember { mutableStateOf(false) }
    var startRoute by remember { mutableStateOf(Routes.SPLASH) }

    LaunchedEffect(Unit) {
        val profile = ProfileStore.profileFlow(context).firstOrNull()
        startRoute = when {
            profile?.username.isNullOrBlank() -> Routes.USERNAME
            profile!!.answeredIds.size >= content.questions.size -> Routes.ALL_CAUGHT_UP
            else -> Routes.QUESTION
        }
        initialResolved = true
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = nav,
            startDestination = Routes.SPLASH,
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(
                    ready = initialResolved,
                    onContinue = {
                        nav.navigate(startRoute) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    },
                )
            }
            composable(Routes.USERNAME) {
                UsernameScreen(
                    onDone = {
                        nav.navigate(Routes.QUESTION) {
                            popUpTo(Routes.USERNAME) { inclusive = true }
                        }
                    },
                    onSettings = { nav.navigate(Routes.SETTINGS) },
                )
            }
            composable(Routes.QUESTION) {
                QuestionScreen(
                    content = content,
                    onCorrect = { qid ->
                        nav.navigate(Routes.success(qid)) {
                            popUpTo(Routes.QUESTION) { inclusive = true }
                        }
                    },
                    onAllDone = {
                        nav.navigate(Routes.ALL_CAUGHT_UP) {
                            popUpTo(Routes.QUESTION) { inclusive = true }
                        }
                    },
                    onSettings = { nav.navigate(Routes.SETTINGS) },
                )
            }
            composable(Routes.SUCCESS) { backStack ->
                val qid = backStack.arguments?.getString("qid")?.toIntOrNull() ?: -1
                val q = content.questions.firstOrNull { it.id == qid }
                SuccessScreen(
                    question = q,
                    onContinue = {
                        nav.navigate(Routes.QUESTION) {
                            popUpTo(Routes.SUCCESS) { inclusive = true }
                        }
                    },
                )
            }
            composable(Routes.ALL_CAUGHT_UP) {
                AllCaughtUpScreen(
                    onSettings = { nav.navigate(Routes.SETTINGS) },
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onAbout = { nav.navigate(Routes.ABOUT) },
                    onPrivacy = { nav.navigate(Routes.PRIVACY) },
                    onBack = { nav.popBackStack() },
                )
            }
            composable(Routes.ABOUT) {
                AboutScreen(onBack = { nav.popBackStack() })
            }
            composable(Routes.PRIVACY) {
                PrivacyScreen(onBack = { nav.popBackStack() })
            }
        }
    }

    // Auto-advance past splash once initial route is resolved.
    LaunchedEffect(initialResolved) {
        if (initialResolved) {
            nav.navigate(startRoute) {
                popUpTo(Routes.SPLASH) { inclusive = true }
            }
        }
    }

    // Collect profile once to ensure DataStore is warm (side effect).
    ProfileStore.profileFlow(context).collectAsState(initial = null).value
}
