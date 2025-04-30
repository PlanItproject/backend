package com.trip.planit.recommend.repository;

import com.trip.planit.User.entity.User;
import com.trip.planit.recommend.entity.DestinationRecommendationResult;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DestinationRecommendationResultRepository extends JpaRepository<DestinationRecommendationResult, Long> {

    List<DestinationRecommendationResult> findByUser(User user);

    @Query("SELECT r.recommendedCity AS city, COUNT(r) AS count " +
            "FROM DestinationRecommendationResult r " +
            "GROUP BY r.recommendedCity " +
            "ORDER BY count DESC")
    List<CityCountProjection> getCityRecommendationStatistics();

    interface CityCountProjection {
        String getCity();
        Long getCount();
    }
}