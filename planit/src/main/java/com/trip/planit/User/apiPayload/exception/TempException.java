package com.trip.planit.User.apiPayload.exception;


import com.trip.planit.User.apiPayload.code.BaseErrorCode;

public class TempException extends GeneralException {

  public TempException(BaseErrorCode errorCode) {
    super(errorCode);
  }
}
