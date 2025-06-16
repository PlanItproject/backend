package com.trip.planit.User.apiPayload.code;

public interface BaseErrorCode {

  ErrorReasonDTO getReason();

  ErrorReasonDTO getReasonHttpStatus();
}
