package ru.zagrebin.front_mobile.data.remote.api

import android.content.Context
import android.util.Base64
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
        val cookie = entry.cookie
        val parts = listOf(
            entry.url.scheme,
            entry.url.host,
            cookie.name,
            cookie.value,
            cookie.domain,
            cookie.path,
            cookie.expiresAt.toString(),
            cookie.secure.toString(),
            cookie.httpOnly.toString(),
            cookie.hostOnly.toString(),
            cookie.persistent.toString()
        ).map { encodePart(it) }
        return parts.joinToString("|")
    }

    private fun decodeCookie(encoded: String): CookieEntry? {
        val parts = encoded.split('|')
        if (parts.size < 11) return null
        val decoded = parts.map { decodePart(it) }
        val scheme = decoded[0]
        val host = decoded[1]
        val name = decoded[2]
        val value = decoded[3]
        val domain = decoded[4]
        val path = decoded[5]
        val expiresAt = decoded[6].toLongOrNull() ?: return null
        val secure = decoded[7].toBoolean()
        val httpOnly = decoded[8].toBoolean()
        val hostOnly = decoded[9].toBoolean()
        val persistent = decoded[10].toBoolean()

        val origin = "$scheme://$host/".toHttpUrl()
        val builder = Cookie.Builder()
            .name(name)
            .value(value)
            .path(path)
        if (hostOnly) {
            builder.hostOnlyDomain(domain)
        } else {
            builder.domain(domain)
        }
        if (secure) builder.secure()
        if (httpOnly) builder.httpOnly()
        if (persistent) builder.expiresAt(expiresAt)

        val cookie = runCatching { builder.build() }.getOrNull() ?: return null
        return CookieEntry(origin, cookie)
    }

    private fun encodePart(value: String): String =
        Base64.encodeToString(value.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

    private fun decodePart(value: String): String =
        String(Base64.decode(value, Base64.NO_WRAP), Charsets.UTF_8)

    private data class CookieEntry(val url: HttpUrl, val cookie: Cookie)

    companion object {
        private const val PREFS_NAME = "http_cookies"
        private const val KEY_COOKIES = "cookie_values"
    }
}
