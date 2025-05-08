package com.example.gitcontributions.service;

import com.example.gitcontributions.model.ContributionStats;
import com.example.gitcontributions.model.GitCommit;
import com.example.gitcontributions.model.GitRepository;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class GitService {

    /**
     * Find all Git repositories under the given root directory.
     *
     * @param rootDir The root directory to start the search from.
     * @return A list of Git repositories.
     * @throws IOException if there's an error accessing the file system
     */
    public List<GitRepository> findGitRepositories(String rootDir) throws IOException {
        List<GitRepository> repositories = new ArrayList<>();
        Path rootPath = Paths.get(rootDir);

        if (!Files.exists(rootPath)) {
            log.error("Root directory does not exist: {}", rootDir);
            throw new NoSuchFileException(rootDir);
        }

        try (Stream<Path> paths = Files.walk(rootPath)) {
            List<Path> gitDirs = paths
                    .filter(Files::isDirectory)
                    .filter(path -> path.getFileName().toString().equals(".git"))
                    .collect(Collectors.toList());

            for (Path gitDir : gitDirs) {
                Path repoPath = gitDir.getParent();
                String repoName = repoPath.getFileName().toString();

                repositories.add(GitRepository.builder()
                        .name(repoName)
                        .path(repoPath)
                        .commits(new ArrayList<>())
                        .build());
            }
        } catch (IOException e) {
            log.error("Error walking directory: {}", rootDir, e);
            throw new IOException("Error searching for Git repositories: " + e.getMessage(), e);
        }

        return repositories;
    }

    /**
     * Get commits for a specific repository within a date range by a specific author.
     *
     * @param repository The Git repository.
     * @param days Number of days to look back.
     * @param authorEmail Email of the author to filter commits by.
     * @return The repository with commits added.
     * @throws IOException if there's an error accessing the repository
     * @throws GitAPIException if there's an error executing Git commands
     */
    public GitRepository getCommits(GitRepository repository, int days, String authorEmail) throws IOException, GitAPIException {
        List<GitCommit> commits = new ArrayList<>();
        Path gitDir = repository.getPath().resolve(".git");

        if (!Files.exists(gitDir)) {
            log.error("Git directory does not exist: {}", gitDir);
            throw new NoSuchFileException(gitDir.toString());
        }

        try {
            Repository jgitRepo = new FileRepositoryBuilder()
                    .setGitDir(gitDir.toFile())
                    .build();

            try (Git git = new Git(jgitRepo)) {
                LocalDateTime since = LocalDateTime.now().minusDays(days);
                Date sinceDate = Date.from(since.atZone(ZoneId.systemDefault()).toInstant());

                Iterable<RevCommit> revCommits = git.log().all().call();

                for (RevCommit revCommit : revCommits) {
                    PersonIdent authorIdent = revCommit.getAuthorIdent();
                    Date commitDate = authorIdent.getWhen();

                    // Skip commits older than 'since' date
                    if (commitDate.before(sinceDate)) {
                        continue;
                    }

                    // Skip commits not by the specified author
                    if (authorEmail != null && !authorEmail.isEmpty() && 
                            !authorEmail.equals(authorIdent.getEmailAddress())) {
                        continue;
                    }

                    LocalDateTime commitDateTime = LocalDateTime.ofInstant(
                            commitDate.toInstant(), ZoneId.systemDefault());

                    GitCommit commit = GitCommit.builder()
                            .hash(revCommit.getName())
                            .authorName(authorIdent.getName())
                            .authorEmail(authorIdent.getEmailAddress())
                            .date(commitDateTime)
                            .message(revCommit.getShortMessage())
                            .repository(repository.getName())
                            .build();

                    commits.add(commit);
                }
            }
        } catch (IOException e) {
            log.error("Error accessing repository: {}", repository.getPath(), e);
            throw new IOException("Error accessing Git repository: " + e.getMessage(), e);
        } catch (GitAPIException e) {
            log.error("Error executing Git command for repository: {}", repository.getPath(), e);
            throw new GitAPIException("Error executing Git command: " + e.getMessage(), e);
        }

        repository.setCommits(commits);
        return repository;
    }

    /**
     * Calculate statistics from a list of commits.
     *
     * @param commits A list of commit dictionaries.
     * @return Statistics about the commits.
     */
    public ContributionStats calculateStats(List<GitCommit> commits) {
        if (commits.isEmpty()) {
            return ContributionStats.builder()
                    .totalCommits(0)
                    .commitsByDate(new HashMap<>())
                    .build();
        }

        // Initialize stats
        Map<LocalDate, Integer> commitsByDate = new HashMap<>();

        // Process each commit
        for (GitCommit commit : commits) {
            LocalDate date = commit.getDate().toLocalDate();

            // Count commits by date
            commitsByDate.put(date, commitsByDate.getOrDefault(date, 0) + 1);
        }

        // Sort commits by date to find first and last
        List<GitCommit> sortedCommits = commits.stream()
                .sorted(Comparator.comparing(GitCommit::getDate))
                .collect(Collectors.toList());

        return ContributionStats.builder()
                .totalCommits(commits.size())
                .commitsByDate(commitsByDate)
                .firstCommit(sortedCommits.get(0))
                .lastCommit(sortedCommits.get(sortedCommits.size() - 1))
                .build();
    }

    /**
     * Get the current Git user's name and email.
     *
     * @return A map containing the user's name and email.
     * @throws IOException if there's an error executing Git commands
     */
    public Map<String, String> getGitUser() throws IOException {
        Map<String, String> user = new HashMap<>();
        user.put("name", "Unknown");
        user.put("email", "Unknown");

        try {
            ProcessBuilder pb = new ProcessBuilder("git", "config", "user.name");
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                try (Scanner scanner = new Scanner(process.getErrorStream())) {
                    StringBuilder errorMsg = new StringBuilder();
                    while (scanner.hasNextLine()) {
                        errorMsg.append(scanner.nextLine());
                    }
                    log.error("Git command failed with exit code {}: {}", exitCode, errorMsg);
                    throw new IOException("Failed to execute Git command. Make sure Git is installed and in your PATH.");
                }
            }

            try (Scanner scanner = new Scanner(process.getInputStream())) {
                if (scanner.hasNextLine()) {
                    user.put("name", scanner.nextLine().trim());
                }
            }

            pb = new ProcessBuilder("git", "config", "user.email");
            process = pb.start();
            exitCode = process.waitFor();

            if (exitCode != 0) {
                try (Scanner scanner = new Scanner(process.getErrorStream())) {
                    StringBuilder errorMsg = new StringBuilder();
                    while (scanner.hasNextLine()) {
                        errorMsg.append(scanner.nextLine());
                    }
                    log.error("Git command failed with exit code {}: {}", exitCode, errorMsg);
                    throw new IOException("Failed to execute Git command. Make sure Git is installed and in your PATH.");
                }
            }

            try (Scanner scanner = new Scanner(process.getInputStream())) {
                if (scanner.hasNextLine()) {
                    user.put("email", scanner.nextLine().trim());
                }
            }
        } catch (IOException e) {
            log.error("Error getting Git user", e);
            throw new IOException("Failed to execute Git command. Make sure Git is installed and in your PATH.", e);
        } catch (InterruptedException e) {
            log.error("Git command was interrupted", e);
            Thread.currentThread().interrupt();
            throw new IOException("Git command was interrupted", e);
        }

        return user;
    }
}
