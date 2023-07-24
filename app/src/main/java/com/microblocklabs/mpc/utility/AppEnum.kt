package com.microblocklabs.mpc.utility

sealed class AppEnum

object HttpResponseCode : AppEnum(){
    const val SUCCESS = 200
    const val FAILED = 400
}