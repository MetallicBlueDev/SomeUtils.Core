package com.tr4ncer.io;

import com.tr4ncer.logger.*;
import com.tr4ncer.utils.*;
import java.io.*;
import java.nio.file.*;
import java.text.*;
import java.util.*;

/**
 * Return informations about a persistent set of properties.
 *
 * @author Sébastien Villemain
 */
public final class SimplePropertyInfo {

    private Path path;

    private Properties prop;

    private boolean createOnDefaulfValue = false;

    public SimplePropertyInfo(Path path) {
        setPath(path);
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public boolean canCreateOnDefaulfValue() {
        return createOnDefaulfValue;
    }

    public void setCreateOnDefaulfValue(boolean createOnDefaulfValue) {
        this.createOnDefaulfValue = createOnDefaulfValue;
    }

    public Properties getProperties() {
        return prop;
    }

    public String getString(String name, String defaultValue) {
        String value = defaultValue;
        String valueString = prop != null ? prop.getProperty(name) : null;

        if (valueString != null) {
            value = valueString;
        } else {
            createOnDefaulfValue(name, defaultValue);
        }
        return value;
    }

    public float getFloat(String name, float defaultValue) {
        float value = defaultValue;
        String valueString = getString(name, null);

        if (valueString != null) {
            value = ConvertHelper.toFloat(valueString, defaultValue);
        }
        return value;
    }

    public int getInt(String name, int defaultValue) {
        int value = defaultValue;
        String valueString = getString(name, null);

        if (valueString != null) {
            value = ConvertHelper.toInt(valueString, defaultValue);
        }
        return value;
    }

    public boolean getBoolean(String name, boolean defaultValue) {
        boolean value = defaultValue;
        String valueString = getString(name, null);

        if (valueString != null) {
            value = ConvertHelper.toBoolean(valueString, defaultValue);
        }
        return value;
    }

    public Date getDateInternational(String name) {
        return getDate(name, DateHelper.INTERNATIONAL_FORMAT);
    }

    public Date getDate(String name, String pattern) {
        Date value = null;
        String valueString = getString(name, null);

        if (valueString != null && !valueString.isBlank()) {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            try {
                value = sdf.parse(valueString);
            } catch (ParseException ex) {
                LoggerManager.getInstance().addError(ex);
            }
        }
        return value;
    }

    public void setString(String name, String value) {
        checkInstance();
        prop.setProperty(name, value != null ? value : "");
    }

    public void setDateInternational(String name, Date value) {
        setDate(name, value, DateHelper.INTERNATIONAL_FORMAT);
    }

    public void setDate(String name, Date value, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        setString(name, sdf.format(value));
    }

    public boolean load() {
        clear();
        prop = loadFromPath();
        return prop != null;
    }

    public boolean save() {
        return saveToPath();
    }

    public void clear() {
        if (prop != null) {
            prop.clear();
            prop = null;
        }
    }

    private void checkInstance() {
        if (prop == null) {
            prop = new Properties();
        }
    }

    private void createOnDefaulfValue(String name, String value) {
        if (createOnDefaulfValue) {
            setString(name, value);
            save();
        }
    }

    private boolean saveToPath() {
        boolean saved = false;

        if (path != null && prop != null) {
            File file = path.toFile();

            try (OutputStream output = new FileOutputStream(file)) {
                prop.store(output, null);
                saved = true;
            } catch (IOException ex) {
                LoggerManager.getInstance().addError(ex);
            }
        }
        return saved;
    }

    private Properties loadFromPath() {
        Properties rslt = null;

        if (path != null) {
            rslt = new Properties();
            File file = path.toFile();

            try (InputStream input = new FileInputStream(file)) {
                rslt.load(input);
            } catch (IOException ex) {
                LoggerManager.getInstance().addError(ex);
                rslt = null;
            }
        }
        return rslt;
    }

}
