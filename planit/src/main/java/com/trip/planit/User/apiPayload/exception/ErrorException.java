package com.trip.planit.User.apiPayload.exception;

import com.trip.planit.User.apiPayload.code.BaseErrorCode;

public class ErrorException extends GeneralException {

  public ErrorException(BaseErrorCode errorCode){
    super(errorCode);
  }
}
