package com.microblocklabs.mpc.network

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.microblocklabs.mpc.room.dao.VestingDetailsDao
import com.microblocklabs.mpc.room.dao.WalletDetailsDao
import com.microblocklabs.mpc.room.entity.VestingDetails
import com.microblocklabs.mpc.room.entity.WalletDetails

class VestingDetailsRepository(private val vestingDetailsDao: VestingDetailsDao) {

    private val userVestingList = vestingDetailsDao.getAllVestingData()

    @WorkerThread
    suspend fun insert(vestingDetails: VestingDetails){
        vestingDetailsDao.insertData(vestingDetails)
    }

    @WorkerThread
    suspend fun deleteData() {
        vestingDetailsDao.deleteAll()
    }

    fun getVestingDetailsLive(): LiveData<List<VestingDetails>> {
        return userVestingList
    }
}