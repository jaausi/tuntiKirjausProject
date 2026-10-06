package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.dao.Dao;
import com.sirvja.tuntikirjaus.domain.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigurationServiceTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("should import the configurations it exported")
    void shouldImportExportedConfiguration() throws IOException {
        InMemoryConfigurationDao sourceDao = new InMemoryConfigurationDao();
        ConfigurationService source = new ConfigurationService(sourceDao);
        source.insertOrUpdate(new Configuration("browser", "CHROME"));
        source.insertOrUpdate(new Configuration("kiekuHoursSaveButtonXpath", "//button[.//span[normalize-space()='Tallenna']]"));
        source.saveKiekuProjectMapping("Other admin work", "Hallinto ja kehitys (ÄÖ)");
        source.saveProjectBudget("TUNTI", 37.5);
        source.insertOrUpdate(new Configuration("nullValue", null));
        Path file = tempDir.resolve("configuration.properties");

        assertEquals(4, source.exportConfiguration(file));

        InMemoryConfigurationDao targetDao = new InMemoryConfigurationDao();
        targetDao.save(new Configuration("browser", "SAFARI"));
        targetDao.save(new Configuration("logUploadUrl", "https://logs.example"));
        ConfigurationService target = new ConfigurationService(targetDao);

        assertEquals(4, target.importConfiguration(file));

        Map<String, String> expected = new TreeMap<>(sourceDao.values);
        expected.remove("nullValue");
        expected.put("logUploadUrl", "https://logs.example");
        assertEquals(expected, targetDao.values);
        assertEquals(Map.of("Other admin work", "Hallinto ja kehitys (ÄÖ)"), target.getKiekuProjectMappings());
    }

    @Test
    @DisplayName("should not import anything when the file contains an invalid value")
    void shouldNotImportInvalidConfiguration() throws IOException {
        Path file = tempDir.resolve("configuration.properties");
        Files.writeString(file, "kiekuUrl=https://kieku.example\nbrowser=NETSCAPE\n", StandardCharsets.UTF_8);
        InMemoryConfigurationDao dao = new InMemoryConfigurationDao();

        assertThrows(IllegalArgumentException.class, () -> new ConfigurationService(dao).importConfiguration(file));
        assertEquals(Map.of(), dao.values);
    }

    @Test
    @DisplayName("should remove a Kieku project mapping")
    void shouldRemoveKiekuProjectMapping() {
        ConfigurationService service = new ConfigurationService(new InMemoryConfigurationDao());
        service.saveKiekuProjectMapping("TUNTI", "Kehitys");
        service.saveKiekuProjectMapping("OTHER", "Muu");

        service.removeKiekuProjectMapping("TUNTI");

        assertEquals(Map.of("OTHER", "Muu"), service.getKiekuProjectMappings());
    }

    private static class InMemoryConfigurationDao implements Dao<Configuration, String> {
        private final Map<String, String> values = new TreeMap<>();

        @Override
        public Optional<Configuration> get(String id) {
            return values.containsKey(id) ? Optional.of(new Configuration(id, values.get(id))) : Optional.empty();
        }

        @Override
        public List<Configuration> getAllToList() {
            List<Configuration> configurations = new ArrayList<>();
            values.forEach((key, value) -> configurations.add(new Configuration(key, value)));
            return configurations;
        }

        @Override
        public List<Configuration> getAllFromToList(LocalDate localDate) {
            return List.of();
        }

        @Override
        public Configuration save(Configuration configuration) {
            values.put(configuration.getKey(), configuration.getValue());
            return configuration;
        }

        @Override
        public void update(Configuration configuration) {
            values.put(configuration.getKey(), configuration.getValue());
        }

        @Override
        public void delete(Configuration configuration) {
            values.remove(configuration.getKey());
        }
    }
}
