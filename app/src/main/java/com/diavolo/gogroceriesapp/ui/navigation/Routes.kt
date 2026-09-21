package com.diavolo.gogroceriesapp.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object Home

@Serializable
data object Analytics

@Serializable
data class ListDetail(val listId: Long)

@Serializable
data class ActiveShopping(val listId: Long)

@Serializable
data class TripSummary(val listId: Long)
