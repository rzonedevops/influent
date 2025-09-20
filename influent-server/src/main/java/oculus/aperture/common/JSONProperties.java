package oculus.aperture.common;

import oculus.aperture.spi.common.Properties;

/**
 * Stub class to replace missing ApertureJS dependency
 */
public class JSONProperties extends Properties {
    
    public JSONProperties() {
        super();
    }
    
    public JSONProperties(String jsonString) {
        // Stub implementation - parse JSON and populate properties
        super();
    }
    
    public String[] getStrings(String key) {
        String value = getProperty(key);
        if (value != null) {
            // Simple implementation - split by comma
            return value.split(",");
        }
        return new String[0];
    }
}