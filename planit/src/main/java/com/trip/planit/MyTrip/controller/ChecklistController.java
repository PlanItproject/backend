package com.trip.planit.MyTrip.controller;

import com.trip.planit.MyTrip.dto.ChecklistResponseDto;
import com.trip.planit.MyTrip.dto.ChecklistItemDto;
import com.trip.planit.MyTrip.service.ChecklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "MyTrip")
@RestController
@RequestMapping("/MyTrip")
@RequiredArgsConstructor
public class ChecklistController {
    private final ChecklistService checklistService;

    @GetMapping("/{country}")
    @Operation(summary = "체크리스트 조회")
    public ChecklistResponseDto getChecklist(@PathVariable String country) {
        return checklistService.getChecklistForCountry(country);
    }

    @PostMapping("/{country}/custom")
    @Operation(summary = "커스텀 체크리스트 내용 저장")
    public ChecklistItemDto addCustomItem(@PathVariable String country,
                                          @RequestParam String description) {
        return checklistService.addCustomItem(country, description);
    }
}
