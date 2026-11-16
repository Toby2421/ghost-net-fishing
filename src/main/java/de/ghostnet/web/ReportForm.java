package de.ghostnet.web;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public class ReportForm {
    @NotNull(message="Bitte Breitengrad eingeben.")
    @DecimalMin(value="-90", message="Mindestens -90 Grad.") @DecimalMax(value="90", message="Höchstens 90 Grad.")
    @Digits(integer=3, fraction=6, message="Maximal sechs Nachkommastellen.")
    private BigDecimal latitude;
    @NotNull(message="Bitte Längengrad eingeben.")
    @DecimalMin(value="-180", message="Mindestens -180 Grad.") @DecimalMax(value="180", message="Höchstens 180 Grad.")
    @Digits(integer=3, fraction=6, message="Maximal sechs Nachkommastellen.")
    private BigDecimal longitude;
    @NotNull(message="Bitte geschätzte Fläche eingeben.") @Positive(message="Die Fläche muss größer als null sein.")
    @Digits(integer=10, fraction=2, message="Maximal zehn Stellen vor und zwei nach dem Komma.")
    private BigDecimal sizeSquareMeters;
    @Size(max=100, message="Maximal 100 Zeichen.") private String name="";
    @Size(max=40, message="Maximal 40 Zeichen.") private String phone="";
    @AssertTrue(message="Für eine persönliche Meldung bitte Name und Telefonnummer angeben. Für eine anonyme Meldung beide Felder leer lassen.")
    public boolean isContactComplete() { return name.isBlank() == phone.isBlank(); }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal v) { latitude=v; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal v) { longitude=v; }
    public BigDecimal getSizeSquareMeters() { return sizeSquareMeters; }
    public void setSizeSquareMeters(BigDecimal v) { sizeSquareMeters=v; }
    public String getName() { return name; }
    public void setName(String v) { name=v==null ? "" : v.trim(); }
    public String getPhone() { return phone; }
    public void setPhone(String v) { phone=v==null ? "" : v.trim(); }
}
