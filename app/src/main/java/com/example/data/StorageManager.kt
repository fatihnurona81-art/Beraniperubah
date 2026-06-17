package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object StorageManager {
    private const val PREFS_NAME = "sigma_habit_prefs"
    private const val KEY_THEME = "ui_theme"
    private const val KEY_LANG = "ui_lang"
    private const val KEY_CURRENT_USER = "current_user"
    private const val KEY_USERS = "registered_users"
    private const val KEY_HABITS = "user_habits"

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val usersAdapter = moshi.adapter<Map<String, String>>(
        Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
    )
    private val habitsAdapter = moshi.adapter<List<Habit>>(
        Types.newParameterizedType(List::class.java, Habit::class.java)
    )

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // --- Localization Language Setting ---
    fun getLanguage(context: Context): AppLanguage {
        val name = getPrefs(context).getString(KEY_LANG, AppLanguage.INDONESIA.name)
        return try {
            AppLanguage.valueOf(name ?: AppLanguage.INDONESIA.name)
        } catch (e: Exception) {
            AppLanguage.INDONESIA
        }
    }

    fun setLanguage(context: Context, lang: AppLanguage) {
        getPrefs(context).edit().putString(KEY_LANG, lang.name).apply()
    }

    // --- Dynamic Theme Setting ---
    fun getTheme(context: Context): AppTheme {
        val name = getPrefs(context).getString(KEY_THEME, AppTheme.DARK.name)
        return try {
            AppTheme.valueOf(name ?: AppTheme.DARK.name)
        } catch (e: Exception) {
            AppTheme.DARK
        }
    }

    fun setTheme(context: Context, theme: AppTheme) {
        getPrefs(context).edit().putString(KEY_THEME, theme.name).apply()
    }

    // --- Logged-In Account Session ---
    fun getLoggedInUser(context: Context): String? {
        return getPrefs(context).getString(KEY_CURRENT_USER, null)
    }

    fun setLoggedInUser(context: Context, username: String?) {
        getPrefs(context).edit().putString(KEY_CURRENT_USER, username).apply()
    }

    // --- Local User Registration ---
    fun getRegisteredUsers(context: Context): Map<String, String> {
        val json = getPrefs(context).getString(KEY_USERS, null) ?: return emptyMap()
        return try {
            usersAdapter.fromJson(json) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun registerUser(context: Context, username: String, pass: String): Boolean {
        val curUsers = getRegisteredUsers(context).toMutableMap()
        if (curUsers.containsKey(username)) {
            return false // User already exists!
        }
        curUsers[username] = pass
        val json = usersAdapter.toJson(curUsers)
        getPrefs(context).edit().putString(KEY_USERS, json).apply()
        return true
    }

    fun validateUser(context: Context, username: String, pass: String): Boolean {
        val users = getRegisteredUsers(context)
        return users[username] == pass
    }

    // --- Habits LocalStorage Management ---
    fun getAllHabits(context: Context): List<Habit> {
        val json = getPrefs(context).getString(KEY_HABITS, null) ?: return emptyList()
        return try {
            habitsAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getHabitsForUser(context: Context, username: String): List<Habit> {
        return getAllHabits(context).filter { it.username == username }
    }

    fun saveHabit(context: Context, habit: Habit) {
        val allHabits = getAllHabits(context).toMutableList()
        val index = allHabits.indexOfFirst { it.id == habit.id }
        if (index != -1) {
            allHabits[index] = habit
        } else {
            allHabits.add(habit)
        }
        val json = habitsAdapter.toJson(allHabits)
        getPrefs(context).edit().putString(KEY_HABITS, json).apply()
    }

    fun deleteHabit(context: Context, habitId: String) {
        val allHabits = getAllHabits(context).toMutableList()
        allHabits.removeAll { it.id == habitId }
        val json = habitsAdapter.toJson(allHabits)
        getPrefs(context).edit().putString(KEY_HABITS, json).apply()
    }

    fun toggleHabitCompletion(context: Context, habitId: String, dateStr: String): Boolean {
        val allHabits = getAllHabits(context).toMutableList()
        val index = allHabits.indexOfFirst { it.id == habitId }
        if (index == -1) return false

        val habit = allHabits[index]
        val curDaysDone = habit.daysDone.toMutableList()
        val completed = if (curDaysDone.contains(dateStr)) {
            curDaysDone.remove(dateStr)
            false
        } else {
            curDaysDone.add(dateStr)
            true
        }

        allHabits[index] = habit.copy(daysDone = curDaysDone)
        val json = habitsAdapter.toJson(allHabits)
        getPrefs(context).edit().putString(KEY_HABITS, json).apply()
        return completed
    }
}
