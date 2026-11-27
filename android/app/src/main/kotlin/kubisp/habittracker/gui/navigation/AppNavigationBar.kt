package kubisp.habittracker.gui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

/** UI-NAV-BAR: the label is always shown, so the icon carries no description of its own. */
@Composable
fun AppNavigationBar(selectedTab: AppTab?, onTabSelected: (AppTab) -> Unit) {
    NavigationBar {
        AppTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                icon = { Icon(painter = painterResource(tab.icon), contentDescription = null) },
                label = { Text(text = stringResource(tab.label)) }
            )
        }
    }
}
