# Beispieldaten und manuelle Kontrolle

Diese Eingabefolge erzeugt vier Zustände ähnlich den Abbildungen des Fallstudienberichts. Nur fiktive Kontakte verwenden. Die Tabelle ist eine reproduzierbare Anleitung, kein zusätzliches Protokoll eines behaupteten manuellen Testlaufs.

Die Netz-IDs beginnen nur bei einer neuen Datenbank mit 1. Eine vorhandene Datenbank nicht löschen; bei Bedarf zuerst die Anwendung beenden und den Datenordner sichern beziehungsweise verschieben.

| Schritt | Eingabe oder Aktion | Erwartetes Ergebnis |
|---|---|---|
| 1 | Netz melden: 54.18 / 12.08 / 25 m²; kein Kontakt | Gemeldet, keine meldende Person |
| 2 | Netz melden: 54.22 / 12.16 / 40 m²; kein Kontakt | Zweiter Fund |
| 3 | Netz melden: 54.30 / 12.24 / 65 m²; kein Kontakt | Dritter Fund |
| 4 | Netz melden: 54.40 / 12.32 / 15 m²; kein Kontakt | Vierter Fund |
| 5 | Netz 1 und Netz 2 mit Demo Person / 0000-123456 übernehmen | Zwei Netze, gleiche bergende Person |
| 6 | Netz 1 mit demselben Kontakt geborgen melden | Netz 1 Geborgen |
| 7 | Netz 3 mit Demo Meldung / 0000-987654 verschollen melden | Netz 3 Verschollen |
| 8 | Offene Bergungen öffnen | Nur Netz 2 und Netz 4 sichtbar |
| 9 | Anwendung beenden und aus demselben Ordner starten | Alle vier Netze und ihre Statuswerte erhalten |

Für eine Validierungsaufnahme einen neuen Fund mit Namen, aber ohne Telefonnummer absenden: Die Oberfläche weist auf den unvollständigen Kontakt hin. Dieser Fund darf nicht gespeichert werden.

## Datenbankabfragen

```sql
SELECT ID, STATUS, REPORTER_ID AS MELDER, RESCUER_ID AS BERGER,
       MISSING_REPORTER_ID AS VERSCHOLLEN_VON
FROM GHOST_NET ORDER BY ID;

SELECT * FROM CONTACT_PERSON ORDER BY ID;

SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'PUBLIC'
ORDER BY TABLE_NAME, ORDINAL_POSITION;
```

Die Aliasnamen MELDER, BERGER und VERSCHOLLEN_VON benennen nur die angezeigten Ergebnisspalten um. Die Datenbankspalten heißen weiterhin REPORTER_ID, RESCUER_ID und MISSING_REPORTER_ID.
