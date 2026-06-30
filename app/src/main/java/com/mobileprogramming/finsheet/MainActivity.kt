package com.mobileprogramming.finsheet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.mobileprogramming.finsheet.core.theme.FinSheetTheme
import com.mobileprogramming.finsheet.ui.navigation.FinSheetNavGraph
import com.mobileprogramming.finsheet.ui.navigation.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinSheetTheme {
                val navController = rememberNavController()
                val auth = FirebaseAuth.getInstance()
                val startDestination: Any = if (auth.currentUser != null) {
                    Screen.Dashboard
                } else {
                    Screen.Login
                }
                FinSheetNavGraph(
                    navController = navController,
                    startDestination = startDestination
                )
            }
        }
    }
}