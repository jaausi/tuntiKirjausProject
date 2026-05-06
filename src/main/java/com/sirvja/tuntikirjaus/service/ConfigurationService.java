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
                .forEach(configurationDao::save);
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

    private static String budgetKey(String project) {
        return "projectBudget." + project;
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
                .filter(k -> k.startsWith("projectBudget."))
                .map(k -> k.substring("projectBudget.".length()))
                .toList();
    }
}
