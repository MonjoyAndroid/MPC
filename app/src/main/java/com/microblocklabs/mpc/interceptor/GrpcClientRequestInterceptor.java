package com.microblocklabs.mpc.interceptor;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

public class GrpcClientRequestInterceptor implements ClientInterceptor {
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            final MethodDescriptor<ReqT, RespT> methodDescriptor,
            final CallOptions callOptions,
            final Channel channel) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
                channel.newCall(methodDescriptor, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
//                var userToken = UserContext.getUserContext().getUserToken();
////                log.info("Setting userToken in header");
//                headers.put(Metadata.Key.of("JWT", Metadata.ASCII_STRING_MARSHALLER), userToken);
                super.start(responseListener, headers);
            }

            @Override
            public void sendMessage(ReqT message) {
                super.sendMessage(message);
            }
        };
    }
}
