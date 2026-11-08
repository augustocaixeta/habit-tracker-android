package br.edu.iftm.habittracker.ui.habits

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.edu.iftm.habittracker.R
import br.edu.iftm.habittracker.data.Habit
import br.edu.iftm.habittracker.data.currentStreak
import br.edu.iftm.habittracker.data.isDoneToday
import br.edu.iftm.habittracker.ui.theme.PtBr
import br.edu.iftm.habittracker.ui.theme.StreakColor
import br.edu.iftm.habittracker.ui.theme.habitColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Listagem dos hábitos do dia, com o progresso, a marcação de feito e o acesso ao cadastro.
 *
 * @param onAddHabit abre o cadastro de um hábito novo.
 * @param onOpenHabit abre os detalhes do hábito tocado.
 * @param onLogout encerra a sessão e volta ao login.
 * @param viewModel estado da listagem.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    onAddHabit: () -> Unit,
    onOpenHabit: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: HabitListViewModel = viewModel(factory = HabitListViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmLogout by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.list_greeting, state.userName),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = todayLabel(stringResource(R.string.list_date_pattern)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { confirmLogout = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = stringResource(R.string.cd_logout)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(text = stringResource(R.string.list_add_habit)) },
                icon = { Icon(imageVector = Icons.Rounded.Add, contentDescription = null) },
                onClick = onAddHabit,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        when {
            !state.loaded -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            state.habits.isEmpty() -> EmptyState(modifier = Modifier.padding(padding))

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    ProgressCard(done = state.doneToday, total = state.habits.size, progress = state.progress)
                }
                item {
                    Text(
                        text = stringResource(R.string.list_section_today),
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(state.habits, key = { it.id }) { habit ->
                    HabitItem(
                        habit = habit,
                        onToggle = { viewModel.toggleDone(habit.id) },
                        onClick = { onOpenHabit(habit.id) }
                    )
                }
            }
        }
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmLogout = false
                        onLogout()
                    }
                ) {
                    Text(text = stringResource(R.string.logout_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmLogout = false }) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            },
            title = { Text(text = stringResource(R.string.logout_title)) },
            text = { Text(text = stringResource(R.string.logout_message)) }
        )
    }
}

/**
 * Cartão verde com a quantidade de hábitos feitos hoje e a barra de progresso.
 *
 * @param done hábitos feitos hoje.
 * @param total total de hábitos.
 * @param progress fração concluída, de 0 a 1.
 */
@Composable
private fun ProgressCard(done: Int, total: Int, progress: Float) {
    val animated by animateFloatAsState(targetValue = progress, label = "progress")
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(R.string.list_progress_title),
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.list_progress_count, done, total),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
            Spacer(Modifier.height(14.dp))
            LinearProgressIndicator(
                progress = { animated },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.25f),
                drawStopIndicator = {}
            )
        }
    }
}

/**
 * Cartão de um hábito com ícone, nome, sequência atual e o botão de feito.
 *
 * @param habit hábito exibido.
 * @param onToggle alterna o status de hoje.
 * @param onClick abre os detalhes do hábito.
 */
@Composable
private fun HabitItem(habit: Habit, onToggle: () -> Unit, onClick: () -> Unit) {
    val color = habitColor(habit.colorIndex)
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HabitIconBadge(icon = habit.icon, color = color, size = 48.dp, iconSize = 26.dp)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                StreakLine(streak = habit.currentStreak)
            }
            DoneCheckbox(done = habit.isDoneToday, color = color, onToggle = onToggle)
        }
    }
}

/**
 * Linha com a sequência de dias seguidos, ou um convite para começar quando ela é zero.
 *
 * @param streak dias seguidos concluídos.
 */
@Composable
private fun StreakLine(streak: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (streak > 0) {
            Icon(
                imageVector = Icons.Rounded.LocalFireDepartment,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = StreakColor
            )
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = if (streak > 0) {
                pluralStringResource(R.plurals.list_streak, streak, streak)
            } else {
                stringResource(R.string.list_streak_empty)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Mensagem exibida quando o usuário ainda não cadastrou hábitos.
 *
 * @param modifier modificador aplicado ao conteúdo.
 */
@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.Spa,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.list_empty_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.list_empty_message),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Data de hoje por extenso para o cabeçalho, com a primeira letra maiúscula.
 *
 * @param pattern padrão de data lido do strings.xml.
 * @return data formatada, como "Domingo, 8 de novembro".
 */
private fun todayLabel(pattern: String): String {
    return LocalDate.now()
        .format(DateTimeFormatter.ofPattern(pattern, PtBr))
        .replaceFirstChar { it.uppercase() }
}
