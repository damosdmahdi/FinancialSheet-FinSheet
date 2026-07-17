package com.mobileprogramming.finsheet.ui.features.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.mobileprogramming.finsheet.R
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onNavigateToDashboard: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    val authClient = remember {
        GoogleAuthClient(
            context = context,
            auth = FirebaseAuth.getInstance()
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_finsheet),
            contentDescription = "App Logo",
            modifier = Modifier.size(120.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Welcome to FinSheet",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Sign in to continue managing your finances",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        val result = authClient.signIn()
                        isLoading = false
                        if (result != null) {
                            Toast.makeText(context, "Sign In Successful", Toast.LENGTH_SHORT).show()
                            onNavigateToDashboard()
                        } else {
                            Toast.makeText(context, "Sign In Failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFE0E0E0)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = androidx.compose.ui.graphics.Color.White,
                    contentColor = androidx.compose.ui.graphics.Color(0xFF1F1F1F)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = com.mobileprogramming.finsheet.R.drawable.ic_google_logo),
                        contentDescription = "Google Logo",
                        tint = androidx.compose.ui.graphics.Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Masuk dengan Google",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = androidx.compose.ui.graphics.Color(0xFF1F1F1F)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        val result = authClient.signInAsGuest()
                        isLoading = false
                        if (result != null) {
                            Toast.makeText(context, "Masuk sebagai Guest Berhasil", Toast.LENGTH_SHORT).show()
                            onNavigateToDashboard()
                        } else {
                            Toast.makeText(context, "Gagal masuk sebagai Guest", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(text = "Masuk sebagai Guest", fontSize = 16.sp)
            }

        }
    }
}
