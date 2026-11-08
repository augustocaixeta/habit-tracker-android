package br.edu.iftm.habittracker.data

import java.time.LocalDate

enum class HabitIcon {
    CHECK,
    WATER,
    BOOK,
    RUN,
    MEDITATE,
    FOOD,
    SLEEP,
    CODE,
    MUSIC,
    PLANT
}

data class Habit(
    val id: String,
    val title: String,
    val description: String = "",
    val icon: HabitIcon = HabitIcon.CHECK,
    val colorIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val completedDates: Set<LocalDate> = emptySet()
)

val Habit.isDoneToday: Boolean
    get() = isDoneOn(LocalDate.now())

val Habit.totalCompletions: Int
    get() = completedDates.size

/**
 * Dias seguidos em que o hábito foi concluído. Se ainda não foi feito hoje,
 * a contagem parte de ontem, para a sequência não zerar antes do fim do dia.
 */
val Habit.currentStreak: Int
    get() {
        var day = LocalDate.now()
        if (!isDoneOn(day)) {
            day = day.minusDays(1)
        }
        var count = 0
        while (isDoneOn(day)) {
            count++
            day = day.minusDays(1)
        }
        return count
    }

/**
 * Verifica se o hábito foi concluído em um dia.
 *
 * @receiver hábito consultado.
 * @param date dia verificado.
 * @return true quando o dia está entre as conclusões.
 */
fun Habit.isDoneOn(date: LocalDate): Boolean = date in completedDates

/**
 * Alterna o status de um dia entre feito e não feito.
 *
 * @receiver hábito alterado.
 * @param date dia marcado ou desmarcado.
 * @return cópia do hábito com o novo status.
 */
fun Habit.toggledOn(date: LocalDate): Habit {
    if (isDoneOn(date)) {
        return copy(completedDates = completedDates - date)
    }
    return copy(completedDates = completedDates + date)
}
