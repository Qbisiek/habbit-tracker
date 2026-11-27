package kubisp.habittracker.gui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kubisp.habittracker.gui.shell.AppShellScreen
import kubisp.habittracker.gui.shell.AppShellViewModel

class MainActivity : ComponentActivity() {
    private val shell: AppShellViewModel by viewModels {
        viewModelFactory { initializer { AppShellViewModel(application as PresenterFactories) } }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                AppShellScreen(shell)
            }
        }
    }
}
