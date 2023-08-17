package com.microblocklabs.mpc.interceptor

import android.util.Log
import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener
import io.grpc.Metadata
import io.grpc.MethodDescriptor


class GrpcClientResponseInterceptor : ClientInterceptor {

    override fun <ReqT, RespT> interceptCall(
        methodDescriptor: MethodDescriptor<ReqT, RespT>?,
        callOptions: CallOptions?,
        channel: Channel
    ): ClientCall<ReqT, RespT> {
        return object : SimpleForwardingClientCall<ReqT, RespT>(
            channel.newCall(methodDescriptor, callOptions)
        ) {
            override fun start(responseListener: Listener<RespT>?, headers: Metadata) {
                super.start(
                    object : SimpleForwardingClientCallListener<RespT>(
                        responseListener
                    ) {
                        override fun onMessage(message: RespT) {
                            val keyData = Metadata.Key.of(ACCESS_TOKEN, Metadata.ASCII_STRING_MARSHALLER)
                            val jwtString = headers.get(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER))
                            Log.d("Received header from Server: ", keyData.originalName())
                            Log.d("Received response from Server: ", jwtString.toString())
                            super.onMessage(message)
                        }
                    },
                    headers
                )
            }
        }
    }

    companion object {
        private const val ACCESS_TOKEN = "myTuthorization"
    }
}