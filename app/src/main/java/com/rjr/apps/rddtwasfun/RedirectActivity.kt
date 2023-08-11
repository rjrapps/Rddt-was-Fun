package com.rjr.apps.rddtwasfun

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

    private companion object {
        private const val PROTOCOL_HTTPS = "https://"
        private const val SUBDOMAIN_OLD = "https://old."
        private const val SUBDOMAIN_WWW = "https://www."
        private const val REDDIT_URL = "reddit.com"
        private const val REDDIT_APP_LINK = "reddit.app.link"
        private const val REDDIT_MEDIA_LINK = "https://www.reddit.com/media"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            // Clear query parameter garbage
            val cleanedUrl = intent.data?.clearQueryParams() ?: run {
                onError()
                return@launch
            }

            // If a Reddit media link, we need to show it in new reddit (ugh)
            if (cleanedUrl.contains(REDDIT_MEDIA_LINK)) {
                startBrowser(
                    url = cleanedUrl,
                    temporarilyDisableDeeplinking = true
                )
                return@launch
            }

            // If it's already an old.reddit link (somehow), skip the rest
            if (cleanedUrl.contains(SUBDOMAIN_OLD)) {
                startBrowser(cleanedUrl)
                return@launch
            }

            fun String.checkUrl(): Boolean {
                // Replace https://www. with https://old.
                if (contains("$SUBDOMAIN_WWW$REDDIT_URL")) {
                    startBrowser(replace(SUBDOMAIN_WWW, SUBDOMAIN_OLD))
                    return true
                } else if (contains("$PROTOCOL_HTTPS$REDDIT_URL")) {
                    startBrowser(replace(PROTOCOL_HTTPS, SUBDOMAIN_OLD))
                    return true
                }

                return false
            }

            if (!cleanedUrl.checkUrl()) {
                try {
                    val urlTmp = URL(cleanedUrl)
                    val connection = urlTmp.openConnection() as HttpURLConnection

                    // Set desktop user-agent
                    connection.setRequestProperty( "User-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.4 (KHTML, like Gecko) Chrome/22.0.1229.94 Safari/537.4" )

                    // Need to call inputStream to get the redirectUrl
                    connection.inputStream

                    var redirectUrl = connection.url.toString()

                    if (cleanedUrl.contains(REDDIT_APP_LINK)) {
                        // Clean link again
                        redirectUrl = redirectUrl.clearQueryParams() ?: run {
                            onError()
                            return@launch
                        }
                    }

                    connection.disconnect()

                    if (!redirectUrl.checkUrl()) {
                        // All else fails
                        onError()
                    }
                } catch (e: Exception) {
                    Log.e(this@RedirectActivity::class.java.simpleName, e.message, e)
                    onError()
                }
            }
        }
    }

    private fun Uri.clearQueryParams(): String? {
        var cleanedUrl = toString().split("?").getOrNull(0)
        val contextCount = getQueryParameter("context")
        val urlParam = getQueryParameter("url")

        if (!urlParam.isNullOrBlank()) {
            cleanedUrl += "?url=$urlParam"
        }

        if (!contextCount.isNullOrBlank()) {
            cleanedUrl += if (!urlParam.isNullOrBlank()) "&" else "?"
            cleanedUrl += "context=$contextCount"
        }

        return cleanedUrl
    }

    private fun String.clearQueryParams(): String? {
        val cleanedUrl = toString().split("?").getOrNull(0)
        val contextCount = Uri.parse(this).getQueryParameter("context")

        if (!contextCount.isNullOrBlank()) {
            return "$cleanedUrl?context=$contextCount"
        }

        return cleanedUrl
    }

    private fun startBrowser(url: String, temporarilyDisableDeeplinking: Boolean = false) {
        fun startBrowser() {
            val uri = Uri.parse(url)

            startActivity(
                Intent(Intent.ACTION_VIEW)
                    .setData(uri)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }

        if (temporarilyDisableDeeplinking) {
            // Temporarily disable deeplinking
            setDeepLinkingState(PackageManager.COMPONENT_ENABLED_STATE_DISABLED)

            startBrowser()

            // Re-enable deeplinking
            setDeepLinkingState(PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
        } else {
            startBrowser()
        }

        finish()
    }

    /**
     * Show error toast and launch unedited link in browser.
     */
    private fun onError() {
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(
                this@RedirectActivity,
                R.string.error,
                Toast.LENGTH_LONG
            ).show()

            startBrowser(
                url = intent.data?.toString() ?: return@launch,
                temporarilyDisableDeeplinking = true
            )
        }
    }

    private fun setDeepLinkingState(state: Int) {
        val compName = ComponentName(packageName, this::class.java.name)
        packageManager.setComponentEnabledSetting(compName, state, PackageManager.DONT_KILL_APP)
    }
}
