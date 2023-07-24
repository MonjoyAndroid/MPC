package com.microblocklabs.mpc.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "WalletDetails")
data class WalletDetails(
    @PrimaryKey @ColumnInfo(name = "EthereumAddress") var ethereumAddress: String,
    @ColumnInfo(name = "Email") var email: String,
    @ColumnInfo(name = "AccountCount") var accountCount: Int,
    @ColumnInfo(name = "AccountName") var accountName: String,
    @ColumnInfo(name = "PublicKey") var publicKey: String,
    @ColumnInfo(name = "TwoPercent") var TwoPercent: Double,
    @ColumnInfo(name = "RestValue") var RestValue: Double,
    @ColumnInfo(name = "PerMonth") var PerMonth: Double,
    @ColumnInfo(name = "Cifd") var Cifd: Double,
    @ColumnInfo(name = "UniqueId") var uniqueId: String,
    @ColumnInfo(name = "IfUniqueId") var ifUniqueId: Boolean

)
