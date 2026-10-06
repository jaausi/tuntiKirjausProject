package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.dao.ConfigurationDao;
import com.sirvja.tuntikirjaus.dao.Dao;
import com.sirvja.tuntikirjaus.domain.Configuration;
import com.sirvja.tuntikirjaus.exporter.impl.KiekuConfiguration;
import com.sirvja.tuntikirjaus.exporter.impl.KiekuHoursConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Properties;
import java.util.TreeMap;

public class ConfigurationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationService.class);

    private static final String EXPORT_FILE_COMMENT = "Tuntikirjaus configuration";

    private final Dao<Configuration, String> configurationDao;

    public ConfigurationService() {
        this(new ConfigurationDao());
    }

    public ConfigurationService(Dao<Configuration, String> configurationDao) {
        this.configurationDao = configurationDao;
    }

    public KiekuConfiguration getKiekuConfiguration() {
        return KiekuConfiguration.mapToConfiguration(getAllAsMap());
    }

    public KiekuHoursConfiguration getKiekuHoursConfiguration() {
        return KiekuHoursConfiguration.mapToConfiguration(getAllAsMap());
    }

    private Map<String, String> getAllAsMap() {
        // Values may be null, which Collectors.toMap does not allow
        Map<String, String> confMap = new TreeMap<>();
        configurationDao.getAllToList().forEach(conf -> confMap.put(conf.getKey(), conf.getValue()));
        return confMap;
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

    private static String kiekuProjectMappingKey(String project) {
        return KiekuHoursConfiguration.PROJECT_MAPPING_KEY_PREFIX + project;
    }

    /**
     * @return project of the application -> Kieku project
     */
    public Map<String, String> getKiekuProjectMappings() {
        return getKiekuHoursConfiguration().projectToKiekuRow();
    }

    public void saveKiekuProjectMapping(String project, String kiekuProject) {
        insertOrUpdate(new Configuration(kiekuProjectMappingKey(project), kiekuProject));
    }

    public void removeKiekuProjectMapping(String project) {
        configurationDao.delete(new Configuration(kiekuProjectMappingKey(project), ""));
    }

    /**
     * Writes all configurations to a properties file.
     *
     * @return number of exported configurations
     */
    public int exportConfiguration(Path file) throws IOException {
        Properties properties = new Properties();
        configurationDao.getAllToList().stream()
                .filter(conf -> conf.getValue() != null)
                .forEach(conf -> properties.setProperty(conf.getKey(), conf.getValue()));
        // Properties.store writes the keys in sorted order
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            properties.store(writer, EXPORT_FILE_COMMENT);
        }
        LOGGER.info("Exported {} configuration(s) to a file", properties.size());
        return properties.size();
    }

    /**
     * Reads configurations from a properties file written by {@link #exportConfiguration(Path)}. Configurations
     * in the file replace existing ones with the same key, other existing configurations are kept.
     *
     * @return number of imported configurations
     * @throws IllegalArgumentException if the file contains invalid values, nothing is imported then
     */
    public int importConfiguration(Path file) throws IOException {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        Map<String, String> configurations = new TreeMap<>();
        properties.stringPropertyNames().forEach(key -> configurations.put(key, properties.getProperty(key)));

        validate(configurations);
        configurations.forEach((key, value) -> insertOrUpdate(new Configuration(key, value)));
        LOGGER.info("Imported {} configuration(s) from a file", configurations.size());
        return configurations.size();
    }

    private static void validate(Map<String, String> configurations) {
        String browser = configurations.get(KiekuConfiguration.BROWSER_KEY);
        if (browser != null && !KiekuConfiguration.isValidBrowserConfig(browser)) {
            throw new IllegalArgumentException("Tiedostossa on virheellinen selain: " + browser);
        }
        String saveAutomatically = configurations.get(KiekuHoursConfiguration.SAVE_AUTOMATICALLY_KEY);
        if (saveAutomatically != null && !KiekuHoursConfiguration.isValidBooleanConfig(saveAutomatically)) {
            throw new IllegalArgumentException("Asetuksen " + KiekuHoursConfiguration.SAVE_AUTOMATICALLY_KEY
                    + " arvon pitää olla true tai false");
        }
    }
}
