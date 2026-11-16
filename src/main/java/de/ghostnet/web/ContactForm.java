package de.ghostnet.web;
import jakarta.validation.constraints.*;
public class ContactForm {
    @NotBlank(message="Bitte Namen eingeben.") @Size(max=100, message="Maximal 100 Zeichen.")
    private String name="";
    @NotBlank(message="Bitte Telefonnummer eingeben.") @Size(max=40, message="Maximal 40 Zeichen.")
    private String phone="";
    public String getName() { return name; }
    public void setName(String v) { name=v==null ? "" : v.trim(); }
    public String getPhone() { return phone; }
    public void setPhone(String v) { phone=v==null ? "" : v.trim(); }
}
