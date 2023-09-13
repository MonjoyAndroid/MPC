package com.microblocklabs.mpc.utility

import java.math.BigDecimal


object Constant {
    const val DATABASE_VERSION = 1
    const val baseURLForDev = "54.145.255.3"
    const val baseURLForProd = "34.201.39.208"
    const val portNumber = 50051
    const val UserID = "UserID"
    const val SessionToken = "SessionToken"
    const val privacyPolicyUrl = "https://cifdaqwallet.com/assets/pdf/privacy-policy.pdf"
    const val termsConditionsUrl = "https://cifdaqscan.io/assets/cifdaqwallet_terms_conditions.pdf"
    const val ClientName = "ClientName"
    const val RegisterType = "RegisterType"
    const val ClientLogo = "ClientLogo"
    const val HeaderColour = "HeaderColour"
    const val SubHeaderColour = "SubHeaderColour"
    const val SubHeaderColourDark = "SubHeaderColourDark"
    const val SubHeaderColourlight = "SubHeaderColourlight"
    const val HeaderTextColour = "HeaderTextColour"
    const val IconColour = "IconColour"
    const val DottedLineColour = "DottedLineColour"
    const val ButtonColor = "ButtonColor"
    const val SubscriberSessionGUID = "SubscriberSessionGUID"
    const val AppName = "AppName"
    val tokenValWithUSD: BigDecimal = BigDecimal("0.8")
    const val INACTIVITY_TIMEOUT_MS: Long = 10 * 60 * 1000 // 10 minutes
}