package com.ps.patiantservice.Grpc;

import billing.BillingResponse;
import billing.BillingResquest;
import billing.BillingServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BillingServiceGrpcClient {

    private final BillingServiceGrpc.BillingServiceBlockingStub stub;

    public BillingServiceGrpcClient(
            @Value("${billing.service.address:localhost}") String serverAddress,
            @Value("${billing.service.grpc.port:9001}") int serverPort){

        log.info("Server Address:{} Port:{}", serverAddress, serverPort);
        ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress, serverPort).usePlaintext().build();
        stub = BillingServiceGrpc.newBlockingStub(channel);
    }

    public BillingResponse createBillingAccount(String patientId, String name, String email){
        BillingResquest resquest = BillingResquest.newBuilder().setPatientId(patientId)
                .setName(name).setEmail(email).build();
        BillingResponse resp = stub.createBillingAccount(resquest);
        log.info("Create billing account successful"+resp.getAccountId());
        return resp;
    }

}

