# Ghost Net Fishing

Java-Webanwendung zur Meldung und Bergung von Geisternetzen. Projektversion 0.1.1, Kurs IPWA02-01.

## Voraussetzungen und Start

Benötigt werden ein vollständiges JDK 17 oder 21 sowie Internetzugang für den ersten Download der Maven-Abhängigkeiten. Eine zusätzliche Maven-Installation ist nicht notwendig.

1. Projektarchiv vollständig entpacken.
2. Den Ordner mit `pom.xml` in IntelliJ IDEA als Maven-Projekt öffnen.
3. Ein JDK 17 oder 21 als Project SDK einstellen und den Maven-Import abwarten.
4. `de.ghostnet.GhostNetApplication` starten. Das Arbeitsverzeichnis ist der Projektordner.
5. Im Browser http://localhost:8080 öffnen.

Alternativ im Projektordner:

```powershell
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```sh
sh mvnw spring-boot:run
```

## Funktionen und Fachregeln

- Netze mit GPS-Koordinaten und geschätzter Fläche in m² erfassen.
- Anonyme Meldung: Name und Telefonnummer bleiben beide leer. Persönliche Meldung: beide Angaben erforderlich.
- Bergung mit Name und Telefonnummer übernehmen. Je Netz ist höchstens eine bergende Person möglich.
- Dieselbe Person kann mehrere Netze übernehmen. Der Abgleich verwendet außen getrimmte, ansonsten exakte Kontaktangaben.
- Offene Bergungen nach den Zuständen Gemeldet und Bergung bevorstehend filtern.
- Übernommene Netze mit denselben Kontaktangaben als Geborgen abschließen.
- Offene Netze mit verpflichtenden Kontaktangaben als Verschollen abschließen.
- Geborgen und Verschollen sind Endzustände.

Weltkarte und Umverteilung von Bergungen gehören nicht zum gewählten Funktionsumfang.

## Technik und Projektstruktur

Spring Boot 3.5.16, Spring MVC, Thymeleaf, Spring Data JPA/Hibernate, H2 und Bean Validation. Spring Security schützt schreibende Formularaktionen durch CSRF-Token; es gibt keinen Benutzerlogin.

- `model`: Entitäten und Statuswerte
- `repository`: JPA-Zugriff einschließlich exklusiver Datensatzsperre
- `service`: transaktionale Fachregeln
- `web`: Controller und validierte Formularobjekte
- `config`: Web-Sicherheitskonfiguration
- `src/main/resources/templates`: serverseitige Ansichten
- `src/test`: automatisierte Funktions- und Persistenztests
- `docs`: Entwurf, reproduzierbare Beispieldaten und Prüfbericht

## Datenhaltung

Das Paket enthält keine lokale Datenbank. Beim ersten Start ist die Liste leer. Die Anwendung legt `data/ghostnet.mv.db` im Arbeitsverzeichnis an. Ein Neustart mit demselben Arbeitsverzeichnis erhält die Daten. Für eine neue leere Datenbank zuerst die Anwendung beenden und den Datenordner an einen anderen Ort verschieben.

Die lokale H2-Konsole ist unter http://localhost:8080/h2-console erreichbar:

- JDBC URL: `jdbc:h2:file:./data/ghostnet`
- Benutzer: `sa`
- Passwort: leer

## Tests und Paketbau

Windows:

```powershell
.\mvnw.cmd clean verify
```

Linux/macOS:

```sh
sh mvnw clean verify
```

Die Tests verwenden eine getrennte In-Memory-Datenbank und eine temporäre Dateidatenbank. Die Anwendungsdatenbank wird nicht verändert. Die Ergebnisse stehen in `target/surefire-reports/`. Ein erfolgreicher Lauf endet mit `BUILD SUCCESS` und erzeugt `target/ghost-net-fishing-0.1.1.jar`.

Start der erzeugten JAR aus dem Projektordner:

```sh
java -jar target/ghost-net-fishing-0.1.1.jar
```

## Grenzen

Kontaktangaben sind keine verifizierte Identität. Telefonnummern werden auf Vorhandensein und Länge, nicht auf Erreichbarkeit geprüft. Der Prototyp bindet an 127.0.0.1 und ist für lokale Nutzung bestimmt. Für einen öffentlichen Betrieb sind ein Identitäts- und Berechtigungskonzept, geschützte Datenbankverwaltung, Migrationen und Sicherungen erforderlich. Nur fiktive Kontakte für Tests verwenden.

## Veröffentlichung

Der Projektordner enthält den für das öffentliche Repository vorgesehenen Quellcode. `target/`, `data/` und IDE-Einstellungen sind durch `.gitignore` ausgeschlossen. Aufgabenstellungs-PDF und persönliche Prüfungsunterlagen gehören nicht in das öffentliche Repository.
