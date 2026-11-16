# Prüfbericht zur Projektversion 0.1.1

Prüfdatum: 12.11.2026. Umgebung: Linux, OpenJDK 21.0.13, Apache Maven 3.9.11, Spring Boot 3.5.16. Der Quellcode wird für Java 17 kompiliert.

## Automatisierte Prüfung

`clean verify` wurde erfolgreich ausgeführt: 11 Tests, 0 Fehler, 0 fehlgeschlagene Assertions, 0 übersprungene Tests. Maven meldete BUILD SUCCESS und erzeugte die ausführbare Spring-Boot-JAR. Die Surefire-Ergebnisdateien stehen in `testprotokolle/`.

| Testmethode | Nachgewiesenes Verhalten |
|---|---|
| anonymousReportIsPersisted | Anonymer Fund gespeichert, reporter leer, Status REPORTED |
| recoveryLifecycleAndOnePersonWithMultipleNets | Eine Person für zwei Netze; doppelte Übernahme und fremde Bestätigung abgewiesen; Abschluss und Filter |
| missingRequiresContactAndClosesNet | Kontaktpflicht, Abschluss als MISSING, Ausschluss aus offenen Netzen |
| invalidCoordinatesAndIncompleteContactAreRejected | Ungültiger Breitengrad und unvollständiger Kontakt ohne Speicherung abgewiesen |
| simultaneousReservationsHaveExactlyOneWinner | Zwei konkurrierende Übernahmen desselben Netzes, genau ein Erfolg |
| pagesRenderAndCsrfIsRequired | Ansichten rendern, fehlendes CSRF-Token abgewiesen, gültige Meldung und Validierungsrückgabe |
| coordinateBoundariesAndPrecisionAreValidated | Zulässige Grenzwerte; ungültiger Längengrad, Fläche null und zu viele Nachkommastellen abgewiesen |
| personalReportTrimsAndReusesContact | Kontaktfelder werden außen getrimmt; Person bei Meldung und Übernahme wiederverwendet |
| plannedNetCanBeMissingButCannotBeReopened | Keine Bergung vor Übernahme; Verschollen nach Übernahme möglich; ursprünglicher Bergungskontakt bleibt erhalten; Endzustände gesperrt |
| webActionsPersistChangesAndRejectInvalidContacts | HTTP-Formularaktionen für Übernahme, Bergung und Verschollenmeldung; fehlerhafte Kontakte; verständlicher deutscher Hinweis bei nicht numerischer Eingabe |
| completedRecoverySurvivesApplicationRestart | Zwei nacheinander gestartete und geschlossene Anwendungskontexte mit derselben H2-Datei: Netz, Kontakt und Bergungsstatus bleiben erhalten |

Die Integrationstests nutzen eine In-Memory-Datenbank. Der Neustarttest verwendet eine temporäre H2-Dateidatenbank und startet zwei eingebettete Webserver nacheinander auf freien Ports. Die normale Anwendungsdatenbank wird nicht verändert.

## Reproduktion

Windows: `.\mvnw.cmd clean verify`

Linux/macOS: `sh mvnw clean verify`

Ein vollständiges JDK 17 oder 21 ist erforderlich. Beim ersten Lauf muss Maven die Abhängigkeiten herunterladen können. Die Prüfung wurde unter Linux ausgeführt; ein eigener Windows-Lauf wird damit nicht behauptet.

## Reichweite

Die Prüfungen belegen ausgewählte fachliche Regeln, HTTP-Verarbeitung, CSRF-Schutz und Dateipersistenz. Sie sind kein vollständiger Sicherheits-, Last- oder Barrierefreiheitstest. Die bestehenden Bildschirmaufnahmen dokumentieren die Oberfläche; sie sind von den automatisierten Tests zu unterscheiden.

## Prüfung der paketierten Anwendung

Die erzeugte JAR wurde separat mit `java -jar` gestartet. Ein HTTP-Client rief das Meldeformular auf, übernahm Session und CSRF-Token und übermittelte einen anonymen Fund. Die nachfolgende Übersicht enthielt GN-1 und die Erfolgsmeldung; die H2-Datei wurde angelegt. Anschließend wurde der Prozess geordnet beendet. Dies ist ein HTTP-Funktionstest, keine neue visuelle Browserprüfung.
