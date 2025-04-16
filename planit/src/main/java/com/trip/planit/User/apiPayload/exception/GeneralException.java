package com.trip.planit.User.apiPayload.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import com.trip.planit.User.apiPayload.code.BaseErrorCode;
import com.trip.planit.User.apiPayload.code.ErrorReasonDTO;

@Getter
@AllArgsConstructor
public class GeneralException extends RuntimeException {

  private final BaseErrorCode code;

  public ErrorReasonDTO getErrorReason() {
    return this.code.getReason();
  }

  public ErrorReasonDTO getErrorReasonHttpStatus() {
    return this.code.getReasonHttpStatus();
  }
}
