package br.edu.iftm.habittracker.ui.habits

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.edu.iftm.habittracker.R
import br.edu.iftm.habittracker.data.HabitIcon

/**
 * Botão circular do status do dia. Fica preenchido com a cor do hábito quando está feito.
 *
 * @param done true quando o hábito foi feito hoje.
 * @param color cor do hábito.
 * @param onToggle alterna entre feito e não feito.
 * @param size diâmetro do botão.
 */
@Composable
fun DoneCheckbox(done: Boolean, color: Color, onToggle: () -> Unit, size: Dp = 36.dp) {
    val background by animateColorAsState(targetValue = if (done) color else Color.Transparent, label = "check")
    val description = stringResource(if (done) R.string.cd_mark_not_done else R.string.cd_mark_done)
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
            .border(2.dp, color, CircleShape)
            .clickable(onClick = onToggle)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        if (done) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                modifier = Modifier.size(size * 0.6f),
                tint = Color.White
            )
        }
    }
}

/**
 * Ícone do hábito sobre um quadrado arredondado com a cor do hábito em transparência.
 *
 * @param icon ícone do hábito.
 * @param color cor do hábito.
 * @param size lado do quadrado.
 * @param iconSize tamanho do ícone.
 * @param cornerRadius raio dos cantos do quadrado.
 */
@Composable
fun HabitIconBadge(icon: HabitIcon, color: Color, size: Dp, iconSize: Dp, cornerRadius: Dp = 14.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon.vector(),
            contentDescription = stringResource(icon.labelRes()),
            modifier = Modifier.size(iconSize),
            tint = color
        )
    }
}
