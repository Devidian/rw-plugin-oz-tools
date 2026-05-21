package de.omegazirkel.risingworld.tools.settings;

public class AdminSettingsEntry {
    private final String key;
    private final String label;
    private final String description;
    private final String value;
    private final String defaultValue;
    private final AdminSettingsType type;
    private final boolean sensitive;
    private final AdminSettingsWriter writer;

    public AdminSettingsEntry(
            String key,
            String label,
            String description,
            String value,
            String defaultValue,
            AdminSettingsType type,
            boolean sensitive,
            AdminSettingsWriter writer) {
        this.key = key;
        this.label = label == null || label.isBlank() ? key : label;
        this.description = description == null ? "" : description;
        this.value = value == null ? "" : value;
        this.defaultValue = defaultValue == null ? "" : defaultValue;
        this.type = type == null ? AdminSettingsType.STRING : type;
        this.sensitive = sensitive;
        this.writer = writer;
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public String getValue() {
        if (sensitive) {
            return "";
        }
        return value;
    }

    public String getDefaultValue() {
        if (sensitive) {
            return "";
        }
        return defaultValue;
    }

    public AdminSettingsType getType() {
        return type;
    }

    public boolean isSensitive() {
        return sensitive;
    }

    public boolean isEditable() {
        return !sensitive && writer != null;
    }

    public boolean write(String newValue) {
        if (!isEditable()) {
            return false;
        }
        return writer.write(newValue);
    }
}
