package com.nagendra.platform.models;


import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Audit {

    private String createdBy;

    private String updatedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private boolean isActive;
}
