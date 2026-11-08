package br.edu.iftm.habittracker.ui.habits

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import br.edu.iftm.habittracker.HabitTrackerApp
import br.edu.iftm.habittracker.data.AuthRepository
import br.edu.iftm.habittracker.data.Habit
import br.edu.iftm.habittracker.data.HabitIcon
import br.edu.iftm.habittracker.data.HabitRepository
import br.edu.iftm.habittracker.ui.navigation.HabitForm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HabitFormUiState(
    val isNew: Boolean = true,
    val title: String = "",
    val description: String = "",
    val icon: HabitIcon = HabitIcon.CHECK,
    val colorIndex: Int = 0,
    val habit: Habit? = null,
    val titleMissing: Boolean = false,
    val finished: Boolean = false
)

class HabitFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val habits: HabitRepository,
    auth: AuthRepository
) : ViewModel() {

    private val habitId = savedStateHandle.toRoute<HabitForm>().habitId
    private val userId = auth.currentUser?.uid.orEmpty()

    private val _state = MutableStateFlow(HabitFormUiState(isNew = habitId == null))
    val state: StateFlow<HabitFormUiState> = _state.asStateFlow()

    init {
        if (habitId != null) {
            observeHabit(habitId)
        }
    }

    /**
     * Atualiza o nome digitado.
     *
     * @param value texto do campo.
     */
    fun onTitleChange(value: String) {
        _state.update { it.copy(title = value, titleMissing = false) }
    }

    /**
     * Atualiza a descrição digitada.
     *
     * @param value texto do campo.
     */
    fun onDescriptionChange(value: String) {
        _state.update { it.copy(description = value) }
    }

    /**
     * Troca o ícone escolhido.
     *
     * @param value ícone tocado no seletor.
     */
    fun onIconChange(value: HabitIcon) {
        _state.update { it.copy(icon = value) }
    }

    /**
     * Troca a cor escolhida.
     *
     * @param value posição da cor na paleta.
     */
    fun onColorChange(value: Int) {
        _state.update { it.copy(colorIndex = value) }
    }

    /**
     * Cadastra o hábito novo ou grava a edição e avisa a tela para voltar à listagem.
     */
    fun save() {
        val current = _state.value
        if (current.title.isBlank()) {
            _state.update { it.copy(titleMissing = true) }
            return
        }
        viewModelScope.launch {
            val existing = current.habit
            if (existing == null) {
                habits.add(userId, current.title, current.description, current.icon, current.colorIndex)
            } else {
                habits.update(
                    userId,
                    existing.copy(
                        title = current.title.trim(),
                        description = current.description.trim(),
                        icon = current.icon,
                        colorIndex = current.colorIndex
                    )
                )
            }
            _state.update { it.copy(finished = true) }
        }
    }

    /**
     * Marca ou desmarca o hábito como feito hoje, pelo cartão de detalhes.
     */
    fun toggleToday() {
        val id = habitId ?: return
        viewModelScope.launch {
            habits.toggleDone(userId, id)
        }
    }

    /**
     * Exclui o hábito e avisa a tela para voltar à listagem.
     */
    fun delete() {
        val id = habitId ?: return
        viewModelScope.launch {
            habits.delete(userId, id)
            _state.update { it.copy(finished = true) }
        }
    }

    /**
     * Acompanha o hábito em edição. O formulário é preenchido só na primeira leitura,
     * para não apagar o que o usuário está digitando quando o status do dia muda.
     *
     * @param id identificador do hábito.
     */
    private fun observeHabit(id: String) {
        viewModelScope.launch {
            habits.observe(userId, id).filterNotNull().collect { habit ->
                _state.update { current ->
                    if (current.habit != null) {
                        current.copy(habit = habit)
                    } else {
                        current.copy(
                            title = habit.title,
                            description = habit.description,
                            icon = habit.icon,
                            colorIndex = habit.colorIndex,
                            habit = habit
                        )
                    }
                }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as HabitTrackerApp
                HabitFormViewModel(createSavedStateHandle(), app.habits, app.auth)
            }
        }
    }
}
