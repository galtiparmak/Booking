package com.geko.Booking.Entity.Mongo;

public enum Action {
    LOGIN,
    LOGOUT,
    UNSUCCESSFUL_LOGIN_ATTEMPT,
    BOOKED_LISTING,
    CANCELLED_BOOKING,
    PAYMENT_COMPLETED,
    PAYMENT_PENDING,
    PAYMENT_FAILED,
    PAYMENT_REFUNDED,
    REVIEWED,
    USER_UPDATED,
    SEARCHER_REGISTERED,
    HOMEOWNER_REGISTERED,
    LISTING_CREATED,
    LISTING_DELETED,
    VIEWED_LISTING,
    LIKED_LISTING,
    IMAGE_UPLOADED,
    LISTINGS_UPLOADED
}