package com.example.gitcontributions.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContributionRequest {
    @NotBlank(message = "Root directory is required")
    private String rootDirectory;
    
    @Min(value = 1, message = "Days must be at least 1")
    private int days = 30;
    
    private String userEmail;
}