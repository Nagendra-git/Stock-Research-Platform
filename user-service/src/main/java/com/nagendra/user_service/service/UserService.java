package com.nagendra.user_service.service;

import com.nagendra.user_service.dto.RegisterRequestDto;
import com.nagendra.user_service.dto.RegisterResponseDto;

public interface UserService {
    RegisterResponseDto register( RegisterRequestDto request);
}
