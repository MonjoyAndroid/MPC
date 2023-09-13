package com.microblocklabs.mpc.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "VestingDetails")
data class VestingDetails(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val totalAmount: String,
    val lockedAmount: String,
    val unlockedAmount: String,
    val perMonth: String,
    val twoPercentage: String,
    val claimedAmount: String,
    val joinedVestingCycle: String,
    val claimedVestingCycle: String,
    val isUserActive: Boolean
)
