package com.microblocklabs.mpc.utility

import com.google.gson.Gson
import com.microblocklabs.mpc.room.entity.SharePartDetails

object GSONConverter {
    private val gson = Gson()


    fun convertObjectToString(sharedPartDetails: SharePartDetails): String {
        return gson.toJson(sharedPartDetails)
    }

}