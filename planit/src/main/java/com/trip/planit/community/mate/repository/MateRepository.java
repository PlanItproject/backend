package com.trip.planit.community.mate.repository;

import com.trip.planit.community.mate.entity.MatePost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MateRepository extends JpaRepository<MatePost, Long> {
    // 필요한 경우 메소드 쿼리 추가 가능
}
