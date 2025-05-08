package com.example.gitcontributions.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitCommit {
    private String hash;
    private String authorName;
    private String authorEmail;
    private LocalDateTime date;
    private String message;
    private String repository;
}