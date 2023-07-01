package com.rjr.apps.ripwasfun

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL


class RedirectActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {

            // Clear query parameter garbage
            val cleanedUrl = intent.data?.toString()?.split("?")?.getOrNull(0) ?: run {
                onError()
                return@launch
            }

            // If it's already an old.reddit link (somehow), skip the rest
            if (cleanedUrl.contains("https://old.")) {
                startBrowser(cleanedUrl)
                return@launch
            }

            // Replace https://www. with https://old.
            if (cleanedUrl.contains("https://www.reddit.com")) {
                startBrowser(cleanedUrl.replace("https://www.", "https://old."))
            } else if (cleanedUrl.contains("https://reddit.com")) {
                startBrowser(cleanedUrl.replace("https://", "https://old."))
            } else {
                try {
                    val urlTmp = URL(cleanedUrl)
                    val connection = urlTmp.openConnection() as HttpURLConnection
                    connection.setRequestProperty( "User-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.4 (KHTML, like Gecko) Chrome/22.0.1229.94 Safari/537.4" )
                    connection.inputStream // Need to call inputStream to get the redirectUrl

                    var redirectUrl = connection.url.toString()

                    if (cleanedUrl.contains("reddit.app.link")) {
                        // Clean link again
                        redirectUrl = redirectUrl.split("?").getOrNull(0) ?: run {
                            onError()
                            return@launch
                        }
                    }

                    connection.disconnect()

                    // Replace https://www. with https://old.
                    if (redirectUrl.contains("https://www.reddit.com")) {
                        startBrowser(redirectUrl.replace("https://www.", "https://old."))
                    } else if (redirectUrl.contains("https://reddit.com")) {
                        startBrowser(redirectUrl.replace("https://", "https://old."))
                    } else {
                        // All else fails
                        onError()
                    }
                } catch (e: Exception) {
                    Log.e("RedirectActivity", e.message, e)
                    onError()
                }
            }
        }
    }

    private fun startBrowser(url: String) {
        startActivity(
            Intent(Intent.ACTION_VIEW)
                .setData(
                    Uri.parse(url)
                )
        )

        finish()
    }

    /**
     * Show error toast and launch unedited link in browser.
     */
    private fun onError() {
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(this@RedirectActivity, R.string.error, Toast.LENGTH_LONG).show()

            setDeepLinkingState(PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
            startBrowser(
                url = intent.data?.toString() ?: run {
                    setDeepLinkingState(PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
                    finish()
                    return@launch
                }
            )
            setDeepLinkingState(PackageManager.COMPONENT_ENABLED_STATE_ENABLED)

            finish()
        }
    }

    private fun setDeepLinkingState(state: Int) {
        val compName = ComponentName(packageName, this::class.java.name)
        packageManager.setComponentEnabledSetting(compName, state, PackageManager.DONT_KILL_APP)
    }
}
