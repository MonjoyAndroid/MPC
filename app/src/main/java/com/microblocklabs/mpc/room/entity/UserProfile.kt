package com.microblocklabs.mpc.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "UserProfileDetails")
data class UserProfile (
        @PrimaryKey
        @ColumnInfo(name = "Email") var email: String,
        @ColumnInfo(name = "IsEmailVerified") var emailVerified: Boolean,
        @ColumnInfo(name = "PhoneNumber") var phoneNumber: String,
        @ColumnInfo(name = "IsPhoneNumberVerified") var phoneNumberVerified: Boolean,
        @ColumnInfo(name = "Mnemonic") var mnemonic: String,
        )