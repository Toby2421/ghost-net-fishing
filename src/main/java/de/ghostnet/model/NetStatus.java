package de.ghostnet.model;
public enum NetStatus {
    REPORTED("Gemeldet"), RECOVERY_PLANNED("Bergung bevorstehend"),
    RECOVERED("Geborgen"), MISSING("Verschollen");
    private final String label;
    NetStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
    public boolean isOpen() { return this == REPORTED || this == RECOVERY_PLANNED; }
}
