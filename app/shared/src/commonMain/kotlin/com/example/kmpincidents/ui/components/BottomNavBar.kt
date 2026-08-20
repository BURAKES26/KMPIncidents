package com.example.kmpincidents.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.example.kmpincidents.generated.resources.Res
import com.example.kmpincidents.generated.resources.list
import com.example.kmpincidents.generated.resources.map
import com.example.kmpincidents.generated.resources.users
import com.example.kmpincidents.generated.resources.profile
import com.example.kmpincidents.generated.resources.stats
import com.example.kmpincidents.navigation.IncidentMapKey
import com.example.kmpincidents.navigation.MyIncidentListKey
import com.example.kmpincidents.navigation.UserManagementKey
import com.example.kmpincidents.navigation.IncidentListKey
import com.example.kmpincidents.navigation.StatsKey
import com.example.kmpincidents.core.data.model.Role
import com.example.kmpincidents.ui.icons.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

sealed class BottomNavItem(
    val key: NavKey,
    val titleRes: StringResource,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector,
    val requiredRoles: Set<Role>
) {
    data object List : BottomNavItem(
        key = IncidentListKey,
        titleRes = Res.string.list,
        unselectedIcon = ListIcon,
        selectedIcon = ListFilledIcon,
        requiredRoles = setOf(Role.OFFICIAL, Role.ADMIN)
    )

    data object Map : BottomNavItem(
        key = IncidentMapKey,
        titleRes = Res.string.map,
        unselectedIcon = MapIcon,
        selectedIcon = MapFilledIcon,
        requiredRoles = setOf(Role.OFFICIAL, Role.ADMIN)
    )

    data object Users : BottomNavItem(
        key = UserManagementKey,
        titleRes = Res.string.users,
        unselectedIcon = PeopleIcon,
        selectedIcon = PeopleFilledIcon,
        requiredRoles = setOf(Role.ADMIN)
    )

    data object Profile : BottomNavItem(
        key = MyIncidentListKey,
        titleRes = Res.string.profile,
        unselectedIcon = AccountCircleIcon,
        selectedIcon = AccountCircleFilledIcon,
        requiredRoles = setOf(Role.OFFICIAL, Role.ADMIN)
    )

    data object Stats : BottomNavItem(
        key = StatsKey,
        titleRes = Res.string.stats,
        unselectedIcon = MaterialSymbolsQueryStats,
        selectedIcon = MaterialSymbolsQueryStats,
        requiredRoles = setOf(Role.OFFICIAL, Role.ADMIN)
    )
}

@Composable
fun BottomNavBar(
    currentKey: NavKey,
    userRole: Role?,
    onNavigateTo: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    if (userRole == Role.USER || userRole == null) return

    val navItems = listOf(
        BottomNavItem.List,
        BottomNavItem.Map,
        BottomNavItem.Stats,
        BottomNavItem.Users,
        BottomNavItem.Profile
    ).filter { userRole in it.requiredRoles }

    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        navItems.forEach { item ->
            val isSelected = currentKey::class == item.key::class

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigateTo(item.key) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = stringResource(item.titleRes),
                        modifier = Modifier.size(32.dp)
                    )
                },
                label = {
                    Text(
                        text = stringResource(item.titleRes),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color(0xFF6B7280),
                    unselectedTextColor = Color(0xFF6B7280),
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ),
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
    }
}