package com.ps.billingservice.grpc;

import billing.BillingResponse;
import billing.BillingResquest;
import com.google.api.Billing;
import com.ps.billingservice.BillingServiceApplication;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import billing.BillingServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class BillingGrpcService extends BillingServiceGrpc.BillingServiceImplBase {

    public static final Logger log =  LoggerFactory.getLogger(BillingGrpcService.class);

    @Override
    public void createBillingAccount(billing.BillingResquest request, StreamObserver<billing.BillingResponse> responseObserver) {
        log.info("createBillingAccount {}",request.toString());

        BillingResponse resp = BillingResponse.newBuilder()
                .setAccountId("123")
                .setStatus("ACTIVE")
                .build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }
}
