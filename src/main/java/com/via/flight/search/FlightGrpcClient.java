package com.via.flight.search;


import com.via.flight.services.FlightSearchRequest;
import com.via.flight.services.FlightSearchResponse;
import com.via.flight.services.FlightSearchServiceGrpc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

/**
 * @author Vivek Wiki
 */
public class FlightGrpcClient {
    private static final Logger logger = Logger.getLogger(FlightGrpcClient.class.getName());

    FlightSearchServiceGrpc.FlightSearchServiceBlockingStub blockingStub;
    FlightSearchServiceGrpc.FlightSearchServiceStub asyncStub;
    ManagedChannel channel;

    public FlightGrpcClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                //set true: avoid SSL/TLS
                .usePlaintext(true)
                .build();

        blockingStub = FlightSearchServiceGrpc.newBlockingStub(channel);
        asyncStub = FlightSearchServiceGrpc.newStub(channel);
    }

    public static void main(String[] args) throws InterruptedException {
        FlightGrpcClient client = new FlightGrpcClient("localhost", 8080);
        try {
            FlightSearchRequest request = com.via.flight.services.FlightSearchRequest.getDefaultInstance();
            client.search(request);
            client.getSearchUsingAsyncStub();
        } finally {
            client.shutdown();
        }
    }

    private void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    private void search(FlightSearchRequest request) {

        FlightSearchResponse response = blockingStub.search(request);

        logger.info(String.valueOf(response.getIsDomestic()));
    }

    /**
     * for one-directional streaming
     */
    public void getSearchUsingAsyncStub() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        // Build Search-Request
        FlightSearchRequest request = FlightSearchRequest.getDefaultInstance();

        StreamObserver<FlightSearchResponse> responseObserver = new StreamObserver<FlightSearchResponse>() {
            @Override
            public void onNext(FlightSearchResponse value) {
            }

            @Override
            public void onError(Throwable t) {
                Status status = Status.fromThrowable(t);
                logger.info("failed with status : " + status);
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                logger.info("finished!");
                latch.countDown();
            }
        };
        asyncStub.streamSearch(responseObserver);
        latch.await();
    }
}
