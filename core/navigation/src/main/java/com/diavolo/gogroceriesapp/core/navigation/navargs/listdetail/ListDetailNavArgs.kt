package com.diavolo.gogroceriesapp.core.navigation.navargs.listdetail

import com.diavolo.gogroceriesapp.core.navigation.base.BaseNavArgs
import kotlinx.serialization.Serializable

@Serializable
data class ListDetailNavArgs(val listId: Long) : BaseNavArgs
