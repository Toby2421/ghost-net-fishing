package de.ghostnet.service;
import de.ghostnet.model.*;
import de.ghostnet.repository.*;
import de.ghostnet.web.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import java.util.List;
@Service
@Validated
@Transactional
public class GhostNetService {
    private final GhostNetRepository nets;
    private final PersonRepository people;
    public GhostNetService(GhostNetRepository nets, PersonRepository people) { this.nets=nets; this.people=people; }
    @Transactional(readOnly=true)
    public List<GhostNet> list(boolean openOnly) {
        return nets.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
            .filter(n -> !openOnly || n.getStatus().isOpen()).toList();
    }
    @Transactional(readOnly=true)
    public GhostNet get(Long id) {
        return nets.findById(id).orElseThrow(() -> new BusinessException("Dieses Netz wurde nicht gefunden."));
    }
    public GhostNet report(@Valid ReportForm form) {
        Person reporter=form.getName().isBlank() ? null : contact(form.getName(), form.getPhone());
        return nets.save(new GhostNet(form.getLatitude(), form.getLongitude(), form.getSizeSquareMeters(), reporter));
    }
    public void reserve(Long id, @Valid ContactForm form) {
        GhostNet net=locked(id);
        if(net.getStatus()!=NetStatus.REPORTED) throw new BusinessException("Dieses Netz ist nicht mehr zur Übernahme verfügbar.");
        net.planRecovery(contact(form.getName(), form.getPhone()));
    }
    public void recover(Long id, @Valid ContactForm form) {
        GhostNet net=locked(id);
        if(net.getStatus()!=NetStatus.RECOVERY_PLANNED) throw new BusinessException("Zuerst muss eine Bergung übernommen werden.");
        Person rescuer=net.getRescuer();
        if(!rescuer.getName().equals(form.getName()) || !rescuer.getPhone().equals(form.getPhone()))
            throw new BusinessException("Die Angaben stimmen nicht mit der eingetragenen bergenden Person überein.");
        net.recover();
    }
    public void missing(Long id, @Valid ContactForm form) {
        GhostNet net=locked(id);
        if(!net.getStatus().isOpen()) throw new BusinessException("Dieses Netz ist bereits abgeschlossen.");
        net.markMissing(contact(form.getName(), form.getPhone()));
    }
    private GhostNet locked(Long id) {
        return nets.findForUpdate(id).orElseThrow(() -> new BusinessException("Dieses Netz wurde nicht gefunden."));
    }
    private Person contact(String name, String phone) {
        // Gleiche exakt eingegebene Kontaktdaten werden einer vorhandenen Person zugeordnet.
        // Dies ermöglicht mehrere Netze je Person, ist aber keine Identitätsprüfung.
        return people.findByNameAndPhone(name, phone).orElseGet(() -> people.save(new Person(name, phone)));
    }
}
