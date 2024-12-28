package com.trip.planit.travelplanner.dto;

import java.io.Serializable;

public class SelectedPlaceDTO implements Serializable {  // Serializable 추가
    private static final long serialVersionUID = 1L;    // 직렬화 버전 ID 추가

    private Long id;
    private String placeName;
    private String placeId;
    private String address;
    private Double latitude; // 위도
    private Double longitude; // 경도

    // 기본 생성자
    public SelectedPlaceDTO() {}

    // 모든 필드를 포함한 생성자
    public SelectedPlaceDTO(Long id, String placeName, String placeId, String address, Double latitude, Double longitude) {
        this.id = id;
        this.placeName = placeName;
        this.placeId = placeId;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getter/Setter 추가
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPlaceName() { return placeName; }
    public void setPlaceName(String placeName) { this.placeName = placeName; }

    public String getPlaceId() { return placeId; }
    public void setPlaceId(String placeId) { this.placeId = placeId; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
