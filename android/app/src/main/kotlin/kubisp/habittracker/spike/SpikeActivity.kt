package kubisp.habittracker.spike

import android.Manifest
import android.app.LocaleManager
import android.os.Bundle
import android.os.LocaleList
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kubisp.habittracker.R

/** Entry point of the spike prototypes (T1.4-T1.7), removed in T1.8. */
class SpikeActivity : ComponentActivity() {
    private val localeManager by lazy { getSystemService(LocaleManager::class.java) }
    private val habitNotifications by lazy { SpikeHabitNotifications(this) }
    private var activeState by mutableStateOf(ActiveNotificationsState())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val languageState =
            NotificationLanguageState(
                effectiveLocales = resources.configuration.locales.toLanguageTags(),
                appLocales = localeManager.applicationLocales.toLanguageTags()
            )
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SpikeScreen(
                        languageState = languageState,
                        activeState = activeState,
                        onSelectLanguage = ::selectLanguage,
                        onShowNotification = ::showNotification,
                        onActiveAction = ::handleActiveAction
                    )
                }
            }
        }
    }

    // Lists on every return to the foreground, so the list after a killed process or a force-stop is visible at once.
    override fun onResume() {
        super.onResume()
        listShown(ListTrigger.Resume)
    }

    // An empty tag list means "follow the device language".
    private fun selectLanguage(languageTags: String) {
        localeManager.applicationLocales = LocaleList.forLanguageTags(languageTags)
    }

    // Spike only: there is no Clock adapter yet (docs/DESIGN.md section 5.4); the code is removed in T1.8.
    private fun showNotification() {
        SpikeTimerNotification(this).showRunning(System.currentTimeMillis())
    }

    private fun handleActiveAction(action: ActiveAction) {
        when (action) {
            is ActiveAction.Show -> showHabit(action.habit)

            ActiveAction.List -> listShown(ListTrigger.Button)

            is ActiveAction.ShowAndList -> {
                showHabit(action.habit)
                listShown(ListTrigger.RightAfterNotify)
            }
        }
    }

    private fun showHabit(habit: String) {
        val isPosted = habitNotifications.show(habit, System.currentTimeMillis())
        activeState = activeState.copy(isPermissionMissing = !isPosted)
    }

    private fun listShown(trigger: ListTrigger) {
        activeState =
            activeState.copy(
                trigger = trigger,
                listedAtMillis = System.currentTimeMillis(),
                shown = habitNotifications.shown()
            )
    }
}

private data class NotificationLanguageState(val effectiveLocales: String, val appLocales: String)

private data class ActiveNotificationsState(
    val trigger: ListTrigger? = null,
    val listedAtMillis: Long = 0L,
    val shown: List<ShownNotification> = emptyList(),
    val isPermissionMissing: Boolean = false
)

private enum class ListTrigger { Resume, Button, RightAfterNotify }

private sealed interface ActiveAction {
    data class Show(val habit: String) : ActiveAction

    data object List : ActiveAction

    data class ShowAndList(val habit: String) : ActiveAction
}

@Composable
private fun SpikeScreen(
    languageState: NotificationLanguageState,
    activeState: ActiveNotificationsState,
    onSelectLanguage: (String) -> Unit,
    onShowNotification: () -> Unit,
    onActiveAction: (ActiveAction) -> Unit
) {
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    SpikeContent(
        languageState = languageState,
        activeState = activeState,
        onSelectLanguage = onSelectLanguage,
        onRequestPermission = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
        onShowNotification = onShowNotification,
        onActiveAction = onActiveAction
    )
}

@Composable
private fun SpikeContent(
    languageState: NotificationLanguageState,
    activeState: ActiveNotificationsState,
    onSelectLanguage: (String) -> Unit,
    onRequestPermission: () -> Unit,
    onShowNotification: () -> Unit,
    onActiveAction: (ActiveAction) -> Unit
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).safeDrawingPadding().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WidthsSection()
        OutlinedButton(onClick = onRequestPermission) { Text(stringResource(R.string.spike_request_permission)) }
        NotificationLanguageSection(languageState, onSelectLanguage, onShowNotification)
        ActiveNotificationsSection(activeState, onActiveAction)
    }
}

@Composable
private fun NotificationLanguageSection(
    state: NotificationLanguageState,
    onSelectLanguage: (String) -> Unit,
    onShowNotification: () -> Unit
) {
    Text(text = stringResource(R.string.spike_language_title), style = MaterialTheme.typography.titleLarge)
    Text(text = stringResource(R.string.spike_language_effective, state.effectiveLocales))
    Text(text = stringResource(R.string.spike_language_app_setting, appLocalesText(state.appLocales)))
    LanguageButtons(onSelectLanguage)
    Button(onClick = onShowNotification) { Text(stringResource(R.string.spike_show_notification)) }
}

@Composable
private fun LanguageButtons(onSelectLanguage: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { onSelectLanguage("pl") }) { Text(stringResource(R.string.spike_language_polish)) }
        OutlinedButton(onClick = { onSelectLanguage("en") }) { Text(stringResource(R.string.spike_language_english)) }
        OutlinedButton(onClick = { onSelectLanguage("") }) {
            Text(stringResource(R.string.spike_language_follow_device))
        }
    }
}

@Composable
private fun appLocalesText(appLocales: String): String =
    appLocales.ifEmpty { stringResource(R.string.spike_language_not_set) }

@Composable
private fun ActiveNotificationsSection(state: ActiveNotificationsState, onAction: (ActiveAction) -> Unit) {
    Text(text = stringResource(R.string.spike_active_title), style = MaterialTheme.typography.titleLarge)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { onAction(ActiveAction.Show("A")) }) { Text(stringResource(R.string.spike_active_show, "A")) }
        Button(onClick = { onAction(ActiveAction.Show("B")) }) { Text(stringResource(R.string.spike_active_show, "B")) }
    }
    OutlinedButton(onClick = { onAction(ActiveAction.List) }) { Text(stringResource(R.string.spike_active_list)) }
    OutlinedButton(onClick = { onAction(ActiveAction.ShowAndList("C")) }) {
        Text(stringResource(R.string.spike_active_show_and_list, "C"))
    }
    if (state.isPermissionMissing) Text(stringResource(R.string.spike_active_permission_missing))
    ShownNotificationsList(state)
}

@Composable
private fun ShownNotificationsList(state: ActiveNotificationsState) {
    val trigger = state.trigger ?: return
    Text(stringResource(R.string.spike_active_listed, trigger.name, state.shown.size))
    state.shown.forEach { Text(shownNotificationText(it, state.listedAtMillis)) }
}

// Technical spike output, not user-visible text of the app.
private fun shownNotificationText(shown: ShownNotification, listedAtMillis: Long): String =
    "tag=${shown.tag} id=${shown.id} ongoing=${shown.isOngoing} " +
        "posted ${listedAtMillis - shown.postTimeMillis} ms before the list"

private val aShownNotification = ShownNotification(tag = "habit:A", id = 2, isOngoing = true, postTimeMillis = 1_000L)

@Preview
@Composable
private fun SpikeContentPreview() {
    MaterialTheme {
        SpikeContent(
            languageState = NotificationLanguageState(effectiveLocales = "pl-PL", appLocales = "pl"),
            activeState =
                ActiveNotificationsState(
                    trigger = ListTrigger.Button,
                    listedAtMillis = 2_000L,
                    shown = listOf(aShownNotification)
                ),
            onSelectLanguage = {},
            onRequestPermission = {},
            onShowNotification = {},
            onActiveAction = {}
        )
    }
}
