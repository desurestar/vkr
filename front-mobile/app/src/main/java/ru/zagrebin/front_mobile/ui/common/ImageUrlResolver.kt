package ru.zagrebin.front_mobile.ui.common

import ru.zagrebin.front_mobile.data.AppContainer

fun String?.asImageModelUrl(): String? = AppContainer.resolveMediaUrl(this)
