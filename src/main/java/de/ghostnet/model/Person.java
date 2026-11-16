package de.ghostnet.model;
import jakarta.persistence.*;
@Entity
@Table(name="contact_person", uniqueConstraints=@UniqueConstraint(columnNames={"name", "phone"}))
public class Person {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, length=100) private String name;
    @Column(nullable=false, length=40) private String phone;
    protected Person() {}
    public Person(String name, String phone) { this.name=name; this.phone=phone; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
}
