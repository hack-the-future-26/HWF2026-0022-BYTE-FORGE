package com.titleverify.titleverify_ai;

import com.titleverify.titleverify_ai.config.RegistryDataSeeder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=none"
})
class TitleverifyAiApplicationTests {

    @MockitoBean
    private DataSource dataSource;

    @MockitoBean
    private RegistryDataSeeder registryDataSeeder;

    @BeforeEach
    void setUp() throws Exception {
        Connection connection = Mockito.mock(Connection.class);
        DatabaseMetaData metaData = Mockito.mock(DatabaseMetaData.class);
        Mockito.when(dataSource.getConnection()).thenReturn(connection);
        Mockito.when(connection.getMetaData()).thenReturn(metaData);
    }

    @Test
    void contextLoads() {
    }

}
