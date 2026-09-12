package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.readingtutor.audio.AudioService
import com.example.readingtutor.data.ReadingTutorDatabase
import com.example.readingtutor.data.ReadingTutorRepository
import com.example.readingtutor.ui.home.HomeScreen
import com.example.readingtutor.ui.lesson.LessonScreen
import com.example.readingtutor.ui.parent.ParentDashboardScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var audioService: AudioService
    private lateinit var repository: ReadingTutorRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        audioService = AudioService(applicationContext)
        val database = ReadingTutorDatabase.getDatabase(applicationContext)
        repository = ReadingTutorRepository(database.dao(), applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            repository.initializeIfEmpty()
        }

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(
                        repository = repository,
                        audioService = audioService,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioService.release()
    }
}

@Composable
fun AppNavigation(
    repository: ReadingTutorRepository,
    audioService: AudioService,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") {
            HomeScreen(
                repository = repository,
                audioService = audioService,
                onStartDay = { dayNumber ->
                    navController.navigate("lesson/$dayNumber")
                },
                onOpenParentZone = {
                    navController.navigate("parent")
                }
            )
        }

        composable(
            route = "lesson/{dayNumber}",
            arguments = listOf(navArgument("dayNumber") { type = NavType.IntType })
        ) { backStackEntry ->
            val dayNumber = backStackEntry.arguments?.getInt("dayNumber") ?: 1
            LessonScreen(
                dayNumber = dayNumber,
                repository = repository,
                audioService = audioService,
                onBackToHome = {
                    navController.popBackStack()
                }
            )
        }

        composable("parent") {
            ParentDashboardScreen(
                repository = repository,
                onBack = {
                    navController.popBackStack()
                },
                onSelectDay = { dayNumber ->
                    navController.navigate("lesson/$dayNumber")
                }
            )
        }
    }
}
