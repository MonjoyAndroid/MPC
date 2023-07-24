package com.microblocklabs.mpc.network

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.microblocklabs.mpc.room.dao.UserProfileDao
import com.microblocklabs.mpc.room.dao.WalletDetailsDao
import com.microblocklabs.mpc.room.entity.UserProfile
import com.microblocklabs.mpc.room.entity.WalletDetails

class WalletDetailsRepository(private val walletDetailsDao: WalletDetailsDao) {

    private val userWalletList = walletDetailsDao.getWalletListLive()

    @WorkerThread
    suspend fun insert(walletDetails: WalletDetails){
        walletDetailsDao.insert(walletDetails)
    }

    @WorkerThread
    suspend fun deleteData() {
        walletDetailsDao.deleteAll()
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



    fun getWalletDetailsLive(): LiveData<List<WalletDetails>> {
        return userWalletList
    }

}