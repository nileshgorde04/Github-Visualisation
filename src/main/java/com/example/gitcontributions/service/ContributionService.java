package com.example.gitcontributions.service;

import com.example.gitcontributions.dto.ContributionRequest;
import com.example.gitcontributions.dto.ContributionResponse;
import com.example.gitcontributions.model.ContributionStats;
import com.example.gitcontributions.model.GitCommit;
import com.example.gitcontributions.model.GitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContributionService {

    private final GitService gitService;

    /**
     * Generate contribution statistics based on the request.
     *
     * @param request The contribution request.
     * @return The contribution response.
     * @throws IOException if there's an error accessing the file system or executing Git commands
     * @throws GitAPIException if there's an error executing Git commands
     */
    public ContributionResponse generateContributions(ContributionRequest request) throws IOException, GitAPIException {
        // Validate that either rootDirectory or remoteRepoUrl is provided
        if ((request.getRootDirectory() == null || request.getRootDirectory().isEmpty()) && 
            (request.getRemoteRepoUrl() == null || request.getRemoteRepoUrl().isEmpty())) {
            throw new IllegalArgumentException("Either Root Directory or Remote Repository URL must be provided");
        }

        // Get user information
        Map<String, String> user = gitService.getGitUser();
        String userName = user.get("name");
        String userEmail = request.getUserEmail() != null ? request.getUserEmail() : user.get("email");

        log.info("Analyzing contributions for: {} <{}>", userName, userEmail);

        List<GitRepository> repositories = new ArrayList<>();

        // Check if a remote repository URL is provided
        if (request.getRemoteRepoUrl() != null && !request.getRemoteRepoUrl().isEmpty()) {
            log.info("Analyzing remote repository: {}", request.getRemoteRepoUrl());

            // Clone and analyze the remote repository
            GitRepository remoteRepo = gitService.cloneAndAnalyzeRemoteRepository(
                    request.getRemoteRepoUrl(), 
                    request.getDays(), 
                    userEmail);

            repositories.add(remoteRepo);
        } else {
            // Validate root directory
            String rootDir = request.getRootDirectory();
            Path rootPath = Paths.get(rootDir);
            if (!Files.exists(rootPath)) {
                log.error("Root directory does not exist: {}", rootDir);
                throw new NoSuchFileException(rootDir);
            }

            // Find Git repositories in the local file system
            repositories = gitService.findGitRepositories(request.getRootDirectory());
        }

        if (repositories.isEmpty()) {
            log.warn("No Git repositories found");
            return ContributionResponse.builder()
                    .userName(userName)
                    .userEmail(userEmail)
                    .totalRepositories(0)
                    .totalCommits(0)
                    .commitsByDate(new HashMap<>())
                    .repositories(new ArrayList<>())
                    .build();
        }

        log.info("Found {} Git repositories", repositories.size());

        // Collect all commits across repositories
        List<GitCommit> allCommits = new ArrayList<>();
        for (GitRepository repository : repositories) {
            // Skip repositories that already have commits (like the remote repository)
            if (repository.getCommits() == null || repository.getCommits().isEmpty()) {
                GitRepository repoWithCommits = gitService.getCommits(repository, request.getDays(), userEmail);
                allCommits.addAll(repoWithCommits.getCommits());
            } else {
                allCommits.addAll(repository.getCommits());
            }
        }

        // Calculate overall stats
        ContributionStats stats = gitService.calculateStats(allCommits);

        // Create response
        return ContributionResponse.builder()
                .userName(userName)
                .userEmail(userEmail)
                .totalRepositories(repositories.size())
                .totalCommits(stats.getTotalCommits())
                .commitsByDate(stats.getCommitsByDate())
                .repositories(repositories)
                .overallStats(stats)
                .build();
    }
}
