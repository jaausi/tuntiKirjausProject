package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.dao.ConfigurationDao;
import com.sirvja.tuntikirjaus.dao.Dao;
import com.sirvja.tuntikirjaus.domain.Configuration;
import com.sirvja.tuntikirjaus.exporter.impl.KiekuConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigurationService {

    private final Dao<Configuration, String> configurationDao;

    public ConfigurationService() {
        this.configurationDao = new ConfigurationDao();
    }

    public KiekuConfiguration getKiekuConfiguration() {
        Map<String, String> confMap = configurationDao.getAllToList()
                .stream()
                .collect(Collectors.toMap(Configuration::getKey, Configuration::getValue));

        return KiekuConfiguration.mapToConfiguration(confMap);
    }

    public void saveKiekuConfiguration(KiekuConfiguration kiekuConfiguration) {
        Map<String, String> confMap = KiekuConfiguration.toMap(kiekuConfiguration);
        confMap.entrySet()
                .stream()
                .map(entry -> new Configuration(entry.getKey(), entry.getValue()))
                .forEach(this::insertOrUpdate);
    }

    public Optional<Configuration> getConfiguration(String key) {
        return configurationDao.get(key);
    }

    public void saveConfiguration(Configuration configuration) {
        configurationDao.save(configuration);
    }

    public void insertOrUpdate(Configuration configuration) {
        configurationDao.get(configuration.getKey()).ifPresentOrElse(
                _ -> configurationDao.update(configuration),
                () -> configurationDao.save(configuration)
        );
    }
    public void update(Configuration configuration) {
        configurationDao.update(configuration);
    }

    private static final String BUDGET_KEY_PREFIX = "projectBudget.";
    private static final String PROJECT_CODE_KEY_PREFIX = "projectCode.";
    private static final String FOLLOW_BUDGET_KEY_PREFIX = "projectFollowBudget.";

    private static String budgetKey(String project) {
        return BUDGET_KEY_PREFIX + project;
    }

    public OptionalLong getProjectBudgetMinutes(String project) {
        return configurationDao.get(budgetKey(project))
                .map(c -> {
                    try {
                        double hours = Double.parseDouble(c.getValue());
                        return OptionalLong.of(Math.round(hours * 60));
                    } catch (NumberFormatException e) {
                        return OptionalLong.empty();
                    }
                })
                .orElse(OptionalLong.empty());
    }

    public void saveProjectBudget(String project, double hours) {
        Configuration conf = new Configuration(budgetKey(project), String.valueOf(hours));
        insertOrUpdate(conf);
    }

    public void removeProjectBudget(String project) {
        configurationDao.delete(new Configuration(budgetKey(project), ""));
    }

    public List<String> getProjectsWithBudget() {
        return configurationDao.getAllToList().stream()
                .map(Configuration::getKey)
                .filter(k -> k.startsWith(BUDGET_KEY_PREFIX))
                .map(k -> k.substring(BUDGET_KEY_PREFIX.length()))
                .toList();
    }

    private static String projectCodeKey(String project) {
        return PROJECT_CODE_KEY_PREFIX + project;
    }

    public String getProjectCode(String project) {
        return configurationDao.get(projectCodeKey(project))
                .map(Configuration::getValue)
                .orElse("");
    }

    /** Saves the project code of the project, a blank code removes it. */
    public void saveProjectCode(String project, String projectCode) {
        if (projectCode == null || projectCode.isBlank()) {
            configurationDao.delete(new Configuration(projectCodeKey(project), ""));
        } else {
            insertOrUpdate(new Configuration(projectCodeKey(project), projectCode.trim()));
        }
    }

    private static String followBudgetKey(String project) {
        return FOLLOW_BUDGET_KEY_PREFIX + project;
    }

    /**
     * Whether the budget of the project is followed on the main page. Projects that have
     * not been set explicitly are followed when they have a budget.
     */
    public boolean isProjectBudgetFollowed(String project) {
        return isFollowed(
                configurationDao.get(followBudgetKey(project)).map(Configuration::getValue),
                getProjectBudgetMinutes(project).isPresent()
        );
    }

    static boolean isFollowed(Optional<String> storedValue, boolean hasBudget) {
        return storedValue
                .filter(value -> !value.isBlank())
                .map(Boolean::parseBoolean)
                .orElse(hasBudget);
    }

    public void saveProjectBudgetFollowed(String project, boolean followed) {
        insertOrUpdate(new Configuration(followBudgetKey(project), String.valueOf(followed)));
    }

    /** Projects that have a budget, a project code or a follow budget setting saved. */
    public List<String> getProjectsWithBudgetSettings() {
        return configurationDao.getAllToList().stream()
                .map(Configuration::getKey)
                .map(ConfigurationService::projectOfBudgetSettingKey)
                .flatMap(Optional::stream)
                .distinct()
                .toList();
    }

    static Optional<String> projectOfBudgetSettingKey(String key) {
        return Stream.of(BUDGET_KEY_PREFIX, PROJECT_CODE_KEY_PREFIX, FOLLOW_BUDGET_KEY_PREFIX)
                .filter(key::startsWith)
                .map(prefix -> key.substring(prefix.length()))
                .findFirst();
    }
}
