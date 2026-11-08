package br.edu.iftm.habittracker

import android.app.Application
import br.edu.iftm.habittracker.data.AuthRepository
import br.edu.iftm.habittracker.data.HabitRepository

class HabitTrackerApp : Application() {

    val auth by lazy { AuthRepository() }

    val habits by lazy { HabitRepository(this) }
}
