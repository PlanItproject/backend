package com.trip.planit.Chat.dto;

import com.trip.planit.Chat.entity.MessageType;
import java.time.LocalDateTime;

public record ChatMessageDTO(String sender, String content, MessageType type, LocalDateTime createdAt) {}
