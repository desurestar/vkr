package ru.zagrebin.front_mobile.ui.screens.profile.components

sealed interface ProfileEvent {
    data object OnCreateClick : ProfileEvent
    data object OnMyPostsClick : ProfileEvent
    data object OnShoppingListClick : ProfileEvent
    data object OnDraftsClick : ProfileEvent
    data object OnEditAccountClick : ProfileEvent
    data object OnSecurityClick : ProfileEvent
}
