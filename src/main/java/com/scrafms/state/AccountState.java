package com.scrafms.state;

public interface AccountState {
    boolean canBook();
    boolean canCancelBooking();
    String getStateName();
}
