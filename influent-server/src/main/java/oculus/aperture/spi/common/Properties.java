package oculus.aperture.spi.common;

/**
 * Stub class to replace missing ApertureJS dependency
 */
public class Properties extends java.util.Properties {
    
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }
    
    public int getInteger(String key, int defaultValue) {
        String value = getProperty(key);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }
    
    public double getDouble(String key, double defaultValue) {
        String value = getProperty(key);
        return value != null ? Double.parseDouble(value) : defaultValue;
    }
    
    public String getString(String key, String defaultValue) {
        return getProperty(key, defaultValue);
    }
}