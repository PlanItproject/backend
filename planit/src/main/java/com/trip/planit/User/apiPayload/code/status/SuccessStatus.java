package com.trip.planit.User.apiPayload.code.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import com.trip.planit.User.apiPayload.code.BaseCode;
import com.trip.planit.User.apiPayload.code.ReasonDTO;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseCode {

  _OK(HttpStatus.OK, "성공입니다.");

  private final HttpStatus httpStatus;
  private final String message;

  @Override
  public ReasonDTO getReason() {
    return ReasonDTO.builder()
        .status(httpStatus.value())
        .isSuccess(true)
        .message(message)
        .httpStatus(httpStatus)
        .build();
  }

  @Override
  public ReasonDTO getReasonHttpStatus() {
    return ReasonDTO.builder()
        .status(httpStatus.value())
        .isSuccess(true)
        .message(message)
        .httpStatus(httpStatus)
        .build();
  }
}