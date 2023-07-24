package com.microblocklabs.mpc.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.microblocklabs.mpc.room.dao.SharedPartDao
import com.microblocklabs.mpc.utility.Constant
import com.microblocklabs.mpc.room.dao.UserProfileDao
import com.microblocklabs.mpc.room.dao.WalletDetailsDao
import com.microblocklabs.mpc.room.entity.SharePartDetails
import com.microblocklabs.mpc.room.entity.UserProfile
import com.microblocklabs.mpc.room.entity.WalletDetails

@Database(entities = [UserProfile::class, WalletDetails::class, SharePartDetails::class], version = Constant.DATABASE_VERSION, exportSchema = false)
abstract class MPCDatabase : RoomDatabase() {
//    abstract fun mClientProfileDao(): ClientProfileDao?
    abstract fun mWalletDetailsDao(): WalletDetailsDao?
//    abstract fun mTabUserListDao(): TabUserListDao?
    abstract fun mUserProfileDao(): UserProfileDao?
    abstract fun mSharedPartDao(): SharedPartDao?
//    abstract fun mTemplateInfoDao(): TemplateInfoDao?
//    abstract fun mTabAppTemplateDetailsDao(): TabAppTemplateDetailsDao?
//    abstract fun mGroupInfoDao(): GroupInfoDao?
//    abstract fun mCodeLibraryDao(): CodeLibraryDao?
//    abstract fun mBulletinListDao(): BulletinListDao?


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