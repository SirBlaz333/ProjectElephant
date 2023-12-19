package edu.sumdu.tss.elephant.helper;

import edu.sumdu.tss.elephant.helper.Keys;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeysTest {
    private MockedStatic<FileUtils> mockedFileUtils;
    private File tempFile;

    @BeforeEach
    void setUp() throws IOException {
        mockedFileUtils = Mockito.mockStatic(FileUtils.class);
        tempFile = createTempPropertiesFile();
    }

    @AfterEach
    void tearDown() {
        mockedFileUtils.close();
        tempFile.delete();
    }

    @Test
    void testLoadParams() throws IOException {
        mockedFileUtils.when(() -> FileUtils.openInputStream(any(File.class)))
                .thenReturn(new FileInputStream(tempFile));

        Keys.loadParams(tempFile);
        assertEquals("127.0.0.1", Keys.get("DB.URL"));
    }

    private File createTempPropertiesFile() throws IOException {
        Properties properties = new Properties();
        properties.setProperty("DB.PORT", "5432");
        properties.setProperty("DB.LOCAL_PATH", "d:\\elephant_temp\\");
        properties.setProperty("DB.HOST", "127.0.0.1");
        properties.setProperty("DB.URL", "127.0.0.1");
        properties.setProperty("DB.NAME", "elephant");
        properties.setProperty("DB.USERNAME", "postgres");
        properties.setProperty("DB.PASSWORD", "password");
        properties.setProperty("DB.OS_USER", "WINDOWS");
        properties.setProperty("APP.URL", "http://127.0.0.1:7000");
        properties.setProperty("APP.PORT", "7000");
        properties.setProperty("EMAIL.HOST", "smtp.gmail.com");
        properties.setProperty("EMAIL.PORT", "465");
        properties.setProperty("EMAIL.USER", "service-mail@gmail.com");
        properties.setProperty("EMAIL.PASSWORD", "my-secret-password");
        properties.setProperty("EMAIL.FROM", "service-mail@gmail.com");
        properties.setProperty("EMAIL.SSL", "true");
        properties.setProperty("DEFAULT_LANG", "EN");
        properties.setProperty("ENV", "PRODUCTION");

        File file = File.createTempFile("test", ".properties");
        try (FileOutputStream out = new FileOutputStream(file)) {
            properties.store(out, null);
        }
        return file;
    }

    @Test
    void testLoadParamsWithMissingProperties() throws IOException {
        Properties incompleteProperties = new Properties();
        incompleteProperties.setProperty("DB.URL", "127.0.0.1"); // Missing other properties

        File incompleteFile = createTempPropertiesFile(incompleteProperties);
        mockedFileUtils.when(() -> FileUtils.openInputStream(any(File.class)))
                .thenReturn(new FileInputStream(incompleteFile));

        assertThrows(IllegalArgumentException.class, () -> Keys.loadParams(incompleteFile));
    }

    @Test
    void testGetForExistingKey() {
        assertEquals("127.0.0.1", Keys.get("DB.URL"));
    }

    @Test
    void testGetWithUninitializedKeys() {
        assertThrows(RuntimeException.class, () -> Keys.get("DB.URL"));
    }

    @Test
    void testGetForEmptyValueKey() throws IOException {
        // Create a file with standard properties
        File fileWithProperties = createTempPropertiesFile();

        // Load properties from file, modify one property to be empty, and save back
        Properties properties = new Properties();
        try (FileInputStream in = new FileInputStream(fileWithProperties)) {
            properties.load(in);
        }
        properties.setProperty("DB.LOCAL_PATH", ""); // Set to empty
        try (FileOutputStream out = new FileOutputStream(fileWithProperties)) {
            properties.store(out, null);
        }

        // Proceed with the test
        mockedFileUtils.when(() -> FileUtils.openInputStream(any(File.class)))
                .thenReturn(new FileInputStream(fileWithProperties));
        Keys.loadParams(fileWithProperties);
        assertThrows(RuntimeException.class, () -> Keys.get("DB.LOCAL_PATH"));
    }

    @Test
    void testIsProduction() {
        assertTrue(Keys.isProduction());
    }

    private File createTempPropertiesFile(Properties properties) throws IOException {
        File file = File.createTempFile("test", ".properties");
        try (FileOutputStream out = new FileOutputStream(file)) {
            properties.store(out, null);
        }
        return file;
    }
}