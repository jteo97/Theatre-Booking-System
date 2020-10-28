package se325.assignment01.concert.service.util;

import se325.assignment01.concert.common.dto.ConcertInfoSubscriptionDTO;

import javax.ws.rs.container.AsyncResponse;

/**
 * The subscription class contains information about the subscription
 */
public class Subscription {

    //Has information about the notification threshold
    private ConcertInfoSubscriptionDTO concertInfoSubscriptionDTO;
    private AsyncResponse response; //The response to be replied to when the notification threshold has been met

    public Subscription(ConcertInfoSubscriptionDTO concertInfoSubscriptionDTO, AsyncResponse response) {
        this.concertInfoSubscriptionDTO = concertInfoSubscriptionDTO;
        this.response = response;
    }

    public ConcertInfoSubscriptionDTO getConcertInfoSubscriptionDTO() {
        return concertInfoSubscriptionDTO;
    }

    public AsyncResponse getResponse() {
        return response;
    }
}