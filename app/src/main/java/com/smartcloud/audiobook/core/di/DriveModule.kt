package com.smartcloud.audiobook.core.di

import android.content.Context
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.smartcloud.audiobook.data.auth.GoogleAccountStore
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
    fun provideGoogleAccountCredential(
        @ApplicationContext context: Context,
        googleAccountStore: GoogleAccountStore,
    ): GoogleAccountCredential {
        return GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_READONLY),
        ).apply {
            selectedAccountName = googleAccountStore.getSelectedAccountName()
        }
    }

    @Provides
    @Singleton
    fun provideDriveService(credential: GoogleAccountCredential): Drive {
        return Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory.getDefaultInstance(),
            credential,
        ).setApplicationName("SmartCloud Audiobook")
            .build()
    }
}
