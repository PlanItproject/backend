package com.trip.planit.MyTrip.service;

import com.trip.planit.MyTrip.dto.ChecklistItemDto;
import com.trip.planit.MyTrip.dto.ChecklistResponseDto;
import com.trip.planit.MyTrip.dto.ChecklistItemType;
import com.trip.planit.MyTrip.entity.ChecklistEntity;
import com.trip.planit.MyTrip.repository.ChecklistItemRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChecklistService {
    private final ChecklistItemRepository checklistItemRepository;

    public ChecklistResponseDto getChecklistForCountry(String country) {
        ChecklistResponseDto checklistResponseDto = new ChecklistResponseDto();
        checklistResponseDto.setRequired(mapList(checklistItemRepository.findByCountryAndType(country, ChecklistItemType.REQUIRED)));
        checklistResponseDto.setRecommend(mapList(checklistItemRepository.findByCountryAndType(country, ChecklistItemType.RECOMMENDED)));
        checklistResponseDto.setCustom(mapList(checklistItemRepository.findByCountryAndType(country, ChecklistItemType.CUSTOM)));
        return checklistResponseDto;
    }

    public ChecklistItemDto addCustomItem(String country, String description) {
        ChecklistEntity checklistEntity = new ChecklistEntity();
        checklistEntity.setCountry(country);
        checklistEntity.setType(ChecklistItemType.CUSTOM);
        checklistEntity.setDescription(description);
        ChecklistEntity saved = checklistItemRepository.save(checklistEntity);
        return map(saved);
    }

    private List<ChecklistItemDto> mapList(List<ChecklistEntity> checklistEntity) {
        return checklistEntity.stream().map(this::map).collect(Collectors.toList());
    }

    private ChecklistItemDto map(ChecklistEntity checklistEntity) {
        ChecklistItemDto checklistItemDto = new ChecklistItemDto();
        checklistItemDto.setId(checklistEntity.getId());
        checklistItemDto.setDescription(checklistEntity.getDescription());
        checklistItemDto.setType(checklistEntity.getType().name());
        checklistItemDto.setChecked(checklistEntity.isChecked());
        return checklistItemDto;
    }
}
