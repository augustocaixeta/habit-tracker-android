package br.edu.iftm.habittracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.habitStore: DataStore<Preferences> by preferencesDataStore(name = "habits")

class HabitRepository(private val context: Context) {

    /**
     * Observa os hábitos de um usuário, do mais antigo para o mais novo.
     *
     * @param userId uid do usuário no Firebase.
     * @return fluxo que emite a lista a cada alteração no DataStore.
     */
    fun observeAll(userId: String): Flow<List<Habit>> =
        context.habitStore.data.map { prefs -> decode(prefs[key(userId)]).sortedBy { it.createdAt } }

    /**
     * Observa um hábito.
     *
     * @param userId uid do usuário no Firebase.
     * @param habitId identificador do hábito.
     * @return fluxo que emite null quando o hábito é excluído.
     */
    fun observe(userId: String, habitId: String): Flow<Habit?> =
        observeAll(userId).map { list -> list.find { it.id == habitId } }

    /**
     * Cadastra um hábito novo.
     *
     * @param userId uid do usuário no Firebase.
     * @param title nome do hábito.
     * @param description descrição opcional.
     * @param icon ícone escolhido.
     * @param colorIndex posição da cor na paleta de hábitos.
     */
    suspend fun add(userId: String, title: String, description: String, icon: HabitIcon, colorIndex: Int) {
        val habit = Habit(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            description = description.trim(),
            icon = icon,
            colorIndex = colorIndex
        )
        edit(userId) { list -> list + habit }
    }

    /**
     * Substitui um hábito pela versão editada.
     *
     * @param userId uid do usuário no Firebase.
     * @param habit hábito com os novos dados.
     */
    suspend fun update(userId: String, habit: Habit) {
        edit(userId) { list -> list.map { if (it.id == habit.id) habit else it } }
    }

    /**
     * Remove um hábito com todo o histórico dele.
     *
     * @param userId uid do usuário no Firebase.
     * @param habitId identificador do hábito.
     */
    suspend fun delete(userId: String, habitId: String) {
        edit(userId) { list -> list.filterNot { it.id == habitId } }
    }

    /**
     * Alterna o status de um dia entre feito e não feito.
     *
     * @param userId uid do usuário no Firebase.
     * @param habitId identificador do hábito.
     * @param date dia alterado, hoje por padrão.
     */
    suspend fun toggleDone(userId: String, habitId: String, date: LocalDate = LocalDate.now()) {
        edit(userId) { list -> list.map { if (it.id == habitId) it.toggledOn(date) else it } }
    }

    /**
     * Lê a lista gravada, aplica a alteração e grava o resultado em uma única transação.
     *
     * @param userId uid do usuário no Firebase.
     * @param transform alteração aplicada sobre a lista atual.
     */
    private suspend fun edit(userId: String, transform: (List<Habit>) -> List<Habit>) {
        context.habitStore.edit { prefs ->
            prefs[key(userId)] = encode(transform(decode(prefs[key(userId)])))
        }
    }

    /**
     * Chave do DataStore que separa os hábitos de cada conta.
     *
     * @param userId uid do usuário no Firebase.
     * @return chave no formato habits_uid.
     */
    private fun key(userId: String): Preferences.Key<String> = stringPreferencesKey("habits_$userId")

    /**
     * Converte a lista de hábitos em JSON.
     *
     * @param habits hábitos a gravar.
     * @return texto JSON com um objeto por hábito.
     */
    private fun encode(habits: List<Habit>): String {
        val array = JSONArray()
        habits.forEach { habit ->
            array.put(
                JSONObject()
                    .put("id", habit.id)
                    .put("title", habit.title)
                    .put("description", habit.description)
                    .put("icon", habit.icon.name)
                    .put("colorIndex", habit.colorIndex)
                    .put("createdAt", habit.createdAt)
                    .put("completedDates", JSONArray(habit.completedDates.map { it.toString() }))
            )
        }
        return array.toString()
    }

    /**
     * Converte o JSON gravado de volta em hábitos. Um conteúdo inválido resulta em lista vazia.
     *
     * @param json texto gravado, ou null quando o usuário ainda não tem hábitos.
     * @return hábitos lidos.
     */
    private fun decode(json: String?): List<Habit> {
        if (json.isNullOrBlank()) {
            return emptyList()
        }
        return runCatching {
            val array = JSONArray(json)
            (0 until array.length()).map { index -> parseHabit(array.getJSONObject(index)) }
        }.getOrDefault(emptyList())
    }

    /**
     * Lê um hábito do JSON. Ícones desconhecidos voltam para o ícone padrão.
     *
     * @param json objeto JSON de um hábito.
     * @return hábito lido.
     */
    private fun parseHabit(json: JSONObject): Habit {
        val dates = json.optJSONArray("completedDates") ?: JSONArray()
        val iconName = json.optString("icon")
        return Habit(
            id = json.getString("id"),
            title = json.getString("title"),
            description = json.optString("description"),
            icon = HabitIcon.entries.find { it.name.equals(iconName, ignoreCase = true) } ?: HabitIcon.CHECK,
            colorIndex = json.optInt("colorIndex", 0),
            createdAt = json.optLong("createdAt"),
            completedDates = (0 until dates.length())
                .mapNotNull { index -> runCatching { LocalDate.parse(dates.getString(index)) }.getOrNull() }
                .toSet()
        )
    }
}
