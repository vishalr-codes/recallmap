package com.recallmap.configuration;

import com.recallmap.organizations.RoleEntity;
import com.recallmap.organizations.RoleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class RoleDataSeeder implements ApplicationRunner {

    private final RoleRepository roleRepository;

    public RoleDataSeeder(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureRole("OWNER", "Owner", "Full organization control");
        ensureRole("ADMIN", "Administrator", "Manage workspaces and deletion workflows");
        ensureRole("DEVELOPER", "Developer", "Inspect lineage and operational workflows");
        ensureRole("VIEWER", "Viewer", "Read-only access to data and reports");
    }

    private void ensureRole(String key, String name, String description) {
        roleRepository.findByRoleKey(key).orElseGet(() -> {
            RoleEntity role = new RoleEntity();
            role.setRoleKey(key);
            role.setName(name);
            role.setDescription(description);
            role.setSystem(true);
            return roleRepository.save(role);
        });
    }
}
