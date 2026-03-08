package com.smartcloud.audiobook.data.auth

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.smartcloud.audiobook.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    private val googleAccountStore: GoogleAccountStore,
) {
    suspend fun signInForDriveReadOnly(activity: Activity): Result<String> {
        val credentialManager = CredentialManager.create(activity)
        val serverClientId = activity.getString(R.string.google_web_client_id)

        if (serverClientId.isBlank()) {
            return Result.failure(IllegalStateException("google_web_client_id is empty"))
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(serverClientId)
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return runCatching {
            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential
            if (
                credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val accountName = googleCredential.id
                googleAccountStore.saveSelectedAccountName(accountName)
                accountName
            } else {
                throw IllegalStateException("Google credential was not returned")
            }
        }
    }

    fun getSignedInAccountName(): String? = googleAccountStore.getSelectedAccountName()
}
