package com.example.kmpincidents.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.example.kmpincidents.data.model.Role
import com.example.kmpincidents.data.repository.AuthRepository
import com.example.kmpincidents.data.store.TokenPreferences
import com.example.kmpincidents.generated.resources.Res
import com.example.kmpincidents.generated.resources.desktop_login_error
import com.example.kmpincidents.ui.screens.auth.LoginScreen
import com.example.kmpincidents.ui.screens.auth.UserProfileScreen
import com.example.kmpincidents.ui.screens.desktop.DesktopHomeScreen
import com.example.kmpincidents.ui.screens.management.IncidentDetailScreen
import com.example.kmpincidents.viewmodel.LoginViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private val desktopNavSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(LoginKey::class)
            subclass(DesktopHomeKey::class)
            subclass(UserProfileKey::class)
            subclass(IncidentDetailKey::class)
        }
    }
}

@Composable
fun DesktopAppNavigation() {
    val backStack = rememberNavBackStack(desktopNavSavedStateConfiguration, LoginKey)
    val scope = rememberCoroutineScope()
    val tokenPreferences: TokenPreferences = koinInject()
    val authRepository: AuthRepository = koinInject()

    fun goToLogin() {
        backStack.removeAll { true }
        backStack.add(LoginKey)
    }

    fun goToHome() {
        backStack.removeAll { true }
        backStack.add(DesktopHomeKey)
    }

    suspend fun ensureOfficialOrReject(loginViewModel: LoginViewModel): Boolean {
        val role = tokenPreferences.getUserRole()
        return if (role == Role.OFFICIAL) {
            true
        } else {
            loginViewModel.rejectAuthenticatedSession(Res.string.desktop_login_error)
            false
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<LoginKey> {
                val loginViewModel: LoginViewModel = koinViewModel()
                LoginScreen(
                    viewModel = loginViewModel,
                    onNavigateToIncidentList = {
                        scope.launch {
                            if (ensureOfficialOrReject(loginViewModel)) {
                                goToHome()
                            }
                        }
                    },
                    // Anonymous reporting is not part of the official desktop workflow.
                    onNavigateToReport = { },
                    onNavigateToRegister = { }
                )
            }

            entry<DesktopHomeKey> {
                DesktopHomeScreen(
                    onNavigateToProfile = { userJson ->
                        backStack.add(UserProfileKey(userJson))
                    },
                    onLogout = { goToLogin() }
                )
            }

            entry<UserProfileKey> {
                UserProfileScreen(
                    userJson = it.userJson,
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }

            entry<IncidentDetailKey> {
                IncidentDetailScreen(
                    incidentId = it.incidentId,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToMyIncidentList = {
                        scope.launch {
                            authRepository.logout()
                            goToLogin()
                        }
                    }
                )
            }
        }
    )
}
