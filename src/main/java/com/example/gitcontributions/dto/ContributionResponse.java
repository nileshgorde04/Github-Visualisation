package com.example.gitcontributions.dto;

import com.example.gitcontributions.model.ContributionStats;
import com.example.gitcontributions.model.GitRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContributionResponse {
    private String userName;
    private String userEmail;
    private int totalRepositories;
    private int totalCommits;
    private Map<LocalDate, Integer> commitsByDate;
    private List<GitRepository> repositories;
    private ContributionStats overallStats;
}