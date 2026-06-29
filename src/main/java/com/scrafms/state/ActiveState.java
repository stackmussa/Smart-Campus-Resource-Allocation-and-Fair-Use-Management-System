package com.scrafms.state;

public class ActiveState implements AccountState {

    @Override
    public boolean canBook() {
        return true;
    }

    @Override
    public boolean canCancelBooking() {
        return true;
    }

    @Override
    public String getStateName() {
        return "ACTIVE";
    }
}
