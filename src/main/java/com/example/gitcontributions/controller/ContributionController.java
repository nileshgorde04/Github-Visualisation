package com.example.gitcontributions.controller;

import com.example.gitcontributions.dto.ContributionRequest;
import com.example.gitcontributions.dto.ContributionResponse;
import com.example.gitcontributions.service.ContributionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping("/api/contributions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ContributionController {

    private final ContributionService contributionService;

    @PostMapping
    public ResponseEntity<ContributionResponse> getContributions(@Valid @RequestBody ContributionRequest request) throws IOException, GitAPIException {
        log.info("Received contribution request: {}", request);
        ContributionResponse response = contributionService.generateContributions(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Git Contributions API is running");
    }
}
