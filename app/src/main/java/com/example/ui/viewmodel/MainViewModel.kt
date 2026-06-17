package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.AppLanguage
import com.example.data.AppTheme
import com.example.data.Habit
import com.example.data.StorageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext

    // App Preferences state
    private val _currentLanguage = MutableStateFlow(StorageManager.getLanguage(context))
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _currentTheme = MutableStateFlow(StorageManager.getTheme(context))
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    private val _loggedInUser = MutableStateFlow(StorageManager.getLoggedInUser(context))
    val loggedInUser: StateFlow<String?> = _loggedInUser.asStateFlow()

    // Habit tracking state
    private val _userHabits = MutableStateFlow<List<Habit>>(emptyList())
    val userHabits: StateFlow<List<Habit>> = _userHabits.asStateFlow()

    // Temporary toast message trigger
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        loadHabitsForCurrentUser()
    }

    fun loadHabitsForCurrentUser() {
        val user = _loggedInUser.value
        if (user != null) {
            _userHabits.value = StorageManager.getHabitsForUser(context, user)
        } else {
            _userHabits.value = emptyList()
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun setLanguage(lang: AppLanguage) {
        StorageManager.setLanguage(context, lang)
        _currentLanguage.value = lang
    }

    fun setTheme(theme: AppTheme) {
        StorageManager.setTheme(context, theme)
        _currentTheme.value = theme
    }

    fun register(username: String, pass: String): Boolean {
        if (StorageManager.registerUser(context, username, pass)) {
            showToast("Registration Success!")
            return true
        } else {
            showToast("Username already exists!")
            return false
        }
    }

    fun login(username: String, pass: String): Boolean {
        if (StorageManager.validateUser(context, username, pass)) {
            StorageManager.setLoggedInUser(context, username)
            _loggedInUser.value = username
            loadHabitsForCurrentUser()
            showToast("Logged in: $username")
            return true
        } else {
            showToast("Invalid Credentials")
            return false
        }
    }

    fun logout() {
        StorageManager.setLoggedInUser(context, null)
        _loggedInUser.value = null
        loadHabitsForCurrentUser()
        showToast("Logged out successfully.")
    }

    // --- Habit Actions ---
    fun addHabit(title: String, description: String) {
        val user = _loggedInUser.value ?: return
        val newHabit = Habit(
            id = UUID.randomUUID().toString(),
            username = user,
            title = title,
            description = description,
            daysDone = emptyList()
        )
        StorageManager.saveHabit(context, newHabit)
        loadHabitsForCurrentUser()
        showToast("Habit added successfully")
    }

    fun updateHabit(id: String, title: String, description: String) {
        val user = _loggedInUser.value ?: return
        val allHabitsForUser = StorageManager.getHabitsForUser(context, user)
        val existing = allHabitsForUser.firstOrNull { it.id == id } ?: return
        val updated = existing.copy(title = title, description = description)
        StorageManager.saveHabit(context, updated)
        loadHabitsForCurrentUser()
        showToast("Habit updated successfully")
    }

    fun deleteHabit(habitId: String) {
        StorageManager.deleteHabit(context, habitId)
        loadHabitsForCurrentUser()
        showToast("Habit deleted")
    }

    fun toggleHabitCompletion(habitId: String, dateStr: String) {
        StorageManager.toggleHabitCompletion(context, habitId, dateStr)
        loadHabitsForCurrentUser()
    }
}
