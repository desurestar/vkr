package ru.zagrebin.front_mobile.data.remote.dto

data class TagDto(
    val id: Long,
    val name: String,
    val label: String? = null,
    val color: String? = null
)
