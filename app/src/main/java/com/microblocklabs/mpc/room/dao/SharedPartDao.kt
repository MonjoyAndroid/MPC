package com.microblocklabs.mpc.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.microblocklabs.mpc.room.entity.SharePartDetails
import com.microblocklabs.mpc.room.entity.WalletDetails

@Dao
interface SharedPartDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(sharePartDetails: SharePartDetails)

    @Query("DELETE FROM SharePartDetails")
    suspend fun deleteAll()

    //    @Query("UPDATE UserProfileDetails SET AccessToken = :accessToken ,RefreshToken= :refreshToken,SecretKey= :secretKey WHERE UserId LIKE :userId ")
//    fun updateUserProfile(userId: String, accessToken: String, refreshToken: String, secretKey: String?)
//
// @Query("UPDATE UserProfileDetails SET UserPassword = :newPassword WHERE UserId LIKE :userId ")
//    fun updateUserPassword(userId: String,newPassword:String)
//
    @Query("SELECT * FROM SharePartDetails")
    fun getSharedPartDetailsList(): List<SharePartDetails>

    @Query("SELECT * FROM SharePartDetails")
    fun getSharedPartDetailsListLive(): LiveData<List<SharePartDetails>>

}

