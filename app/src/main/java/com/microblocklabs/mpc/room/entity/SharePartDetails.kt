package com.microblocklabs.mpc.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SharePartDetails")
data class SharePartDetails(
    @PrimaryKey @ColumnInfo(name = "ethereum_address") var ethereumAddress: String,
    @ColumnInfo(name = "share_part_first") var x: String,
    @ColumnInfo(name = "share_part_second") var y: String
)
