package com.sirvja.tuntikirjaus.service;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurationServiceTest {

    @Test
    void projectWithoutFollowSettingIsFollowedWhenItHasBudget() {
        assertTrue(ConfigurationService.isFollowed(Optional.empty(), true));
        assertFalse(ConfigurationService.isFollowed(Optional.empty(), false));
        assertTrue(ConfigurationService.isFollowed(Optional.of(""), true));
    }

    @Test
    void explicitFollowSettingOverridesBudget() {
        assertFalse(ConfigurationService.isFollowed(Optional.of("false"), true));
        assertTrue(ConfigurationService.isFollowed(Optional.of("true"), false));
    }

    @Test
    void projectIsParsedFromBudgetSettingKeys() {
        assertEquals(Optional.of("Project A"), ConfigurationService.projectOfBudgetSettingKey("projectBudget.Project A"));
        assertEquals(Optional.of("Project B"), ConfigurationService.projectOfBudgetSettingKey("projectCode.Project B"));
        assertEquals(Optional.of("Project C"), ConfigurationService.projectOfBudgetSettingKey("projectFollowBudget.Project C"));
        assertEquals(Optional.empty(), ConfigurationService.projectOfBudgetSettingKey("kieku.url"));
    }
}
