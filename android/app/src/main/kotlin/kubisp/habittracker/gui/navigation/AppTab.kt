package kubisp.habittracker.gui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kubisp.habittracker.R

/**
 * The tabs of the navigation bar (UI-NAV-BAR, `docs/UX.md` section 2). Only the tabs that work are listed
 * (`docs/PLAN.md`, Conventions); Statistics joins in T4.6. The first tab is the start screen.
 */
enum class AppTab(val route: String, @StringRes val label: Int, @DrawableRes val icon: Int) {
    DAY(route = "day", label = R.string.ui_nav_bar_day, icon = R.drawable.ic_tab_day),
    HABITS(route = "habits", label = R.string.ui_nav_bar_habits, icon = R.drawable.ic_tab_habits);

    companion object {
        /** Null before the first destination is shown. */
        fun ofRoute(route: String?): AppTab? = entries.find { it.route == route }
    }
}
