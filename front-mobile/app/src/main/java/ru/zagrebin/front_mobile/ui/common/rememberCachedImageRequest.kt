package ru.zagrebin.front_mobile.ui.common

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil.request.CachePolicy
import coil.request.ImageRequest

@Composable
fun rememberExplicitCacheImageRequest(url: String?): ImageRequest? {
    val context = LocalContext.current
    val data = url.asImageModelUrl()?.trim()?.takeIf { it.isNotBlank() } ?: return null
    return remember(context, data) {
        ImageRequest.Builder(context)
            .data(data.toImageRequestData())
            .memoryCacheKey(data)
            .diskCacheKey(data)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .build()
    }
}

private fun String.toImageRequestData(): Any = when {
    startsWith("content://", ignoreCase = true) -> Uri.parse(this)
    startsWith("file://", ignoreCase = true) -> Uri.parse(this)
    else -> this
}
