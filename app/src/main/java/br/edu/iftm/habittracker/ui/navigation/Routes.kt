package br.edu.iftm.habittracker.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object Login

@Serializable
data object Register

@Serializable
data object Habits

@Serializable
data class HabitForm(val habitId: String? = null)
