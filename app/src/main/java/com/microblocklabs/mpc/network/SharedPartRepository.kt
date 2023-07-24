package com.microblocklabs.mpc.network

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.microblocklabs.mpc.room.dao.SharedPartDao
import com.microblocklabs.mpc.room.entity.SharePartDetails

class SharedPartRepository(private val sharedPartDao: SharedPartDao)  {

    private val sharedPartList = sharedPartDao.getSharedPartDetailsListLive()

    @WorkerThread
    suspend fun insert(sharePartDetails: SharePartDetails){
        sharedPartDao.insert(sharePartDetails)
    }

    @WorkerThread
    suspend fun deleteData() {
        sharedPartDao.deleteAll()
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



    fun getSharedPartDetailsLive(): LiveData<List<SharePartDetails>> {
        return sharedPartList
    }

}