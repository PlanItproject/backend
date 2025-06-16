package com.trip.planit.User.dto;

import com.trip.planit.User.entity.DeleteReason;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteReqeust {
    private DeleteReason deleteReason;
    private String deleteReason_Description;
}
