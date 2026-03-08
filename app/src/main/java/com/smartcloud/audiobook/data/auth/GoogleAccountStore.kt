package com.smartcloud.audiobook.data.auth

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAccountStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getSelectedAccountName(): String? = prefs.getString(KEY_ACCOUNT_NAME, null)

    fun saveSelectedAccountName(accountName: String) {
        prefs.edit().putString(KEY_ACCOUNT_NAME, accountName).apply()
    }

    companion object {
        private const val PREF_NAME = "smart_cloud_auth"
        private const val KEY_ACCOUNT_NAME = "selected_account_name"
    }
}
