package com.novaos.launcher.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for hidden/locked apps.
 */
@Entity(tableName = "hidden_apps")
data class HiddenAppEntity(
    @PrimaryKey
    val packageName: String,
    val locked: Boolean = false
)
