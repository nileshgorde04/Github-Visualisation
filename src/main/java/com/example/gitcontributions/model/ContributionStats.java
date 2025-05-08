package com.example.gitcontributions.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContributionStats {
    private int totalCommits;
    private Map<LocalDate, Integer> commitsByDate;
    private GitCommit firstCommit;
    private GitCommit lastCommit;
}