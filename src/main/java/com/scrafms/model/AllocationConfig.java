package com.scrafms.model;

public class AllocationConfig {
    private int id;
    private String configKey;
    private String configValue;
    private String description;

    public AllocationConfig() {}

    public AllocationConfig(String configKey, String configValue) {
        this.configKey = configKey;
        this.configValue = configValue;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }

    public String getConfigValue() { return configValue; }
    public void setConfigValue(String configValue) { this.configValue = configValue; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
