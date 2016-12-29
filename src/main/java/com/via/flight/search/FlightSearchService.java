package com.via.flight.search;

import com.via.flight.services.FlightSearchRequest;
import com.via.flight.services.FlightSearchResponse;
import com.via.flight.services.FlightSearchServiceGrpc;

import java.util.LinkedHashSet;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

/**
 * @author Vivek Wiki
 */
public class FlightSearchService extends FlightSearchServiceGrpc.FlightSearchServiceImplBase {
    private static LinkedHashSet<StreamObserver<FlightSearchResponse>> observers = new LinkedHashSet<>();

    /**
     * it is async by default
     */
    @Override
    public void search(FlightSearchRequest request, StreamObserver<FlightSearchResponse> responseObserver) {
        //Get Flight search response
        try {
            FlightSearchResponse response = FlightSearchResponse.getDefaultInstance();

            if (response.getIsDomestic()) {
                /**
                 * send value back to the client on success
                 */
                responseObserver.onNext(response);
            } else {
                responseObserver.onError(
                        Status.INTERNAL.withDescription("the flight is international").asException());
            }
            /**
             * sends value back to the client on error
             */
        } catch (Exception e) {
            responseObserver.onError(e);
        }
        /**
         * must be called, when done with the call
         *
         * if not called, the stream of this client will be open and it'll never return the response
         */
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<FlightSearchRequest> streamSearch(StreamObserver<FlightSearchResponse> responseObserver) {
        //registering observers
        observers.add(responseObserver);
        return new StreamObserver<FlightSearchRequest>() {
            @Override
            public void onNext(FlightSearchRequest value) {
                FlightSearchResponse response = FlightSearchResponse.getDefaultInstance();

                //Making sure responses are propagated  to all observers
                for (StreamObserver<FlightSearchResponse> observer : observers) {
                    observer.onNext(response);
                }
            }

            //making sure observers are unregistered in error and in completion
            @Override
            public void onError(Throwable t) {
                observers.remove(responseObserver);
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                observers.remove(responseObserver);
                responseObserver.onCompleted();
            }
        };
    }
}
