package com.microblocklabs.mpc.room.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.microblocklabs.mpc.room.MPCDatabase
import com.microblocklabs.mpc.network.WalletDetailsRepository
import com.microblocklabs.mpc.room.entity.WalletDetails
import kotlinx.coroutines.launch

class WalletDetailsViewModel(application: Application): AndroidViewModel(application) {

    private val walletDetailsRepository: WalletDetailsRepository
    private val walletdetailsList: LiveData<List<WalletDetails>>

    init {
        val dbWalletdetails = MPCDatabase.getDatabase(application)!!.mWalletDetailsDao()
        walletDetailsRepository = WalletDetailsRepository(dbWalletdetails!!)
        walletdetailsList = walletDetailsRepository.getWalletDetailsLive()
    }

    fun insertWalletDetail(walletDetails: WalletDetails){
        viewModelScope.launch {
            walletDetailsRepository.insert(walletDetails)
        }
    }

    fun deleteWalletDetail(){
        viewModelScope.launch {
            walletDetailsRepository.deleteData()
        }
    }

    fun updateWalletDetailData(userId: String, accessToken: String, refreshToken: String, secretKey: String){
        viewModelScope.launch {
//            userProfileRepository.updateWalletDetail(userId, accessToken, refreshToken, secretKey)
        }
    }

    fun updateNewPassword(userId: String, encrytedPassword: String){
        viewModelScope.launch {
//            userProfileRepository.updateNewUserPassword(userId, encrytedPassword)
        }
    }

}