package com.microblocklabs.mpc.model

import java.math.BigDecimal

data class BalanceDetails(
    var balance : BigDecimal,
    var walletAddress : String,
)
