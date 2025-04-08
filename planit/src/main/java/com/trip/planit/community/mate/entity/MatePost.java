package com.trip.planit.community.mate.entity;

import com.trip.planit.User.entity.User; // 작성자 User 관련 엔티티 사용
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;


@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class MatePost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title; // 작성글 제목
    private String content; // 작성글 내용

    @Enumerated(EnumType.STRING)
    private MatePurpose purpose; // 작성 목적

    private String locationName; // 장소 이름
    private Double latitude;      // 위도
    private Double longitude;     // 경도

    private LocalDate startDate;  // 여행 시작일
    private LocalDate endDate;    // 여행 종료일

    @Enumerated(EnumType.STRING)
    private RecruitmentStatus status; // 모집 상태 (OPEN, CLOSED)

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author; // 작성자 정보

}
