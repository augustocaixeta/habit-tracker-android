package br.edu.iftm.habittracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.edu.iftm.habittracker.ui.auth.AuthViewModel
import br.edu.iftm.habittracker.ui.auth.LoginScreen
import br.edu.iftm.habittracker.ui.auth.RegisterScreen
import br.edu.iftm.habittracker.ui.habits.HabitFormScreen
import br.edu.iftm.habittracker.ui.habits.HabitListScreen

/**
 * Grafo de navegação: Login e Registro levam à listagem depois da autenticação,
 * e a listagem abre o formulário de cadastro ou de detalhes de um hábito.
 * Com uma sessão ativa no Firebase, o app já abre na listagem.
 *
 * @param navController controlador que guarda a pilha de telas.
 * @param authViewModel estado da autenticação, compartilhado entre Login e Registro.
 */
@Composable
fun HabitNavHost(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
    val authState by authViewModel.state.collectAsStateWithLifecycle()
    val start: Any = remember { if (authViewModel.state.value.isLoggedIn) Habits else Login }

    LaunchedEffect(authState.isLoggedIn) {
        val destination = navController.currentDestination
        val onAuthScreen = destination?.hasRoute<Login>() == true || destination?.hasRoute<Register>() == true
        if (authState.isLoggedIn && onAuthScreen) {
            navController.navigate(Habits) {
                popUpTo(navController.graph.id) {
                    inclusive = true
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = start) {
        composable<Login> { entry ->
            LoginScreen(
                viewModel = authViewModel,
                onGoToRegister = { navController.navigateFrom(entry, Register) }
            )
        }
        composable<Register> { entry ->
            RegisterScreen(
                viewModel = authViewModel,
                onBackToLogin = { navController.popFrom(entry) }
            )
        }
        composable<Habits> { entry ->
            HabitListScreen(
                onAddHabit = { navController.navigateFrom(entry, HabitForm()) },
                onOpenHabit = { id -> navController.navigateFrom(entry, HabitForm(id)) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Login) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable<HabitForm> { entry ->
            HabitFormScreen(onBack = { navController.popFrom(entry) })
        }
    }
}

/**
 * Abre uma rota só se a tela de origem ainda estiver ativa, evitando telas duplicadas com toques repetidos.
 *
 * @receiver controlador de navegação.
 * @param entry tela que pediu a navegação.
 * @param route destino.
 */
private fun <T : Any> NavHostController.navigateFrom(entry: NavBackStackEntry, route: T) {
    if (entry.lifecycle.currentState == Lifecycle.State.RESUMED) {
        navigate(route)
    }
}

/**
 * Volta uma tela só se a tela de origem ainda estiver ativa, para não fechar a listagem por engano.
 *
 * @receiver controlador de navegação.
 * @param entry tela que pediu para voltar.
 */
private fun NavHostController.popFrom(entry: NavBackStackEntry) {
    if (entry.lifecycle.currentState == Lifecycle.State.RESUMED) {
        popBackStack()
    }
}
