package br.edu.iftm.habittracker.ui.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.edu.iftm.habittracker.R
import br.edu.iftm.habittracker.data.Habit
import br.edu.iftm.habittracker.data.HabitIcon
import br.edu.iftm.habittracker.data.currentStreak
import br.edu.iftm.habittracker.data.isDoneOn
import br.edu.iftm.habittracker.data.isDoneToday
import br.edu.iftm.habittracker.data.totalCompletions
import br.edu.iftm.habittracker.ui.theme.HabitColors
import br.edu.iftm.habittracker.ui.theme.PtBr
import br.edu.iftm.habittracker.ui.theme.StreakColor
import br.edu.iftm.habittracker.ui.theme.habitColor
import java.time.LocalDate
import java.time.format.TextStyle

/**
 * Tela única de cadastro e de detalhes. Sem hábito na rota, cadastra um novo.
 * Com hábito, mostra o status de hoje, as estatísticas e o formulário de edição.
 *
 * @param onBack volta para a listagem.
 * @param viewModel estado do formulário.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitFormScreen(
    onBack: () -> Unit,
    viewModel: HabitFormViewModel = viewModel(factory = HabitFormViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(state.finished) {
        if (state.finished) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(if (state.isNew) R.string.form_title_new else R.string.form_title_details))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    if (!state.isNew) {
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = stringResource(R.string.cd_delete_habit)
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            state.habit?.let { habit ->
                DetailsCard(habit = habit, onToggleToday = viewModel::toggleToday)
                Text(text = stringResource(R.string.form_section_edit), style = MaterialTheme.typography.titleMedium)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                HabitIconBadge(
                    icon = state.icon,
                    color = habitColor(state.colorIndex),
                    size = 56.dp,
                    iconSize = 30.dp,
                    cornerRadius = 16.dp
                )
                Spacer(Modifier.size(12.dp))
                Text(
                    text = state.title.ifBlank { stringResource(R.string.form_preview_placeholder) },
                    style = MaterialTheme.typography.titleMedium,
                    color = if (state.title.isBlank()) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onBackground
                    }
                )
            }
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(R.string.form_field_title)) },
                placeholder = { Text(text = stringResource(R.string.form_field_title_hint)) },
                supportingText = if (state.titleMissing) {
                    { Text(text = stringResource(R.string.form_error_title_required)) }
                } else {
                    null
                },
                isError = state.titleMissing,
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(R.string.form_field_description)) },
                placeholder = { Text(text = stringResource(R.string.form_field_description_hint)) },
                minLines = 3,
                shape = RoundedCornerShape(14.dp)
            )
            Text(text = stringResource(R.string.form_section_icon), style = MaterialTheme.typography.titleMedium)
            IconPicker(selected = state.icon, onSelect = viewModel::onIconChange)
            Text(text = stringResource(R.string.form_section_color), style = MaterialTheme.typography.titleMedium)
            ColorPicker(selected = state.colorIndex, onSelect = viewModel::onColorChange)
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = viewModel::save,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = stringResource(if (state.isNew) R.string.form_action_create else R.string.form_action_save),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        viewModel.delete()
                    }
                ) {
                    Text(text = stringResource(R.string.delete_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            },
            title = { Text(text = stringResource(R.string.delete_title)) },
            text = { Text(text = stringResource(R.string.delete_message)) }
        )
    }
}

/**
 * Linha rolável com as opções de ícone. A opção escolhida ganha borda e fundo na cor primária.
 *
 * @param selected ícone escolhido.
 * @param onSelect recebe o ícone tocado.
 */
@Composable
private fun IconPicker(selected: HabitIcon, onSelect: (HabitIcon) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(HabitIcon.entries) { icon ->
            val isSelected = icon == selected
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onSelect(icon) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon.vector(),
                    contentDescription = stringResource(icon.labelRes()),
                    modifier = Modifier.size(24.dp),
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

/**
 * Círculos com as cores da paleta de hábitos. A cor escolhida ganha uma borda.
 *
 * @param selected posição da cor escolhida.
 * @param onSelect recebe a posição tocada.
 */
@Composable
private fun ColorPicker(selected: Int, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        HabitColors.forEachIndexed { index, color ->
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (index == selected) 3.dp else 0.dp,
                        color = MaterialTheme.colorScheme.onBackground,
                        shape = CircleShape
                    )
                    .clickable { onSelect(index) }
            )
        }
    }
}

/**
 * Cartão de detalhes com o status de hoje, a sequência, o total e o histórico da semana.
 *
 * @param habit hábito exibido.
 * @param onToggleToday alterna o status de hoje.
 */
@Composable
private fun DetailsCard(habit: Habit, onToggleToday: () -> Unit) {
    val color = habitColor(habit.colorIndex)
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.10f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.details_today),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(if (habit.isDoneToday) R.string.details_done else R.string.details_not_done),
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (habit.isDoneToday) color else MaterialTheme.colorScheme.onBackground
                    )
                }
                DoneCheckbox(done = habit.isDoneToday, color = color, onToggle = onToggleToday, size = 44.dp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Stat(
                    label = stringResource(R.string.details_streak),
                    value = pluralStringResource(R.plurals.details_streak_value, habit.currentStreak, habit.currentStreak),
                    icon = Icons.Rounded.LocalFireDepartment,
                    iconTint = StreakColor,
                    modifier = Modifier.weight(1f)
                )
                Stat(
                    label = stringResource(R.string.details_total),
                    value = habit.totalCompletions.toString(),
                    icon = Icons.Rounded.CheckCircle,
                    iconTint = color,
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = stringResource(R.string.details_last_days),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            WeekHistory(habit = habit, color = color)
        }
    }
}

/**
 * Os últimos 7 dias, com a inicial do dia da semana e um círculo preenchido nos dias feitos.
 *
 * @param habit hábito exibido.
 * @param color cor do hábito.
 */
@Composable
private fun WeekHistory(habit: Habit, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        (6 downTo 0).forEach { daysAgo ->
            val day = LocalDate.now().minusDays(daysAgo.toLong())
            val done = habit.isDoneOn(day)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = day.dayOfWeek.getDisplayName(TextStyle.NARROW, PtBr),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (done) color else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.dayOfMonth.toString(),
                        color = if (done) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

/**
 * Caixa de estatística com rótulo pequeno, ícone e valor.
 *
 * @param label rótulo acima do valor.
 * @param value valor formatado.
 * @param icon ícone exibido antes do valor.
 * @param iconTint cor do ícone.
 * @param modifier modificador aplicado à caixa.
 */
@Composable
private fun Stat(label: String, value: String, icon: ImageVector, iconTint: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = iconTint)
            Spacer(Modifier.size(6.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium)
        }
    }
}
