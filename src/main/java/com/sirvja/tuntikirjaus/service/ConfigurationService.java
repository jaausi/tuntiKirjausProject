package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.dao.ConfigurationDao;
import com.sirvja.tuntikirjaus.dao.Dao;
import com.sirvja.tuntikirjaus.domain.Configuration;
import com.sirvja.tuntikirjaus.exporter.impl.KiekuConfiguration;

import java.util.Map;
import java.util.Optional;
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
}
