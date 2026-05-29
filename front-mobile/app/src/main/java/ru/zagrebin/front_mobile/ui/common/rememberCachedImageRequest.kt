package ru.zagrebin.front_mobile.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil.request.CachePolicy
import coil.request.ImageRequest

@Composable
fun rememberCachedImageRequest(url: String?): ImageRequest? {
    val context = LocalContext.current
    val data = url?.takeIf { it.isNotBlank() } ?: return null
    return remember(context, data) {
        ImageRequest.Builder(context)
            .data(data)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .build()
    }
}
