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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class OrganizationController {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public OrganizationController(
            OrganizationRepository organizationRepository,
            OrganizationMembershipRepository membershipRepository,
            UserRepository userRepository,
            RoleRepository roleRepository) {
        this.organizationRepository = organizationRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @GetMapping("/organizations")
    @Transactional(readOnly = true)
    public List<OrganizationSummary> listOrganizations(@AuthenticationPrincipal PrincipalUser principal) {
        UserEntity user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return membershipRepository.findByUser(user).stream()
                .map(membership -> {
                    OrganizationEntity org = membership.getOrganization();
                    return new OrganizationSummary(org.getId(), org.getName(), org.getSlug());
                })
                .toList();
    }

    @PostMapping("/organizations")
    @Transactional
    public ResponseEntity<OrganizationSummary> createOrganization(
            @AuthenticationPrincipal PrincipalUser principal,
            @Valid @RequestBody CreateOrganizationRequest request) {
        UserEntity user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        String slug = toSlug(request.name());

        OrganizationEntity organization = new OrganizationEntity();
        organization.setName(request.name());
        organization.setSlug(slug);
        organization.setDemo(false);
        organizationRepository.save(organization);

        RoleEntity ownerRole = roleRepository.findByRoleKey("OWNER")
                .orElseThrow(() -> new IllegalStateException("OWNER role missing"));

        OrganizationMembershipEntity membership = new OrganizationMembershipEntity();
        membership.setOrganization(organization);
        membership.setUser(user);
        membership.setRole(ownerRole);
        membershipRepository.save(membership);

        return ResponseEntity.ok(new OrganizationSummary(organization.getId(), organization.getName(), organization.getSlug()));
    }

    private String toSlug(String input) {
        String value = input.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "")
                .trim();
        return value.isEmpty() ? "organization" : value;
    }
}
