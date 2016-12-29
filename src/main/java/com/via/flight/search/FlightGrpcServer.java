package com.via.flight.search;

import java.io.IOException;

import io.grpc.Server;
import io.grpc.ServerBuilder;

/**
 * @author Vivek Wiki
 */
public class FlightGrpcServer {
    public static void main(String[] args) throws InterruptedException, IOException {
        Server server = ServerBuilder.forPort(8080)
                .addService(new FlightSearchService().bindService())
                .build();
        server.start();

        server.awaitTermination();
    }
}
