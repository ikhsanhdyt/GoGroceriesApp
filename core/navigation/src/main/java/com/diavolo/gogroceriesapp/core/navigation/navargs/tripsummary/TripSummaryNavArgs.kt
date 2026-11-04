package com.diavolo.gogroceriesapp.core.navigation.navargs.tripsummary

import com.diavolo.gogroceriesapp.core.navigation.base.BaseNavArgs
import kotlinx.serialization.Serializable

@Serializable
data class TripSummaryNavArgs(val listId: Long) : BaseNavArgs
