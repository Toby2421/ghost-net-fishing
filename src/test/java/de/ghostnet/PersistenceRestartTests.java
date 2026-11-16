package de.ghostnet;

import de.ghostnet.model.NetStatus;
import de.ghostnet.service.GhostNetService;
import de.ghostnet.web.ContactForm;
import de.ghostnet.web.ReportForm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.math.BigDecimal;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PersistenceRestartTests {
    @TempDir Path directory;

    @Test
    void completedRecoverySurvivesApplicationRestart() {
        String database = "jdbc:h2:file:" + directory.resolve("restart").toAbsolutePath()
                + ";DB_CLOSE_ON_EXIT=FALSE";
        Long id;
        try (ConfigurableApplicationContext first = start(database)) {
            GhostNetService service = first.getBean(GhostNetService.class);
            ReportForm report = new ReportForm();
            report.setLatitude(new BigDecimal("54.18"));
            report.setLongitude(new BigDecimal("12.08"));
            report.setSizeSquareMeters(new BigDecimal("25"));
            id = service.report(report).getId();
            ContactForm contact = new ContactForm();
            contact.setName("Neustart Test");
            contact.setPhone("0000-123456");
            service.reserve(id, contact);
            service.recover(id, contact);
        }
        try (ConfigurableApplicationContext second = start(database)) {
            GhostNetService service = second.getBean(GhostNetService.class);
            var net = service.get(id);
            assertThat(net.getStatus()).isEqualTo(NetStatus.RECOVERED);
            assertThat(net.getReporter()).isNull();
            assertThat(net.getRescuer().getName()).isEqualTo("Neustart Test");
            assertThat(net.getLatitude()).isEqualByComparingTo("54.18");
            assertThat(service.list(true)).isEmpty();
            assertThat(service.list(false)).hasSize(1);
        }
    }

    private ConfigurableApplicationContext start(String database) {
        return SpringApplication.run(GhostNetApplication.class,
                "--server.port=0", "--spring.datasource.url=" + database,
                "--spring.jpa.hibernate.ddl-auto=update",
                "--spring.h2.console.enabled=false", "--spring.main.banner-mode=off");
    }
}
