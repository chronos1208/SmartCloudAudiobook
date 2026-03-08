package com.smartcloud.audiobook.core.di

import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.smartcloud.audiobook.data.auth.GoogleAccountStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

    @Provides
    @Singleton
    fun provideDriveHttpDataSourceFactory(
        credential: GoogleAccountCredential,
        accountStore: GoogleAccountStore,
    ): DataSource.Factory {
        return DriveAuthHttpDataSourceFactory(
            credential = credential,
            accountStore = accountStore,
        )
    }
}

private class DriveAuthHttpDataSourceFactory(
    private val credential: GoogleAccountCredential,
    private val accountStore: GoogleAccountStore,
) : HttpDataSource.Factory {

    private val delegateFactory = DefaultHttpDataSource.Factory()
        .setUserAgent("SmartCloudAudiobook")

    override fun createDataSource(): HttpDataSource {
        credential.selectedAccountName = accountStore.getSelectedAccountName()
        val token = credential.token.orEmpty()
        return delegateFactory.createDataSource().apply {
            setRequestProperty("Authorization", "Bearer $token")
        }
    }

    override fun setDefaultRequestProperties(defaultRequestProperties: Map<String, String>): HttpDataSource.Factory {
        delegateFactory.setDefaultRequestProperties(defaultRequestProperties)
        return this
    }
}
