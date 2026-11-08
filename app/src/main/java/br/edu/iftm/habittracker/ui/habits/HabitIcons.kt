package br.edu.iftm.habittracker.ui.habits

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector
import br.edu.iftm.habittracker.R
import br.edu.iftm.habittracker.data.HabitIcon

/**
 * Ícone do Material Icons que representa cada opção de hábito.
 *
 * @receiver opção gravada no hábito.
 * @return ícone vetorial.
 */
fun HabitIcon.vector(): ImageVector {
    return when (this) {
        HabitIcon.CHECK -> Icons.Rounded.CheckCircle
        HabitIcon.WATER -> Icons.Rounded.WaterDrop
        HabitIcon.BOOK -> Icons.AutoMirrored.Rounded.MenuBook
        HabitIcon.RUN -> Icons.AutoMirrored.Rounded.DirectionsRun
        HabitIcon.MEDITATE -> Icons.Rounded.SelfImprovement
        HabitIcon.FOOD -> Icons.Rounded.Restaurant
        HabitIcon.SLEEP -> Icons.Rounded.Bedtime
        HabitIcon.CODE -> Icons.Rounded.Computer
        HabitIcon.MUSIC -> Icons.Rounded.MusicNote
        HabitIcon.PLANT -> Icons.Rounded.LocalFlorist
    }
}

/**
 * Nome de cada opção de ícone, lido pelo leitor de tela.
 *
 * @receiver opção gravada no hábito.
 * @return id do texto no strings.xml.
 */
@StringRes
fun HabitIcon.labelRes(): Int {
    return when (this) {
        HabitIcon.CHECK -> R.string.icon_check
        HabitIcon.WATER -> R.string.icon_water
        HabitIcon.BOOK -> R.string.icon_book
        HabitIcon.RUN -> R.string.icon_run
        HabitIcon.MEDITATE -> R.string.icon_meditate
        HabitIcon.FOOD -> R.string.icon_food
        HabitIcon.SLEEP -> R.string.icon_sleep
        HabitIcon.CODE -> R.string.icon_code
        HabitIcon.MUSIC -> R.string.icon_music
        HabitIcon.PLANT -> R.string.icon_plant
    }
}
