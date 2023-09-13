package com.microblocklabs.mpc.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.microblocklabs.mpc.room.entity.VestingDetails

@Dao
interface VestingDetailsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertData(data: VestingDetails)

    @Query("DELETE FROM VestingDetails")
    suspend fun deleteAll()

    @Query("SELECT * FROM VestingDetails")
    fun getAllVestingData(): LiveData<List<VestingDetails>>
}