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
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

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