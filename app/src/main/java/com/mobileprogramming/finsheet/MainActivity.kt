package com.mobileprogramming.finsheet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.mobileprogramming.finsheet.core.theme.FinSheetTheme
import com.mobileprogramming.finsheet.ui.navigation.FinSheetNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinSheetTheme {
                val navController = rememberNavController()
                FinSheetNavGraph(navController = navController)
            }
        }
    }
}