package com.example.gitcontributions.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitRepository {
    private String name;
    private Path path;
    private List<GitCommit> commits;
}