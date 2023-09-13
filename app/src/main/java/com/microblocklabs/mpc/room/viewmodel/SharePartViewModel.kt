package com.microblocklabs.mpc.room.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.microblocklabs.mpc.network.SharedPartRepository
import com.microblocklabs.mpc.network.UserProfileRepository
import com.microblocklabs.mpc.room.MPCDatabase
import com.microblocklabs.mpc.room.entity.SharePartDetails
import com.microblocklabs.mpc.room.entity.UserProfile
import kotlinx.coroutines.launch

class SharePartViewModel(application: Application): AndroidViewModel(application) {

    private val sharedPartRepository: SharedPartRepository
    val sharedPartList: LiveData<List<SharePartDetails>>

    init {
        val dbSharedPart = MPCDatabase.getDatabase(application)!!.mSharedPartDao()
        sharedPartRepository = SharedPartRepository(dbSharedPart!!)
        sharedPartList = sharedPartRepository.getSharedPartDetailsLive()
    }

    fun insertSharedPart(sharePartDetails: SharePartDetails){
        viewModelScope.launch {
            sharedPartRepository.insert(sharePartDetails)
        }
    }

    fun deleteSharedPart(){
        viewModelScope.launch {
            sharedPartRepository.deleteData()
        }
    }

    fun updateUserConfirmation(email:String, isConfirmed:Boolean){
        viewModelScope.launch {
//            sharedPartRepository.updateUserConfirmationStatus(email, isConfirmed)
        }
    }

    fun updateUserProfileData(userId: String, accessToken: String, refreshToken: String, secretKey: String){
        viewModelScope.launch {
//            userProfileRepository.updateUserProfile(userId, accessToken, refreshToken, secretKey)
        }
    }

    fun updateNewPassword(userId: String, encrytedPassword: String){
        viewModelScope.launch {
//            userProfileRepository.updateNewUserPassword(userId, encrytedPassword)
        }
    }
}