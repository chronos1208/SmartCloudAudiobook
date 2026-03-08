package com.smartcloud.audiobook.core.di

import android.content.Context
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DriveModule {
    @Provides
    @Singleton
    fun provideDriveService(@ApplicationContext context: Context): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_READONLY),
        )
        // TODO(Phase5): Set selected account from Google Sign-In / Credential Manager result.
        return Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory.getDefaultInstance(),
            credential,
        ).setApplicationName("SmartCloud Audiobook")
            .build()
    }
}
