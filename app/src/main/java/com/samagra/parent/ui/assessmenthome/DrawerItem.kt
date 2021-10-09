package com.samagra.parent.ui.assessmenthome

import com.samagra.parent.ui.DrawerOptions

data class DrawerItem(
    val name: String,
    val icon: Int,
    var drawerOptionType: DrawerOptions,
    var enabled: Boolean
)