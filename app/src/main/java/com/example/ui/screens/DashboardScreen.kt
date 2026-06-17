package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.AppTheme
import com.example.data.Habit
import com.example.ui.localization.AppLocalizations
import com.example.ui.localization.TransKey
import java.text.SimpleDateFormat
import java.util.*

// Local preview palette helper
data class ThemePreviewSwatch(
    val theme: AppTheme,
    val name: String,
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    username: String,
    currentLanguage: AppLanguage,
    currentTheme: AppTheme,
    userHabits: List<Habit>,
    onAddHabit: (String, String) -> Unit,
    onUpdateHabit: (String, String, String) -> Unit,
    onDeleteHabit: (String) -> Unit,
    onToggleHabit: (String, String) -> Unit,
    onThemeSelect: (AppTheme) -> Unit,
    onLanguageSelect: (AppLanguage) -> Unit,
    onLogout: () -> Unit
) {
    val scrollState = rememberScrollState()
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    // Dialog & Form trigger states
    var showAddHabitDialog by remember { mutableStateOf(false) }
    var editingHabit by remember { mutableStateOf<Habit?>(null) }
    var deletingHabitId by remember { mutableStateOf<String?>(null) }

    // Text controllers for input handling
    var inputName by remember { mutableStateOf("") }
    var inputDesc by remember { mutableStateOf("") }

    // Expanded state tracker of each habit card to reveal stats
    val expandedHabits = remember { mutableStateMapOf<String, Boolean>() }

    // Date Utilities for calculation
    fun getTodayStr(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Calendar.getInstance().time)
    }

    fun getLastNDays(n: Int): List<String> {
        val list = mutableListOf<String>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        for (i in 0 until n) {
            list.add(sdf.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return list
    }

    fun calculateStreak(daysDone: List<String>): Int {
        if (daysDone.isEmpty()) return 0
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val doneSet = daysDone.toSet()

        val todayCal = Calendar.getInstance()
        val todayStr = sdf.format(todayCal.time)

        val yesterdayCal = Calendar.getInstance()
        yesterdayCal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(yesterdayCal.time)

        if (!doneSet.contains(todayStr) && !doneSet.contains(yesterdayStr)) {
            return 0
        }

        val cal = Calendar.getInstance()
        if (!doneSet.contains(todayStr)) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }

        var streak = 0
        while (true) {
            val checkStr = sdf.format(cal.time)
            if (doneSet.contains(checkStr)) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }

    // Progress Calculations
    val todayStr = getTodayStr()
    val totalHabitsCount = userHabits.size
    val completedTodayCount = userHabits.count { it.daysDone.contains(todayStr) }
    val dailyProgress = if (totalHabitsCount > 0) completedTodayCount.toFloat() / totalHabitsCount else 0f

    val last7Days = getLastNDays(7)
    val totalPossibleCompletionsW = totalHabitsCount * 7
    val actualCompletionsW = userHabits.sumOf { h -> h.daysDone.count { last7Days.contains(it) } }
    val weeklyProgress = if (totalPossibleCompletionsW > 0) actualCompletionsW.toFloat() / totalPossibleCompletionsW else 0f

    val last30Days = getLastNDays(30)
    val totalPossibleCompletionsM = totalHabitsCount * 30
    val actualCompletionsM = userHabits.sumOf { h -> h.daysDone.count { last30Days.contains(it) } }
    val monthlyProgress = if (totalPossibleCompletionsM > 0) actualCompletionsM.toFloat() / totalPossibleCompletionsM else 0f

    // Theme preview configurations
    val swatches = listOf(
        ThemePreviewSwatch(AppTheme.LIGHT, "Light", Color(0xFF3F51B5), Color(0xFF009688), Color(0xFFF7F8FA), Color(0xFFFFFFFF)),
        ThemePreviewSwatch(AppTheme.DARK, "Dark", Color(0xFF29B6F6), Color(0xFF26A69A), Color(0xFF121212), Color(0xFF1E1E1E)),
        ThemePreviewSwatch(AppTheme.CYBERPUNK, "Cyberpunk", Color(0xFFFDEC0A), Color(0xFFFF007F), Color(0xFF05050A), Color(0xFF151026)),
        ThemePreviewSwatch(AppTheme.OCEAN_BLUE, "Ocean Blue", Color(0xFF00F5D4), Color(0xFF00BBF9), Color(0xFF06101E), Color(0xFF0D213D)),
        ThemePreviewSwatch(AppTheme.SAKURA_PINK, "Sakura Pink", Color(0xFFFF69B4), Color(0xFFFFB7C5), Color(0xFF1C0D11), Color(0xFF2C161D))
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = AppLocalizations.getString(TransKey.APP_TITLE, currentLanguage),
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            inputName = ""
                            inputDesc = ""
                            showAddHabitDialog = true
                        },
                        modifier = Modifier.testTag("action_add_habit")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Add Habit Icon",
                            tint = primaryColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout Icon",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surfaceColor,
                    titleContentColor = onSurfaceColor
                ),
                modifier = Modifier.testTag("dashboard_topbar")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Adaptive container configuration
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Welcome Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(primaryColor, secondaryColor))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = username.take(2).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = AppLocalizations.getString(TransKey.WELCOME_BACK, currentLanguage),
                                fontSize = 13.sp,
                                color = onSurfaceColor.copy(alpha = 0.6f)
                            )
                            Text(
                                text = username,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = onSurfaceColor
                            )
                        }
                    }
                }

                // Motivation quote banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.08f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatQuote,
                            contentDescription = "Quote",
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = AppLocalizations.getString(TransKey.MOTIVATION_BANNER, currentLanguage),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = onSurfaceColor.copy(alpha = 0.85f),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // MULTI-LEVEL PROGRESS DASHBOARD (Harian, Mingguan, Bulanan)
                Text(
                    text = AppLocalizations.getString(TransKey.SIGMA_LEVEL_BAR, currentLanguage),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = onSurfaceColor,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    textAlign = TextAlign.Start
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Daily Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = surfaceColor)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = AppLocalizations.getString(TransKey.DAILY_PROGRESS, currentLanguage),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = onSurfaceColor.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { dailyProgress },
                                    modifier = Modifier.size(56.dp),
                                    color = primaryColor,
                                    trackColor = primaryColor.copy(alpha = 0.1f),
                                    strokeWidth = 6.dp
                                )
                                Text(
                                    text = "${(dailyProgress * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = onSurfaceColor
                                )
                            }
                        }
                    }

                    // Weekly Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = surfaceColor)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = AppLocalizations.getString(TransKey.WEEKLY_PROGRESS, currentLanguage),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = onSurfaceColor.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { weeklyProgress },
                                    modifier = Modifier.size(56.dp),
                                    color = secondaryColor,
                                    trackColor = secondaryColor.copy(alpha = 0.1f),
                                    strokeWidth = 6.dp
                                )
                                Text(
                                    text = "${(weeklyProgress * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = onSurfaceColor
                                )
                            }
                        }
                    }

                    // Monthly Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = surfaceColor)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = AppLocalizations.getString(TransKey.MONTHLY_PROGRESS, currentLanguage),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = onSurfaceColor.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { monthlyProgress },
                                    modifier = Modifier.size(56.dp),
                                    color = primaryColor,
                                    trackColor = primaryColor.copy(alpha = 0.1f),
                                    strokeWidth = 6.dp
                                )
                                Text(
                                    text = "${(monthlyProgress * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = onSurfaceColor
                                )
                            }
                        }
                    }
                }

                // HABIT TRACKING TASK DECK
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = AppLocalizations.getString(TransKey.HABIT_TRACKER_TITLE, currentLanguage),
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = onSurfaceColor
                    )
                    IconButton(
                        onClick = {
                            inputName = ""
                            inputDesc = ""
                            showAddHabitDialog = true
                        },
                        modifier = Modifier.testTag("btn_add_habit_shortcut")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Quick Add Icon",
                            tint = primaryColor
                        )
                    }
                }

                if (userHabits.isEmpty()) {
                    // Premium design empty-state card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudQueue,
                                contentDescription = "Empty Habits Icon",
                                tint = primaryColor.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = AppLocalizations.getString(TransKey.NO_HABITS_YET, currentLanguage),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = onSurfaceColor.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        userHabits.forEach { habit ->
                            val isDoneToday = habit.daysDone.contains(todayStr)
                            val isExpanded = expandedHabits[habit.id] ?: false
                            val streak = calculateStreak(habit.daysDone)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedHabits[habit.id] = !isExpanded },
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDoneToday) primaryColor.copy(alpha = 0.05f) else surfaceColor
                                ),
                                border = if (isDoneToday) {
                                    BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f))
                                } else {
                                    BorderStroke(1.dp, onSurfaceColor.copy(alpha = 0.06f))
                                }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Visual circular checkbox with touch size target
                                        Box(
                                            modifier = Modifier
                                                .minimumInteractiveComponentSize()
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isDoneToday) primaryColor else Color.Transparent
                                                )
                                                .border(
                                                    width = 2.dp,
                                                    color = if (isDoneToday) primaryColor else onSurfaceColor.copy(alpha = 0.4f),
                                                    shape = CircleShape
                                                )
                                                .clickable { onToggleHabit(habit.id, todayStr) }
                                                .testTag("habit_toggle_${habit.id}"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isDoneToday) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Completed Today Check",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = habit.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = onSurfaceColor,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (habit.description.isNotBlank()) {
                                                Text(
                                                    text = habit.description,
                                                    fontSize = 12.sp,
                                                    color = onSurfaceColor.copy(alpha = 0.6f),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        // Mini streak pill badge
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(secondaryColor.copy(alpha = 0.12f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocalFireDepartment,
                                                contentDescription = "Streak",
                                                tint = secondaryColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "$streak🔥",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = secondaryColor
                                            )
                                        }
                                    }

                                    // Expandable Habit Statistics panel
                                    AnimatedVisibility(
                                        visible = isExpanded,
                                        enter = expandVertically() + fadeIn(),
                                        exit = shrinkVertically() + fadeOut()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 16.dp, start = 4.dp)
                                        ) {
                                            Divider(color = onSurfaceColor.copy(alpha = 0.08f))
                                            Spacer(modifier = Modifier.height(12.dp))

                                            Text(
                                                text = AppLocalizations.getString(TransKey.HABIT_STATS, currentLanguage),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = primaryColor
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text(
                                                        text = AppLocalizations.getString(TransKey.TOTAL_COMPLETIONS, currentLanguage),
                                                        fontSize = 12.sp,
                                                        color = onSurfaceColor.copy(alpha = 0.5f)
                                                    )
                                                    Text(
                                                        text = "${habit.daysDone.size}x",
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 15.sp,
                                                        color = onSurfaceColor
                                                    )
                                                }

                                                val totalDaysElapsed = maxOf(1, ((System.currentTimeMillis() - habit.createdAt) / (1000 * 60 * 60 * 24)).toInt() + 1)
                                                val rate = (habit.daysDone.size.toFloat() / totalDaysElapsed).coerceIn(0f, 1f)
                                                Column {
                                                    Text(
                                                        text = AppLocalizations.getString(TransKey.COMPLETION_RATE, currentLanguage),
                                                        fontSize = 12.sp,
                                                        color = onSurfaceColor.copy(alpha = 0.5f)
                                                    )
                                                    Text(
                                                        text = "${(rate * 100).toInt()}%",
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 15.sp,
                                                        color = onSurfaceColor
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(16.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                TextButton(
                                                    onClick = {
                                                        editingHabit = habit
                                                        inputName = habit.title
                                                        inputDesc = habit.description
                                                    },
                                                    modifier = Modifier.testTag("action_edit_habit_${habit.id}")
                                                ) {
                                                    Icon(imageVector = Icons.Outlined.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(AppLocalizations.getString(TransKey.BTN_EDIT, currentLanguage) ?: "Edit", fontSize = 13.sp)
                                                }

                                                Spacer(modifier = Modifier.width(8.dp))

                                                IconButton(
                                                    onClick = { deletingHabitId = habit.id },
                                                    modifier = Modifier.testTag("action_delete_habit_${habit.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.Delete,
                                                        contentDescription = "Delete",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // UI SETTINGS ACCORDION
                Text(
                    text = AppLocalizations.getString(TransKey.SETTINGS, currentLanguage),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = onSurfaceColor,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, start = 4.dp),
                    textAlign = TextAlign.Start
                )

                // Theme visual selection deck
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = AppLocalizations.getString(TransKey.SELECT_THEME, currentLanguage),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = onSurfaceColor
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            swatches.forEach { swatch ->
                                val isSelected = currentTheme == swatch.theme
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) swatch.background else swatch.background.copy(alpha = 0.4f))
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) swatch.primary else swatch.primary.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { onThemeSelect(swatch.theme) }
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                        .testTag("theme_button_${swatch.theme.name.lowercase()}"),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(swatch.primary))
                                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(swatch.secondary))
                                        }

                                        Text(
                                            text = swatch.name,
                                            color = if (swatch.theme == AppTheme.LIGHT) Color.Black else Color.White,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 15.sp
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected theme Icon",
                                            tint = swatch.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Language select grid deck
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = AppLocalizations.getString(TransKey.SELECT_LANG, currentLanguage),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = onSurfaceColor
                        )

                        val languages = AppLanguage.values()
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            languages.forEach { lang ->
                                val isSelected = currentLanguage == lang
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) primaryColor.copy(alpha = 0.12f) else Color.Transparent)
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) primaryColor else onSurfaceColor.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { onLanguageSelect(lang) }
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                        .testTag("lang_button_${lang.code}"),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val flagString = when (lang) {
                                            AppLanguage.INDONESIA -> "🇮🇩"
                                            AppLanguage.ENGLISH -> "🇺🇸"
                                            AppLanguage.JAPANESE -> "🇯🇵"
                                            AppLanguage.GERMAN -> "🇩🇪"
                                        }

                                        Text(
                                            text = flagString,
                                            fontSize = 20.sp
                                        )

                                        Text(
                                            text = lang.displayName,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = onSurfaceColor,
                                            fontSize = 14.sp
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected language Icon",
                                            tint = primaryColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // --- ADD HABIT DIALOG ---
    if (showAddHabitDialog) {
        AlertDialog(
            onDismissRequest = { showAddHabitDialog = false },
            title = {
                Text(
                    text = AppLocalizations.getString(TransKey.ADD_HABIT_TITLE, currentLanguage),
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text(AppLocalizations.getString(TransKey.HABIT_NAME_LABEL, currentLanguage)) },
                        modifier = Modifier.fillMaxWidth().testTag("add_habit_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = inputDesc,
                        onValueChange = { inputDesc = it },
                        label = { Text(AppLocalizations.getString(TransKey.HABIT_DESC_LABEL, currentLanguage)) },
                        modifier = Modifier.fillMaxWidth().testTag("add_habit_desc_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputName.isNotBlank()) {
                            onAddHabit(inputName, inputDesc)
                            showAddHabitDialog = false
                        }
                    },
                    modifier = Modifier.testTag("add_habit_confirm_btn")
                ) {
                    Text(AppLocalizations.getString(TransKey.BTN_SAVE, currentLanguage))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddHabitDialog = false },
                    modifier = Modifier.testTag("add_habit_cancel_btn")
                ) {
                    Text(AppLocalizations.getString(TransKey.BTN_CANCEL, currentLanguage))
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    // --- EDIT HABIT DIALOG ---
    editingHabit?.let { habit ->
        AlertDialog(
            onDismissRequest = { editingHabit = null },
            title = {
                Text(
                    text = AppLocalizations.getString(TransKey.EDIT_HABIT_TITLE, currentLanguage),
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text(AppLocalizations.getString(TransKey.HABIT_NAME_LABEL, currentLanguage)) },
                        modifier = Modifier.fillMaxWidth().testTag("edit_habit_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = inputDesc,
                        onValueChange = { inputDesc = it },
                        label = { Text(AppLocalizations.getString(TransKey.HABIT_DESC_LABEL, currentLanguage)) },
                        modifier = Modifier.fillMaxWidth().testTag("edit_habit_desc_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputName.isNotBlank()) {
                            onUpdateHabit(habit.id, inputName, inputDesc)
                            editingHabit = null
                        }
                    },
                    modifier = Modifier.testTag("edit_habit_confirm_btn")
                ) {
                    Text(AppLocalizations.getString(TransKey.BTN_SAVE, currentLanguage))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { editingHabit = null },
                    modifier = Modifier.testTag("edit_habit_cancel_btn")
                ) {
                    Text(AppLocalizations.getString(TransKey.BTN_CANCEL, currentLanguage))
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    // --- CONFIRM DELETE DIALOG ---
    deletingHabitId?.let { id ->
        AlertDialog(
            onDismissRequest = { deletingHabitId = null },
            title = {
                Text(
                    text = AppLocalizations.getString(TransKey.CONFIRM_DELETE_TITLE, currentLanguage),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = AppLocalizations.getString(TransKey.CONFIRM_DELETE_MSG, currentLanguage)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteHabit(id)
                        deletingHabitId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("delete_habit_confirm_btn")
                ) {
                    Text(AppLocalizations.getString(TransKey.BTN_DELETE, currentLanguage))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { deletingHabitId = null },
                    modifier = Modifier.testTag("delete_habit_cancel_btn")
                ) {
                    Text(AppLocalizations.getString(TransKey.BTN_CANCEL, currentLanguage))
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}
