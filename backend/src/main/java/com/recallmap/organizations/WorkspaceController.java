package com.recallmap.organizations;

import com.recallmap.auth.PrincipalUser;
import com.recallmap.users.UserEntity;
import com.recallmap.users.UserRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class WorkspaceController {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final WorkspaceRepository workspaceRepository;
    private final OrganizationMembershipRepository membershipRepository;

    public WorkspaceController(
            UserRepository userRepository,
            OrganizationRepository organizationRepository,
            WorkspaceRepository workspaceRepository,
            OrganizationMembershipRepository membershipRepository) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.workspaceRepository = workspaceRepository;
        this.membershipRepository = membershipRepository;
    }

    @GetMapping("/organizations/{organizationId}/workspaces")
    @Transactional(readOnly = true)
    public List<WorkspaceSummary> listWorkspaces(
            @AuthenticationPrincipal PrincipalUser principal,
            @PathVariable UUID organizationId) {
        UserEntity user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        boolean isMember = membershipRepository.findByUser(user).stream()
                .anyMatch(m -> m.getOrganization().getId().equals(organizationId));
        if (!isMember) {
            throw new IllegalArgumentException("User is not a member of this organization");
        }
        return workspaceRepository.findByOrganizationId(organizationId).stream()
                .map(workspace -> new WorkspaceSummary(workspace.getId(), workspace.getName(), workspace.getSlug()))
                .toList();
    }

    @PostMapping("/organizations/{organizationId}/workspaces")
    @Transactional
    public ResponseEntity<WorkspaceSummary> createWorkspace(
            @AuthenticationPrincipal PrincipalUser principal,
            @PathVariable UUID organizationId,
            @Valid @RequestBody CreateWorkspaceRequest request) {
        UserEntity user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        OrganizationEntity organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));
        boolean isMember = membershipRepository.findByUser(user).stream()
                .anyMatch(m -> m.getOrganization().getId().equals(organizationId));
        if (!isMember) {
            throw new IllegalArgumentException("User is not a member of this organization");
        }

        WorkspaceEntity workspace = new WorkspaceEntity();
        workspace.setOrganization(organization);
        workspace.setName(request.name());
        workspace.setSlug(toSlug(request.name()));
        workspace.setDemo(false);
        workspaceRepository.save(workspace);

        return ResponseEntity.ok(new WorkspaceSummary(workspace.getId(), workspace.getName(), workspace.getSlug()));
    }

    private String toSlug(String input) {
        String value = input.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "")
                .trim();
        return value.isEmpty() ? "workspace" : value;
    }
}
