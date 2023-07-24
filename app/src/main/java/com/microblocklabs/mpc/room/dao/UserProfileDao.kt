package com.microblocklabs.mpc.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.microblocklabs.mpc.room.entity.UserProfile

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(userProfile: UserProfile)

    @Query("DELETE FROM UserProfileDetails")
    suspend fun deleteAll()

//    @Query("UPDATE UserProfileDetails SET AccessToken = :accessToken ,RefreshToken= :refreshToken,SecretKey= :secretKey WHERE UserId LIKE :userId ")
//    fun updateUserProfile(userId: String, accessToken: String, refreshToken: String, secretKey: String?)
//
// @Query("UPDATE UserProfileDetails SET UserPassword = :newPassword WHERE UserId LIKE :userId ")
//    fun updateUserPassword(userId: String,newPassword:String)

    @Query("UPDATE UserProfileDetails SET IsEmailVerified = :isConfirmed WHERE Email LIKE :email")
    fun updateUserConfirmationStatus(email: String, isConfirmed: Boolean)
//
    @Query("SELECT * FROM UserProfileDetails")
    fun getUserProfile(): List<UserProfile>

    @Query("SELECT * FROM UserProfileDetails")
    fun getUserProfileLive(): LiveData<List<UserProfile>>
}