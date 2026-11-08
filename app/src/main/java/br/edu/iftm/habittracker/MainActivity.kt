package br.edu.iftm.habittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.edu.iftm.habittracker.ui.navigation.HabitNavHost
import br.edu.iftm.habittracker.ui.theme.HabitTrackerTheme

class MainActivity : ComponentActivity() {

    /**
     * Desenha o app de ponta a ponta e abre o grafo de navegação.
     *
     * @param savedInstanceState estado salvo pelo sistema, se houver.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HabitTrackerTheme {
                HabitNavHost()
            }
        }
    }
}
