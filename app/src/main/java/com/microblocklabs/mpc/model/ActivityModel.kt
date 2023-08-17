package com.microblocklabs.mpc.model

data class ActivityModel(
//    var activityIcon : Int,
    var activityName : String,
    var dateStr : String,
    var activitySenderAddress : String,
    var activityReceiverAddress : String,
    var activityAmount : String,
    var transactionHash : String

)
