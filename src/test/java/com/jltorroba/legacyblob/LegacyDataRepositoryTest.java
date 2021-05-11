package com.jltorroba.legacyblob;



import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Arrays;
import java.util.Random;

import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@DataJpaTest
@ExtendWith(SpringExtension.class)
public class LegacyDataRepositoryTest {

    private static Path actualFile;
    @Autowired
    private LegacyDataRepository repository;

    @Autowired
    private JdbcTemplate template;

    @BeforeAll
    public static void init () {
        StorageServiceFactory.setService(new FileTemporalStorageService());
    }

    @AfterEach
    public void deleteFile () throws IOException {
        Files.deleteIfExists(actualFile);
    }

    @Test
    public void createAndGetTest () throws IOException, SerialException {

        LegacyData data = create();
        LegacyData savedData = repository.getOne(data.getId());
        assertNotNull(savedData.getData());
        assertArrayEquals(savedData.getData(), data.getData());
        SqlRowSet queryForRowSet = template.queryForRowSet("SELECT * FROM LEGACY_DATA");
        queryForRowSet.first();
        SerialBlob source = (SerialBlob) queryForRowSet.getObject(2);
        Path sourcePath = Paths.get(new String(source.getBinaryStream()
                .readAllBytes(), StandardCharsets.UTF_8));

    }

    @Test
    public void updateTest () throws IOException {
        Integer id = create().getId();
        byte[] updateBytes = "Updated".getBytes(StandardCharsets.UTF_8);
        try(ByteArrayOutputStream os= new ByteArrayOutputStream()){
            os.write(updateBytes);
            LegacyData data = repository.getOne(id);
            os.write(data.getData());
            data.setData(os.toByteArray());
        }
        LegacyData updated = repository.getOne(id);
        assertEquals(Arrays.compare(updateBytes, 0, updateBytes.length - 1, updated.getData(), 0, updateBytes.length - 1), 0);
    }

    private LegacyData create () throws IOException {
        try (InputStream is = this.getClass()
                .getResourceAsStream("/blob.dat")) {
            LegacyData data = new LegacyData();
            data.setData(is.readAllBytes());
            return repository.save(data);

        }
    }

    private static class FileTemporalStorageService implements StorageService {

        private Random random = new Random(Instant.now()
                .toEpochMilli());
        @Override
        public byte[] save (byte[] data) {
            try (ByteArrayInputStream is = new ByteArrayInputStream(data)) {

                Path file = Files.createTempFile("legacy-", String.valueOf(random.nextInt()));
                actualFile = file;
                Files.copy(is, file, StandardCopyOption.REPLACE_EXISTING);
                return file.toString()
                        .getBytes(StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        @Override
        public byte[] update (byte[] source, byte[] data) {
            Path file = Paths.get(new String(data, StandardCharsets.UTF_8));
            try (ByteArrayInputStream is = new ByteArrayInputStream(data)) {
                Files.copy(is, file);
            return source;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public byte[] get (byte[] source) {

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                Path file = Paths.get(new String(source, StandardCharsets.UTF_8));
                Files.copy(file, baos);
                return baos.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
