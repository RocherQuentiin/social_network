package com.socialnetwork.socialnetwork.business.interfaces.service;

import com.socialnetwork.socialnetwork.entity.Event;
import com.socialnetwork.socialnetwork.entity.User;

public interface IEventWalletService {

    void refundAllSuccessfulPaymentsForEvent(Event event);

    void refundSuccessfulPaymentsForLeavingAttendee(Event event, User leaver);
}
