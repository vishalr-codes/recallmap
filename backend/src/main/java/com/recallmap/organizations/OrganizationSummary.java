package com.recallmap.organizations;

import java.util.UUID;

public record OrganizationSummary(UUID id, String name, String slug) {
}
