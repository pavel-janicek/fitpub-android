package com.fitpub.android.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import com.fitpub.android.AppContainer
import com.fitpub.android.ui.auth.LoginContent
import com.fitpub.android.ui.auth.ServerSetupContent
import com.fitpub.android.ui.timeline.TimelineScreen
import org.junit.Rule
import org.junit.Test

class AuthAndTimelineSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun serverSetup_acceptsInstanceUrl() {
        composeRule.setContent {
            ServerSetupContent(busy = false, hint = null, onSave = {})
        }

        composeRule.onNodeWithText("Instance URL").performTextInput("https://example.test")
        composeRule.onNodeWithText("Connect").assertIsDisplayed()
    }

    @Test
    fun login_rendersPrimaryActions() {
        composeRule.setContent {
            LoginContent(
                busy = false,
                error = null,
                serverUrl = "https://example.test",
                onLogin = { _, _ -> },
                onOpenRegister = {},
                onOpenPasswordReset = {},
                onChangeServer = {},
                onBrowseAsGuest = {},
            )
        }

        composeRule.onNodeWithText("Log in").assertIsDisplayed()
        composeRule.onNodeWithText("Sign in").assertIsDisplayed()
        composeRule.onNodeWithText("Browse public timeline without an account").assertIsDisplayed()
    }

    @Test
    fun timeline_rendersSearchAndRefreshControls() {
        composeRule.setContent {
            TimelineScreen(
                container = AppContainer(composeRule.activity),
                unitSystem = "METRIC",
                guestMode = true,
                onOpenActivity = {},
                onOpenProfile = {},
                onOpenCreate = {},
                onRequireSignIn = {},
            )
        }

        composeRule.onNodeWithText("FitPub").assertIsDisplayed()
        composeRule.onNodeWithText("Search activities").assertIsDisplayed()
    }
}
