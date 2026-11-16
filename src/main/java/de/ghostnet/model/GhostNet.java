package de.ghostnet.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name="ghost_net")
public class GhostNet {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Version private long version;
    @Column(nullable=false, precision=9, scale=6) private BigDecimal latitude;
    @Column(nullable=false, precision=9, scale=6) private BigDecimal longitude;
    @Column(nullable=false, precision=12, scale=2) private BigDecimal sizeSquareMeters;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private NetStatus status=NetStatus.REPORTED;
    @Column(nullable=false) private LocalDateTime reportedAt=LocalDateTime.now();
    @ManyToOne @JoinColumn(name="reporter_id") private Person reporter;
    @ManyToOne @JoinColumn(name="rescuer_id") private Person rescuer;
    @ManyToOne @JoinColumn(name="missing_reporter_id") private Person missingReporter;
    protected GhostNet() {}
    public GhostNet(BigDecimal latitude, BigDecimal longitude, BigDecimal size, Person reporter) {
        this.latitude=latitude; this.longitude=longitude; this.sizeSquareMeters=size; this.reporter=reporter;
    }
    public Long getId() { return id; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public BigDecimal getSizeSquareMeters() { return sizeSquareMeters; }
    public NetStatus getStatus() { return status; }
    public LocalDateTime getReportedAt() { return reportedAt; }
    public Person getReporter() { return reporter; }
    public Person getRescuer() { return rescuer; }
    public Person getMissingReporter() { return missingReporter; }
    public void planRecovery(Person person) { rescuer=person; status=NetStatus.RECOVERY_PLANNED; }
    public void recover() { status=NetStatus.RECOVERED; }
    public void markMissing(Person person) { missingReporter=person; status=NetStatus.MISSING; }
}
