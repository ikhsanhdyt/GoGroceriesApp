package com.diavolo.gogroceriesapp.core.navigation.navargs.activeshopping

import com.diavolo.gogroceriesapp.core.navigation.base.BaseNavArgs
import kotlinx.serialization.Serializable

@Serializable
data class ActiveShoppingNavArgs(val listId: Long) : BaseNavArgs
