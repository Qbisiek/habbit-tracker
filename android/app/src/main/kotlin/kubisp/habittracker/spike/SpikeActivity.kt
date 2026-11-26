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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kubisp.habittracker.R

/** Entry point of the spike prototypes (T1.4-T1.7), removed in T1.8. */
class SpikeActivity : ComponentActivity() {
    private val localeManager by lazy { getSystemService(LocaleManager::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val state =
            NotificationLanguageState(
                effectiveLocales = resources.configuration.locales.toLanguageTags(),
                appLocales = localeManager.applicationLocales.toLanguageTags()
            )
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SpikeScreen(
                        state = state,
                        onSelectLanguage = ::selectLanguage,
                        onShowNotification = ::showNotification
                    )
                }
            }
        }
    }

    // An empty tag list means "follow the device language".
    private fun selectLanguage(languageTags: String) {
        localeManager.applicationLocales = LocaleList.forLanguageTags(languageTags)
    }

    // Spike only: there is no Clock adapter yet (docs/DESIGN.md section 5.4); the code is removed in T1.8.
    private fun showNotification() {
        SpikeTimerNotification(this).showRunning(System.currentTimeMillis())
    }
}

private data class NotificationLanguageState(val effectiveLocales: String, val appLocales: String)

@Composable
private fun SpikeScreen(
    state: NotificationLanguageState,
    onSelectLanguage: (String) -> Unit,
    onShowNotification: () -> Unit
) {
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    NotificationLanguageContent(
        state = state,
        onSelectLanguage = onSelectLanguage,
        onRequestPermission = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
        onShowNotification = onShowNotification
    )
}

@Composable
private fun NotificationLanguageContent(
    state: NotificationLanguageState,
    onSelectLanguage: (String) -> Unit,
    onRequestPermission: () -> Unit,
    onShowNotification: () -> Unit
) {
    Column(
        modifier = Modifier.safeDrawingPadding().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = stringResource(R.string.spike_language_title), style = MaterialTheme.typography.titleLarge)
        Text(text = stringResource(R.string.spike_language_effective, state.effectiveLocales))
        Text(text = stringResource(R.string.spike_language_app_setting, appLocalesText(state.appLocales)))
        LanguageButtons(onSelectLanguage)
        OutlinedButton(onClick = onRequestPermission) { Text(stringResource(R.string.spike_request_permission)) }
        Button(onClick = onShowNotification) { Text(stringResource(R.string.spike_show_notification)) }
    }
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

@Preview
@Composable
private fun NotificationLanguageContentPreview() {
    MaterialTheme {
        NotificationLanguageContent(
            state = NotificationLanguageState(effectiveLocales = "pl-PL", appLocales = "pl"),
            onSelectLanguage = {},
            onRequestPermission = {},
            onShowNotification = {}
        )
    }
}
