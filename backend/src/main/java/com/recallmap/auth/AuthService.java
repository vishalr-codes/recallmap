package com.recallmap.auth;

import com.recallmap.auth.dto.AuthResponse;
import com.recallmap.auth.dto.LoginRequest;
import com.recallmap.auth.dto.RegisterRequest;
import com.recallmap.organizations.OrganizationEntity;
import com.recallmap.organizations.OrganizationMembershipEntity;
import com.recallmap.organizations.OrganizationMembershipRepository;
import com.recallmap.organizations.OrganizationRepository;
import com.recallmap.organizations.RoleEntity;
import com.recallmap.organizations.RoleRepository;
import com.recallmap.organizations.WorkspaceEntity;
import com.recallmap.organizations.WorkspaceRepository;
import com.recallmap.users.UserEntity;
import com.recallmap.users.UserRepository;
import jakarta.transaction.Transactional;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMembershipRepository membershipRepository;
    private final WorkspaceRepository workspaceRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            OrganizationRepository organizationRepository,
            OrganizationMembershipRepository membershipRepository,
            WorkspaceRepository workspaceRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.workspaceRepository = workspaceRepository;
        this.roleRepository = roleRepository;
        this.membershipRepository = membershipRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim();
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        if (request.password() == null || request.password().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters");
        }

        UserEntity user = new UserEntity();
        user.setEmail(email.toLowerCase(Locale.ROOT));
        user.setDisplayName(request.displayName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEnabled(true);
        userRepository.save(user);

        String orgName = request.organizationName() == null || request.organizationName().isBlank()
                ? request.displayName().trim() + " Studio"
                : request.organizationName().trim();
        String slug = toSlug(orgName);
        Optional<OrganizationEntity> existingOrg = organizationRepository.findBySlug(slug);
        OrganizationEntity organization = existingOrg.orElseGet(() -> {
            OrganizationEntity org = new OrganizationEntity();
            org.setName(orgName);
            org.setSlug(slug);
            org.setDemo(false);
            return organizationRepository.save(org);
        });

        RoleEntity ownerRole = roleRepository.findByRoleKey("OWNER")
                .orElseThrow(() -> new IllegalStateException("OWNER role missing"));

        OrganizationMembershipEntity membership = new OrganizationMembershipEntity();
        membership.setOrganization(organization);
        membership.setUser(user);
        membership.setRole(ownerRole);
        membershipRepository.save(membership);

        WorkspaceEntity workspace = new WorkspaceEntity();
        workspace.setOrganization(organization);
        workspace.setName("Primary Workspace");
        workspace.setSlug("primary-workspace");
        workspace.setDemo(false);
        workspaceRepository.save(workspace);

        String token = jwtService.generate(user);
        return new AuthResponse(token, "Bearer", user.getEmail(), user.getDisplayName(), organization.getId(), workspace.getId());
    }

    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
            if (!authentication.isAuthenticated()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
            }
            UserEntity user = userRepository.findByEmailIgnoreCase(request.email())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
            String token = jwtService.generate(user);
            return new AuthResponse(token, "Bearer", user.getEmail(), user.getDisplayName(), null, null);
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
    }

    private String toSlug(String input) {
        String value = input.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "")
                .trim();
        return value.isEmpty() ? "organization" : value;
    }
}
