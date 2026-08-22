package com.example.kmpincidents.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.example.kmpincidents.ui.screens.auth.LoginScreen
import com.example.kmpincidents.ui.screens.auth.RegisterScreen
import com.example.kmpincidents.ui.screens.auth.UserProfileScreen
import com.example.kmpincidents.ui.screens.incidents.MyIncidentDetailScreen
import com.example.kmpincidents.ui.screens.incidents.MyIncidentListScreen
import com.example.kmpincidents.ui.screens.management.IncidentDetailScreen
import com.example.kmpincidents.ui.screens.management.AllIncidentListScreen
import com.example.kmpincidents.ui.screens.management.IncidentMapScreen
import com.example.kmpincidents.ui.screens.management.UserManagementScreen
import com.example.kmpincidents.ui.screens.incidents.ReportIncidentScreen
import com.example.kmpincidents.ui.screens.stats.StatsScreen
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

private val navSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(LoginKey::class)
            subclass(MyIncidentListKey::class)
            subclass(RegisterKey::class)
            subclass(MyIncidentDetailKey::class)
            subclass(ReportIncidentKey::class)
            subclass(IncidentListKey::class)
            subclass(IncidentMapKey::class)
            subclass(UserManagementKey::class)
            subclass(StatsKey::class)
            subclass(UserProfileKey::class)
            subclass(IncidentDetailKey::class)
        }
    }
}

@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(navSavedStateConfiguration, LoginKey)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<LoginKey> {
                LoginScreen(
                    onNavigateToIncidentList = {
                        backStack.removeAll { true }
                        backStack.add(MyIncidentListKey)
                    },
                    onNavigateToReport = { backStack.add(ReportIncidentKey) },
                    onNavigateToRegister = { backStack.add(RegisterKey) }
                )
            }

            entry<RegisterKey> {
                RegisterScreen(
                    onNavigateToLogin = {
                        backStack.removeLastOrNull() }
                )
            }

            entry<MyIncidentListKey> {
                MyIncidentListScreen(
                    backStack = backStack,
                    onNavigateToDetail = { backStack.add(MyIncidentDetailKey) },
                    onNavigateToUserProfile = { userJson ->
                        backStack.add(UserProfileKey(userJson))
                    },
                    onNavigateToReport = { backStack.add(ReportIncidentKey) },
                    onLogout = {
                        backStack.removeAll { true }
                        backStack.add(LoginKey)
                    },
                    onNavigateToIncidentList = {
                        backStack.removeAll { it !is IncidentListKey }
                        if (backStack.none { it is IncidentListKey }) {
                            backStack.add(IncidentListKey)
                        }
                    },
                    onNavigateToIncidentMap = {
                        backStack.removeAll { it !is IncidentMapKey }
                        if (backStack.none { it is IncidentMapKey }) {
                            backStack.add(IncidentMapKey)
                        }
                    },
                    onNavigateToUserManagement = {
                        backStack.removeAll { it !is UserManagementKey }
                        if (backStack.none { it is UserManagementKey }) {
                            backStack.add(UserManagementKey)
                        }
                    },
                    onNavigateToStats = {
                        backStack.removeAll { it !is StatsKey }
                        if (backStack.none { it is StatsKey }) {
                            backStack.add(StatsKey)
                        }
                    }
                )
            }

            entry<MyIncidentDetailKey> {
                MyIncidentDetailScreen(
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            entry<ReportIncidentKey> {
                ReportIncidentScreen(
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    },
                    onNavigateToLogin = {
                        backStack.removeAll { true }
                        backStack.add(LoginKey)
                    },
                    onNavigateToIncidentList = {
                        backStack.removeAll { true }
                        backStack.add(MyIncidentListKey)
                    }
                )
            }

            entry<IncidentListKey> {
                AllIncidentListScreen(
                    onNavigateToDetail = { incidentId: Long ->
                        backStack.add(IncidentDetailKey(incidentId))
                    },
                    onNavigateToIncidentMap = {
                        backStack.removeAll { it !is IncidentMapKey }
                        if (backStack.none { it is IncidentMapKey }) {
                            backStack.add(IncidentMapKey)
                        }
                    },
                    onNavigateToUserManagement = {
                        backStack.removeAll { it !is UserManagementKey }
                        if (backStack.none { it is UserManagementKey }) {
                            backStack.add(UserManagementKey)
                        }
                    },
                    onNavigateToMyIncidentList = {
                        backStack.removeAll { it !is MyIncidentListKey }
                        if (backStack.none { it is MyIncidentListKey }) {
                            backStack.add(MyIncidentListKey)
                        }
                    },
                    onNavigateToStats = {
                        backStack.removeAll { it !is StatsKey }
                        if (backStack.none { it is StatsKey }) {
                            backStack.add(StatsKey)
                        }
                    }
                )
            }

            entry<IncidentMapKey> {
                IncidentMapScreen(
                    onNavigateToDetail = { incidentId : Long ->
                        backStack.add(IncidentDetailKey(incidentId))
                    },
                    onNavigateToMyIncidentList = {
                        backStack.removeAll { it !is MyIncidentListKey }
                        if (backStack.none { it is MyIncidentListKey }) {
                            backStack.add(MyIncidentListKey)
                        }
                    },
                    onNavigateToIncidentList = {
                        backStack.removeAll { it !is IncidentListKey }
                        if (backStack.none { it is IncidentListKey }) {
                            backStack.add(IncidentListKey)
                        }
                    },
                    onNavigateToUserManagement = {
                        backStack.removeAll { it !is UserManagementKey }
                        if (backStack.none { it is UserManagementKey }) {
                            backStack.add(UserManagementKey)
                        }
                    },
                    onNavigateToStats = {
                        backStack.removeAll { it !is StatsKey }
                        if (backStack.none { it is StatsKey }) {
                            backStack.add(StatsKey)
                        }
                    }
                )
            }

            entry<UserManagementKey> {
                UserManagementScreen(
                    onNavigateToMyIncidentList = {
                        backStack.removeAll { it !is MyIncidentListKey }
                        if (backStack.none { it is MyIncidentListKey }) {
                            backStack.add(MyIncidentListKey)
                        }
                    },
                    onNavigateToIncidentList = {
                        backStack.removeAll { it !is IncidentListKey }
                        if (backStack.none { it is IncidentListKey }) {
                            backStack.add(IncidentListKey)
                        }
                    },
                    onNavigateToIncidentMap = {
                        backStack.removeAll { it !is IncidentMapKey }
                        if (backStack.none { it is IncidentMapKey }) {
                            backStack.add(IncidentMapKey)
                        }
                    },
                    onNavigateToStats = {
                        backStack.removeAll { it !is StatsKey }
                        if (backStack.none { it is StatsKey }) {
                            backStack.add(StatsKey)
                        }
                    }
                )
            }

            entry<UserProfileKey> {
                UserProfileScreen(
                    userJson = it.userJson,
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }

            entry<StatsKey> {
                StatsScreen(
                    onNavigateToMyIncidentList = {
                        backStack.removeAll { it !is MyIncidentListKey }
                        if (backStack.none { it is MyIncidentListKey }) {
                            backStack.add(MyIncidentListKey)
                        }
                    },
                    onNavigateToIncidentList = {
                        backStack.removeAll { it !is IncidentListKey }
                        if (backStack.none { it is IncidentListKey }) {
                            backStack.add(IncidentListKey)
                        }
                    },
                    onNavigateToIncidentMap = {
                        backStack.removeAll { it !is IncidentMapKey }
                        if (backStack.none { it is IncidentMapKey }) {
                            backStack.add(IncidentMapKey)
                        }
                    },
                    onNavigateToUserManagement = {
                        backStack.removeAll { it !is UserManagementKey }
                        if (backStack.none { it is UserManagementKey }) {
                            backStack.add(UserManagementKey)
                        }
                    }
                )
            }

            entry<IncidentDetailKey> {
                IncidentDetailScreen(
                    incidentId = it.incidentId,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToMyIncidentList = {
                        backStack.removeAll { true }
                        backStack.add(MyIncidentListKey)
                    }
                )
            }
        }
    )
}

