package ru.zagrebin.front_mobile.data.remote.api

import android.content.Context
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

class PersistentCookieJar(context: Context) : CookieJar {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (cookies.isEmpty()) return

        val all = readAllCookies().toMutableList()
        cookies.forEach { incoming ->
            all.removeAll { existing ->
                existing.name == incoming.name &&
                    existing.domain == incoming.domain &&
                    existing.path == incoming.path
            }
            if (!incoming.persistent || incoming.expiresAt >= System.currentTimeMillis()) {
                all.add(incoming)
            }
        }
        writeAllCookies(all)
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        val all = readAllCookies().toMutableList()
        val valid = all.filter { it.expiresAt >= now }
        if (valid.size != all.size) writeAllCookies(valid)
        return valid.filter { it.matches(url) }
    }

    private fun readAllCookies(): List<Cookie> {
        val raw = prefs.getStringSet(KEY_COOKIES, emptySet()) ?: emptySet()
        val fallbackUrl = "https://localhost/".toHttpUrl()
        return raw.mapNotNull { cookieHeader ->
            Cookie.parse(fallbackUrl, cookieHeader)
        }.distinctBy { "${it.name}|${it.domain}|${it.path}" }
    }

    private fun writeAllCookies(cookies: List<Cookie>) {
        prefs.edit().putStringSet(KEY_COOKIES, cookies.map { it.toString() }.toSet()).apply()
    }

    companion object {
        private const val PREFS_NAME = "http_cookies"
        private const val KEY_COOKIES = "cookie_values"
    }
}
