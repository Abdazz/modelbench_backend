package com.example.modelbench;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ModelbenchApplicationTests {

    @Autowired
    private JpaTransactionManager gestionnaireTransactions;

    @Test
    void leContexteDemarreAvecJpaActif() {
        assertThat(gestionnaireTransactions).isNotNull();
    }
}
