package com.nagendra.user_service.dto;

public record RegisterResponseDto(
    String id,
    String username,
    String email,
    String message
) {
}