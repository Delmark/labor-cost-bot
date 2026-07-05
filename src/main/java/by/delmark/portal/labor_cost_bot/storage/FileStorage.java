package by.delmark.portal.labor_cost_bot.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileStorage {

    private final JsonMapper mapper;

    @Value("${data.storage}")
    private String dataDir;

    @CacheEvict(value = "userData", allEntries = true)
    public void updateUserData(UserData userData) {
        Path dataPath = Paths.get(dataDir);
        try {
            if (!Files.exists(dataPath)) {
                Files.createDirectories(dataPath);
            }
            Path dataJsonPath = dataPath.resolve("data.json");
            Files.write(dataJsonPath, mapper.writeValueAsBytes(userData));
        } catch (IOException e) {
            log.error("Failed to update user data", e);
            throw new RuntimeException(e);
        }
    }

    @Cacheable(value = "userData", unless = "#result == null || #result.isEmpty()")
    public Optional<UserData> getUserData() {
        Path dataDirPath = Paths.get(dataDir);
        Path dataJsonPath = dataDirPath.resolve("data.json");
        if (!Files.exists(dataDirPath) || !Files.exists(dataJsonPath)) {
            return Optional.empty();
        }

        try (InputStream inputStream = new FileInputStream(dataJsonPath.toFile())) {
            return Optional.of(mapper.readValue(inputStream, UserData.class));
        } catch (IOException fe) {
            log.error("Failed to read data.json file", fe);
            return Optional.empty();
        }
    }
}
