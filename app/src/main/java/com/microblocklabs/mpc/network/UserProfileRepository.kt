package com.microblocklabs.mpc.network

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.microblocklabs.mpc.room.dao.UserProfileDao
import com.microblocklabs.mpc.room.entity.UserProfile

class UserProfileRepository(private val userProfileDao: UserProfileDao) {

    private val userProfileList = userProfileDao.getUserProfileLive()

    @WorkerThread
    suspend fun insert(userProfile: UserProfile){
        userProfileDao.insert(userProfile)
    }

    @WorkerThread
    suspend fun update(userProfile: UserProfile){
        userProfileDao.update(userProfile)
    }

    @WorkerThread
    suspend fun deleteData() {
        userProfileDao.deleteAll()
    }

    @WorkerThread
    fun updateUserConfirmationStatus(email:String, isConfirmed:Boolean){
        userProfileDao.updateUserConfirmationStatus(email, isConfirmed)
    }

//    @WorkerThread
//    fun updateUserProfile(userId:String, accessToken: String, refreshToken: String, secretKey: String) {
//        userProfileDao.updateUserProfile(userId, accessToken, refreshToken, secretKey)
//    }
//
//     @WorkerThread
//    fun updateNewUserPassword(userId:String, password:String) {
//        userProfileDao.updateUserPassword(userId, password)
//    }



    fun getUserProfileLive(): LiveData<List<UserProfile>> {
        return userProfileList
    }

}