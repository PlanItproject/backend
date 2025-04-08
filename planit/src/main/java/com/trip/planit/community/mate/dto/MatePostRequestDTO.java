package com.trip.planit.community.mate.dto;

import com.trip.planit.community.mate.entity.MatePurpose;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
public class MatePostRequestDTO {

    @NotEmpty(message = "제목은 필수입니다.")
    @Size(min = 4, max = 40, message = "제목은 4자 이상 40자 이하로 작성해주세요.")
    private String title;

    @NotEmpty(message = "내용은 필수입니다.")
    private String content;

    @NotNull(message = "여행 목적은 필수입니다.")
    private MatePurpose purpose;

    @NotEmpty(message = "장소 이름은 필수입니다.")
    private String locationName;

    @NotNull(message = "위도는 필수입니다.")
    private Double latitude;

    @NotNull(message = "경도는 필수입니다.")
    private Double longitude;

    @Future(message = "시작 날짜는 미래 날짜여야 합니다.")
    private LocalDate startDate;

    @Future(message = "종료 날짜는 미래 날짜여야 합니다.")
    private LocalDate endDate;
}