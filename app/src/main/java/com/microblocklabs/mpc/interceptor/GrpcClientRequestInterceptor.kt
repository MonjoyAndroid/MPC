package com.microblocklabs.mpc.interceptor

import android.util.Log
import aot.armsproject.utils.AppSharedPreferenceManager
import com.microblocklabs.mpc.utility.Constant
import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall
import io.grpc.Metadata
import io.grpc.MethodDescriptor


class GrpcClientRequestInterceptor(mpcSharedPref: AppSharedPreferenceManager) : ClientInterceptor {
    val token =  mpcSharedPref.getString(Constant.SessionToken)
    override fun <ReqT, RespT> interceptCall(
        methodDescriptor: MethodDescriptor<ReqT, RespT>?,
        callOptions: CallOptions?,
        channel: Channel
    ): ClientCall<ReqT, RespT> {
        return object : SimpleForwardingClientCall<ReqT, RespT>(channel.newCall(methodDescriptor, callOptions)) {
            override fun start(responseListener: Listener<RespT>?, headers: Metadata) {
                val userToken = "Bearer $token"
                headers.put(Metadata.Key.of(ACCESS_TOKEN, Metadata.ASCII_STRING_MARSHALLER), userToken)
                super.start(responseListener, headers)
            }
        }
    }

    companion object {
        private const val ACCESS_TOKEN = "authorization"
    }
}