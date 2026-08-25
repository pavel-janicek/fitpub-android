package com.fitpub.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fitpub.android.ui.AppViewModel
import com.fitpub.android.ui.auth.LoginContent
import com.fitpub.android.ui.auth.LoginViewModel
import com.fitpub.android.ui.auth.PasswordResetContent
import com.fitpub.android.ui.auth.PasswordResetViewModel
import com.fitpub.android.ui.auth.RegisterContent
import com.fitpub.android.ui.auth.RegisterViewModel
import com.fitpub.android.ui.auth.ServerSetupContent
import com.fitpub.android.ui.auth.ServerSetupViewModel
import com.fitpub.android.ui.auth.VerifyCodeContent
import com.fitpub.android.ui.navigation.Routes
import com.fitpub.android.ui.theme.FitPubTheme

class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels { AppViewModel.factory(FitPubApplication.container(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = FitPubApplication.container(this)
        setContent {
            FitPubTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val state by appViewModel.uiState.collectAsState()
                    when {
                        !state.loaded -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                        !state.configured -> ServerSetupRoute(container)
                        !state.loggedIn && !state.guest -> AuthFlowRoute(container, appViewModel)
                        else -> MainAppRoute(container, appViewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun ServerSetupRoute(
    container: AppContainer,
    initialUrl: String? = null,
    allowSkip: Boolean = true,
    onDone: () -> Unit = {},
    onCancel: (() -> Unit)? = null,
) {
    val vm: ServerSetupViewModel = viewModel(factory = ServerSetupViewModel.factory(container))
    val done by vm.done.collectAsState()
    val busy by vm.busy.collectAsState()
    val error by vm.error.collectAsState()
    LaunchedEffect(done) { if (done) onDone() }
    ServerSetupContent(
        busy = busy,
        hint = error,
        initialUrl = initialUrl,
        onSave = vm::connect,
        onSkip = if (allowSkip) ({ vm.skip() }) else null,
        onCancel = onCancel,
    )
}
@Composable
private fun AuthFlowRoute(container: AppContainer, appViewModel: AppViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            val vm: LoginViewModel = viewModel(factory = LoginViewModel.factory(container))
            val busy by vm.busy.collectAsState()
            val error by vm.error.collectAsState()
            val success by vm.success.collectAsState()
            val st by appViewModel.uiState.collectAsState()
            if (success == true) return@composable
            LoginContent(
                busy = busy,
                error = error,
                serverUrl = st.serverUrl,
                onLogin = vm::login,
                onOpenRegister = { navController.navigate(Routes.REGISTER) },
                onOpenPasswordReset = { navController.navigate(Routes.PASSWORD_RESET) },
                onChangeServer = { navController.navigate(Routes.SERVER_SETUP) },
                onBrowseAsGuest = vm::browseAsGuest,
            )
        }
        composable(Routes.SERVER_SETUP) {
            val st by appViewModel.uiState.collectAsState()
            ServerSetupRoute(
                container = container,
                initialUrl = st.serverUrl.takeIf { it.isNotBlank() },
                allowSkip = false,
                onDone = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
            )
        }
        composable(Routes.REGISTER) {
            RegisterRoute(container = container, onBack = { navController.popBackStack() })
        }
        composable(Routes.PASSWORD_RESET) {
            val vm: PasswordResetViewModel = viewModel(factory = PasswordResetViewModel.factory(container))
            val busy by vm.busy.collectAsState()
            val error by vm.error.collectAsState()
            val requested by vm.requested.collectAsState()
            PasswordResetContent(
                busy = busy,
                error = error,
                requested = requested,
                onRequest = vm::request,
                onBack = { navController.popBackStack() },
            )
        }
    }
}

@Composable
private fun RegisterRoute(container: AppContainer, onBack: () -> Unit) {
    val vm: RegisterViewModel = viewModel(factory = RegisterViewModel.factory(container))
    val busy by vm.busy.collectAsState()
    val error by vm.error.collectAsState()
    val status by vm.registrationStatus.collectAsState()
    val awaitingCode by vm.awaitingCode.collectAsState()
    val verified by vm.verified.collectAsState()
    var pendingEmail by remember { mutableStateOf("") }

    when {
        verified == true -> return
        awaitingCode -> VerifyCodeContent(
            email = pendingEmail,
            busy = busy,
            error = error,
            onVerify = { email, code -> vm.verify(email, code) },
            onResend = { email -> vm.resend(email) },
        )
        else -> RegisterContent(
            busy = busy,
            error = error,
            status = status,
            onStart = { username, email, password, displayName, timezone ->
                pendingEmail = email
                vm.start(username, email, password, displayName, null, timezone, null)
            },
            onBack = onBack,
        )
    }
}

@Composable
private fun MainAppRoute(container: AppContainer, appViewModel: AppViewModel) {
    val navController = rememberNavController()
    FitPubNavGraph(navController = navController, container = container, appViewModel = appViewModel)
}
@Composable
private fun FitPubNavGraph(
    navController: NavHostController,
    container: AppContainer,
    appViewModel: AppViewModel,
) {
    NavHost(navController = navController, startDestination = Routes.MAIN) {
        composable(Routes.MAIN) {
            com.fitpub.android.ui.main.MainScaffold(
                container = container,
                appViewModel = appViewModel,
                onOpenActivity = { id -> navController.navigate(Routes.activityDetail(id)) },
                onOpenProfile = { username -> navController.navigate(Routes.profile(username)) },
                onOpenCreate = { navController.navigate(Routes.CREATE) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(
            route = Routes.ACTIVITY_DETAIL,
            arguments = listOf(navArgument("activityId") { }),
        ) { entry ->
            val activityId = entry.arguments?.getString("activityId").orEmpty()
            com.fitpub.android.ui.activity.ActivityDetailScreen(
                activityId = activityId,
                container = container,
                appViewModel = appViewModel,
                onBack = { navController.popBackStack() },
                onOpenProfile = { username -> navController.navigate(Routes.profile(username)) },
            )
        }
        composable(
            route = Routes.PROFILE,
            arguments = listOf(navArgument("username") { }),
        ) { entry ->
            val username = entry.arguments?.getString("username").orEmpty().ifBlank { Routes.ME }
            com.fitpub.android.ui.profile.ProfileScreen(
                username = username,
                container = container,
                appViewModel = appViewModel,
                embedded = false,
                onBack = { navController.popBackStack() },
                onOpenActivity = { id -> navController.navigate(Routes.activityDetail(id)) },
                onEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.CREATE) {
            com.fitpub.android.ui.create.CreateActivityScreen(
                container = container,
                appViewModel = appViewModel,
                onDone = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
            )
        }
        composable(Routes.EDIT_PROFILE) {
            com.fitpub.android.ui.profile.EditProfileScreen(
                container = container,
                appViewModel = appViewModel,
                onDone = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
            )
        }
        composable(Routes.SETTINGS) {
            com.fitpub.android.ui.settings.SettingsScreen(
                container = container,
                appViewModel = appViewModel,
                onBack = { navController.popBackStack() },
                onOpenPrivacyZones = { navController.navigate(Routes.PRIVACY_ZONES) },
                onChangeInstance = { navController.navigate(Routes.SERVER_SETUP) },
            )
        }
        composable(Routes.SERVER_SETUP) {
            val st by appViewModel.uiState.collectAsState()
            ServerSetupRoute(
                container = container,
                initialUrl = st.serverUrl.takeIf { it.isNotBlank() },
                allowSkip = false,
                onDone = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
            )
        }
        composable(Routes.PRIVACY_ZONES) {
            com.fitpub.android.ui.settings.PrivacyZonesScreen(
                container = container,
                appViewModel = appViewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
