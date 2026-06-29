package com.scrafms.state;

public class RestrictedState implements AccountState {

    @Override
    public boolean canBook() {
        return false;
    }

    @Override
    public boolean canCancelBooking() {
        return true;
    }

    @Override
    public String getStateName() {
        return "RESTRICTED";
    }
}
