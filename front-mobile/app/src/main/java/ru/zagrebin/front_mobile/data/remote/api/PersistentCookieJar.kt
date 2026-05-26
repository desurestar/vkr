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
                existing.cookie.name == incoming.name &&
                    existing.cookie.domain == incoming.domain &&
                    existing.cookie.path == incoming.path
            }
            if (!incoming.persistent || incoming.expiresAt >= System.currentTimeMillis()) {
                all.add(CookieEntry(url, incoming))
            }
        }
        writeAllCookies(all)
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        val all = readAllCookies().toMutableList()
        val valid = all.filter { it.cookie.expiresAt >= now }
        if (valid.size != all.size) writeAllCookies(valid)
        return valid.map { it.cookie }.filter { it.matches(url) }
    }

    private fun readAllCookies(): List<CookieEntry> {
        val raw = prefs.getStringSet(KEY_COOKIES, emptySet()) ?: emptySet()
        return raw.mapNotNull { encoded ->
            decodeCookie(encoded)
        }.distinctBy { "${it.cookie.name}|${it.cookie.domain}|${it.cookie.path}" }
    }

    private fun writeAllCookies(cookies: List<CookieEntry>) {
        prefs.edit().putStringSet(KEY_COOKIES, cookies.map { encodeCookie(it) }.toSet()).apply()
    }

    private fun encodeCookie(entry: CookieEntry): String {
        return "${entry.url.scheme}|${entry.url.host}|${entry.cookie}"
    }

    private fun decodeCookie(encoded: String): CookieEntry? {
        val parts = encoded.split('|', limit = 3)
        if (parts.size < 3) return null
        val scheme = parts[0]
        val host = parts[1]
        val header = parts[2]
        val origin = "$scheme://$host/".toHttpUrl()
        val cookie = Cookie.parse(origin, header) ?: return null
        return CookieEntry(origin, cookie)
    }

    private data class CookieEntry(val url: HttpUrl, val cookie: Cookie)

    companion object {
        private const val PREFS_NAME = "http_cookies"
        private const val KEY_COOKIES = "cookie_values"
    }
}
