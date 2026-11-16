package de.ghostnet;
import de.ghostnet.model.*;
import de.ghostnet.repository.*;
import de.ghostnet.service.*;
import de.ghostnet.web.*;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureMockMvc
class GhostNetApplicationTests {
    @Autowired GhostNetService service;
    @Autowired GhostNetRepository nets;
    @Autowired PersonRepository people;
    @Autowired MockMvc mvc;
    @BeforeEach void clear() { nets.deleteAll(); people.deleteAll(); }
    ReportForm report() {
        ReportForm f=new ReportForm(); f.setLatitude(new BigDecimal("54.18"));
        f.setLongitude(new BigDecimal("12.08")); f.setSizeSquareMeters(new BigDecimal("25")); return f;
    }
    ContactForm contact(String name) {
        ContactForm f=new ContactForm(); f.setName(name); f.setPhone("0000-123456"); return f;
    }
    @Test void anonymousReportIsPersisted() {
        Long id=service.report(report()).getId();
        GhostNet n=nets.findById(id).orElseThrow();
        assertThat(n.getReporter()).isNull(); assertThat(n.getStatus()).isEqualTo(NetStatus.REPORTED);
        assertThat(n.getLatitude()).isEqualByComparingTo("54.18");
    }
    @Test void recoveryLifecycleAndOnePersonWithMultipleNets() {
        Long a=service.report(report()).getId(), b=service.report(report()).getId();
        service.reserve(a,contact("Demo Person")); service.reserve(b,contact("Demo Person"));
        assertThat(people.count()).isEqualTo(1);
        assertThat(service.get(a).getRescuer().getId()).isEqualTo(service.get(b).getRescuer().getId());
        assertThatThrownBy(() -> service.reserve(a,contact("Andere Person"))).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.recover(a,contact("Andere Person"))).isInstanceOf(BusinessException.class);
        service.recover(a,contact("Demo Person"));
        assertThat(service.list(true)).extracting(GhostNet::getId).containsExactly(b);
        assertThatThrownBy(() -> service.missing(a,contact("Demo Person"))).isInstanceOf(BusinessException.class);
    }
    @Test void missingRequiresContactAndClosesNet() {
        Long id=service.report(report()).getId();
        assertThatThrownBy(() -> service.missing(id,new ContactForm())).isInstanceOf(ConstraintViolationException.class);
        service.missing(id,contact("Demo Person"));
        assertThat(service.get(id).getMissingReporter()).isNotNull();
        assertThat(service.list(true)).isEmpty();
        assertThatThrownBy(() -> service.reserve(id,contact("Demo Person"))).isInstanceOf(BusinessException.class);
    }
    @Test void invalidCoordinatesAndIncompleteContactAreRejected() {
        ReportForm f=report(); f.setLatitude(new BigDecimal("91"));
        assertThatThrownBy(() -> service.report(f)).isInstanceOf(ConstraintViolationException.class);
        f.setLatitude(BigDecimal.ZERO); f.setName("Nur Name");
        assertThatThrownBy(() -> service.report(f)).isInstanceOf(ConstraintViolationException.class);
        assertThat(nets.count()).isZero();
    }
    @Test void simultaneousReservationsHaveExactlyOneWinner() throws Exception {
        Long id=service.report(report()).getId();
        ExecutorService pool=Executors.newFixedThreadPool(2); CountDownLatch start=new CountDownLatch(1);
        try {
            Callable<Boolean> reserve=() -> { start.await(); try { service.reserve(id,contact(Thread.currentThread().getName())); return true; } catch(BusinessException e) { return false; } };
            Future<Boolean> a=pool.submit(reserve), b=pool.submit(reserve); start.countDown();
            assertThat(a.get(10,TimeUnit.SECONDS) ^ b.get(10,TimeUnit.SECONDS)).isTrue();
            assertThat(service.get(id).getStatus()).isEqualTo(NetStatus.RECOVERY_PLANNED);
        } finally { pool.shutdownNow(); }
    }
    @Test void pagesRenderAndCsrfIsRequired() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Gemeinsam gegen Geisternetze")));
        mvc.perform(get("/nets/new")).andExpect(status().isOk());
        mvc.perform(post("/nets/new").param("latitude","54").param("longitude","12").param("sizeSquareMeters","20")).andExpect(status().isForbidden());
        mvc.perform(post("/nets/new").with(csrf()).param("latitude","54").param("longitude","12").param("sizeSquareMeters","20"))
            .andExpect(status().is3xxRedirection());
        Long id=service.list(false).get(0).getId();
        for(String action : new String[]{"reserve","recover","missing"})
            mvc.perform(get("/nets/"+id+"/"+action)).andExpect(status().isOk());
        mvc.perform(get("/").param("open","true")).andExpect(status().isOk());
        mvc.perform(post("/nets/new").with(csrf()).param("latitude","91").param("longitude","12").param("sizeSquareMeters","20"))
            .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("reportForm","latitude"));
    }

    @Test void coordinateBoundariesAndPrecisionAreValidated() {
        ReportForm valid = report();
        valid.setLatitude(new BigDecimal("-90"));
        valid.setLongitude(new BigDecimal("180"));
        valid.setSizeSquareMeters(new BigDecimal("0.01"));
        service.report(valid);
        ReportForm invalid = report();
        invalid.setLongitude(new BigDecimal("180.000001"));
        assertThatThrownBy(() -> service.report(invalid)).isInstanceOf(ConstraintViolationException.class);
        invalid.setLongitude(BigDecimal.ZERO);
        invalid.setSizeSquareMeters(BigDecimal.ZERO);
        assertThatThrownBy(() -> service.report(invalid)).isInstanceOf(ConstraintViolationException.class);
        invalid.setSizeSquareMeters(new BigDecimal("0.001"));
        assertThatThrownBy(() -> service.report(invalid)).isInstanceOf(ConstraintViolationException.class);
        assertThat(nets.count()).isEqualTo(1);
    }

    @Test void personalReportTrimsAndReusesContact() {
        ReportForm form = report();
        form.setName("  Demo Person  ");
        form.setPhone("  0000-123456  ");
        Long id = service.report(form).getId();
        service.reserve(id, contact("Demo Person"));
        assertThat(people.count()).isEqualTo(1);
        assertThat(service.get(id).getReporter().getId()).isEqualTo(service.get(id).getRescuer().getId());
    }

    @Test void plannedNetCanBeMissingButCannotBeReopened() {
        Long id = service.report(report()).getId();
        assertThatThrownBy(() -> service.recover(id, contact("Demo Person"))).isInstanceOf(BusinessException.class);
        service.reserve(id, contact("Demo Person"));
        service.missing(id, contact("Andere Person"));
        assertThat(service.get(id).getStatus()).isEqualTo(NetStatus.MISSING);
        assertThat(service.get(id).getRescuer().getName()).isEqualTo("Demo Person");
        assertThat(service.get(id).getMissingReporter().getName()).isEqualTo("Andere Person");
        assertThatThrownBy(() -> service.recover(id, contact("Demo Person"))).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.missing(id, contact("Andere Person"))).isInstanceOf(BusinessException.class);
    }

    @Test void webActionsPersistChangesAndRejectInvalidContacts() throws Exception {
        mvc.perform(post("/nets/new").with(csrf()).param("latitude", "abc")
            .param("longitude", "12").param("sizeSquareMeters", "20"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Bitte einen gültigen Breitengrad als Zahl eingeben.")));
        Long id = service.report(report()).getId();
        mvc.perform(post("/nets/" + id + "/reserve").with(csrf()).param("name", "Demo Person"))
            .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("contactForm", "phone"));
        mvc.perform(post("/nets/" + id + "/reserve").with(csrf())
            .param("name", "Demo Person").param("phone", "0000-123456"))
            .andExpect(status().is3xxRedirection());
        assertThat(service.get(id).getStatus()).isEqualTo(NetStatus.RECOVERY_PLANNED);
        mvc.perform(post("/nets/" + id + "/recover").with(csrf())
            .param("name", "Andere Person").param("phone", "0000-123456"))
            .andExpect(status().isOk()).andExpect(model().attributeHasErrors("contactForm"));
        assertThat(service.get(id).getStatus()).isEqualTo(NetStatus.RECOVERY_PLANNED);
        mvc.perform(post("/nets/" + id + "/recover").with(csrf())
            .param("name", "Demo Person").param("phone", "0000-123456"))
            .andExpect(status().is3xxRedirection());
        assertThat(service.get(id).getStatus()).isEqualTo(NetStatus.RECOVERED);
        Long missing = service.report(report()).getId();
        mvc.perform(post("/nets/" + missing + "/missing").with(csrf())
            .param("name", "Demo Person").param("phone", "0000-123456"))
            .andExpect(status().is3xxRedirection());
        assertThat(service.get(missing).getStatus()).isEqualTo(NetStatus.MISSING);
        assertThat(service.list(true)).isEmpty();
    }
}
