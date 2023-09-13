package com.microblocklabs.mpc.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.microblocklabs.mpc.room.dao.SharedPartDao
import com.microblocklabs.mpc.utility.Constant
import com.microblocklabs.mpc.room.dao.UserProfileDao
import com.microblocklabs.mpc.room.dao.VestingDetailsDao
import com.microblocklabs.mpc.room.dao.WalletDetailsDao
import com.microblocklabs.mpc.room.entity.SharePartDetails
import com.microblocklabs.mpc.room.entity.UserProfile
import com.microblocklabs.mpc.room.entity.VestingDetails
import com.microblocklabs.mpc.room.entity.WalletDetails

@Database(entities = [UserProfile::class, WalletDetails::class, SharePartDetails::class, VestingDetails::class], version = Constant.DATABASE_VERSION, exportSchema = false)
abstract class MPCDatabase : RoomDatabase() {

    abstract fun mWalletDetailsDao(): WalletDetailsDao?
    abstract fun mUserProfileDao(): UserProfileDao?
    abstract fun mSharedPartDao(): SharedPartDao?
    abstract fun mVestingDetailsDao(): VestingDetailsDao?



    companion object {
        @Volatile
        private var database: MPCDatabase? = null
        @JvmStatic
        fun getDatabase(context: Context): MPCDatabase? {
            if (database == null) {
                synchronized(MPCDatabase::class.java) {
                    if (database == null) {
                        database = Room.databaseBuilder(context.applicationContext, MPCDatabase::class.java, "MPC_DB")
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build()
                    }
                }
            }
            return database
        }
    }
}