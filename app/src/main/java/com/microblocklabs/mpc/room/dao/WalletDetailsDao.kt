package com.microblocklabs.mpc.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.microblocklabs.mpc.room.entity.WalletDetails

@Dao
interface WalletDetailsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(walletDetails: WalletDetails)

    @Query("DELETE FROM WalletDetails")
    suspend fun deleteAll()

//    @Query("UPDATE UserProfileDetails SET AccessToken = :accessToken ,RefreshToken= :refreshToken,SecretKey= :secretKey WHERE UserId LIKE :userId ")
//    fun updateUserProfile(userId: String, accessToken: String, refreshToken: String, secretKey: String?)
//
// @Query("UPDATE UserProfileDetails SET UserPassword = :newPassword WHERE UserId LIKE :userId ")
//    fun updateUserPassword(userId: String,newPassword:String)
//
    @Query("SELECT * FROM WalletDetails")
    fun getWalletList(): List<WalletDetails>

    @Query("SELECT * FROM WalletDetails")
    fun getWalletListLive(): LiveData<List<WalletDetails>>
}