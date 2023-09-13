package com.microblocklabs.mpc.room.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.microblocklabs.mpc.network.VestingDetailsRepository
import com.microblocklabs.mpc.room.MPCDatabase
import com.microblocklabs.mpc.room.entity.VestingDetails
import kotlinx.coroutines.launch

class VestingDetailsViewModel(application: Application): AndroidViewModel(application) {

    private val vestingDetailsRepository: VestingDetailsRepository
    val vestingDetailsList: LiveData<List<VestingDetails>>

    init {
        val dbVestingDetails = MPCDatabase.getDatabase(application)!!.mVestingDetailsDao()
        vestingDetailsRepository = VestingDetailsRepository(dbVestingDetails!!)
        vestingDetailsList = vestingDetailsRepository.getVestingDetailsLive()
    }

    fun insertVestingDetail(vestingDetails: VestingDetails){
        viewModelScope.launch {
            vestingDetailsRepository.insert(vestingDetails)
        }
    }

    fun deleteVestingDetail(){
        viewModelScope.launch {
            vestingDetailsRepository.deleteData()
        }
    }
}