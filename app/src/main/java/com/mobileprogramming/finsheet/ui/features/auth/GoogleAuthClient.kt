package com.mobileprogramming.finsheet.ui.features.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.mobileprogramming.finsheet.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID

class GoogleAuthClient(
    private val context: Context,
    private val auth: FirebaseAuth
) {
    private val credentialManager = CredentialManager.create(context.applicationContext)
    private val webClientId = BuildConfig.WEB_CLIENT_ID

    suspend fun signIn(): AuthResult? {
        if (webClientId.isBlank()) {
            Log.e("GoogleAuthClient", "Web Client ID is empty. Please set WEB_CLIENT_ID in local.properties")
            return null
        }

        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
                .setNonce(generateNonce())
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )
            handleSignIn(result)
        } catch (e: GetCredentialException) {
            Log.e("GoogleAuthClient", "Sign-in failed", e)
            null
        } catch (e: IllegalArgumentException) {
            Log.e("GoogleAuthClient", "Invalid argument during sign in", e)
            null
        } catch (e: Exception) {
            Log.e("GoogleAuthClient", "Unexpected error during sign in", e)
            null
        }
    }

    private suspend fun handleSignIn(result: GetCredentialResponse): AuthResult? {
        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            return try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(firebaseCredential).await()
            } catch (e: GoogleIdTokenParsingException) {
                Log.e("GoogleAuthClient", "Received an invalid google id token response", e)
                null
            } catch (e: Exception) {
                Log.e("GoogleAuthClient", "Firebase authentication failed", e)
                null
            }
        } else {
            Log.e("GoogleAuthClient", "Unexpected type of credential")
            return null
        }
    }

    private suspend fun generateNonce(): String = withContext(Dispatchers.Default) {
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        digest.joinToString("") { "%02x".format(it) }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun signInAsGuest(): AuthResult? {
        return try {
            auth.signInAnonymously().await()
        } catch (e: Exception) {
            Log.e("GoogleAuthClient", "Guest sign-in failed", e)
            null
        }
    }
}
