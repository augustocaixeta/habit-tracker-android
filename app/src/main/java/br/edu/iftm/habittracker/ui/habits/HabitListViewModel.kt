package br.edu.iftm.habittracker.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.edu.iftm.habittracker.HabitTrackerApp
import br.edu.iftm.habittracker.data.AuthRepository
import br.edu.iftm.habittracker.data.Habit
import br.edu.iftm.habittracker.data.HabitRepository
import br.edu.iftm.habittracker.data.isDoneToday
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HabitListUiState(
    val userName: String = "",
    val habits: List<Habit> = emptyList(),
    val loaded: Boolean = false
) {
    val doneToday: Int
        get() = habits.count { it.isDoneToday }

    val progress: Float
        get() = if (habits.isEmpty()) 0f else doneToday.toFloat() / habits.size
}

class HabitListViewModel(private val habits: HabitRepository, auth: AuthRepository) : ViewModel() {

    private val userId = auth.currentUser?.uid.orEmpty()
    private val userName = auth.currentUser?.let { user ->
        user.displayName?.takeIf { it.isNotBlank() } ?: user.email?.substringBefore("@")
    }.orEmpty()

    val state: StateFlow<HabitListUiState> = habits.observeAll(userId)
        .map { list -> HabitListUiState(userName = userName, habits = list, loaded = true) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HabitListUiState(userName = userName))

    /**
     * Marca ou desmarca o hábito como feito hoje. A lista se atualiza pelo fluxo do DataStore.
     *
     * @param habitId identificador do hábito tocado.
     */
    fun toggleDone(habitId: String) {
        viewModelScope.launch {
            habits.toggleDone(userId, habitId)
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as HabitTrackerApp
                HabitListViewModel(app.habits, app.auth)
            }
        }
    }
}
