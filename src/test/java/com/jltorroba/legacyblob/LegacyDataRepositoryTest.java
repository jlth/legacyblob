package com.jltorroba.legacyblob;



import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@DataJpaTest
@ExtendWith(SpringExtension.class)
public class LegacyDataRepositoryTest {

    @Autowired
    private LegacyDataRepository repository;

    @Test
    public void createAndGetTest () throws IOException {

        LegacyData data = create();
        LegacyData savedData = repository.getOne(data.getId());
        assertNotNull(savedData.getData());
        assertArrayEquals(savedData.getData(), data.getData());

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
}
