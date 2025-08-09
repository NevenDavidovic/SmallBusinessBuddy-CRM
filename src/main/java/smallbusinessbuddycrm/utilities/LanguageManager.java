package smallbusinessbuddycrm.utilities;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import java.util.List;
import java.util.ArrayList;

public class LanguageManager {
    private static LanguageManager instance;
    private ObjectProperty<ResourceBundle> bundleProperty = new SimpleObjectProperty<>();
    private Locale currentLocale;
    private static final String LANGUAGE_PREF_KEY = "selected_language";
    private Properties properties;

    // ⭐ NEW: Observable properties that UI elements can bind to
    private List<Runnable> languageChangeListeners = new ArrayList<>();

    private LanguageManager() {
        Preferences prefs = Preferences.userNodeForPackage(LanguageManager.class);
        String savedLang = prefs.get(LANGUAGE_PREF_KEY, "hr");
        setLanguage(savedLang);
    }

    public static LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }

    public void setLanguage(String languageCode) {
        currentLocale = new Locale(languageCode);
        properties = new Properties();

        try {
            // Load properties file with UTF-8 encoding
            String fileName = "/i18n/messages_" + languageCode + ".properties";
            InputStream inputStream = getClass().getResourceAsStream(fileName);
            if (inputStream != null) {
                try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                    properties.load(reader);
                    System.out.println("UTF-8 properties loaded for: " + languageCode);
                }
            } else {
                System.err.println("Properties file not found: " + fileName);
                loadFallbackBundle(languageCode);
            }
        } catch (Exception e) {
            System.err.println("Error loading UTF-8 properties: " + e.getMessage());
            loadFallbackBundle(languageCode);
        }

        Preferences prefs = Preferences.userNodeForPackage(LanguageManager.class);
        prefs.put(LANGUAGE_PREF_KEY, languageCode);
        System.out.println("Language switched to: " + languageCode);

        // ⭐ NEW: Notify all listeners that language changed
        notifyLanguageChange();
    }

    private void loadFallbackBundle(String languageCode) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", currentLocale);
            bundleProperty.set(bundle);
        } catch (Exception e) {
            System.err.println("Even fallback ResourceBundle failed: " + e.getMessage());
        }
    }

    public String getText(String key) {
        try {
            // First try UTF-8 properties
            if (properties != null && properties.containsKey(key)) {
                return properties.getProperty(key);
            }
            // Fallback to ResourceBundle
            else if (bundleProperty.get() != null) {
                return bundleProperty.get().getString(key);
            }
        } catch (Exception e) {
            System.err.println("Missing translation key: " + key);
        }
        return "[" + key + "]";
    }

    // ⭐ NEW: Method for controllers to register for language change notifications
    public void addLanguageChangeListener(Runnable listener) {
        languageChangeListeners.add(listener);
    }

    // ⭐ NEW: Method to remove listeners (for cleanup)
    public void removeLanguageChangeListener(Runnable listener) {
        languageChangeListeners.remove(listener);
    }

    // ⭐ NEW: Notify all registered controllers when language changes
    private void notifyLanguageChange() {
        System.out.println("Notifying " + languageChangeListeners.size() + " listeners of language change");
        for (Runnable listener : languageChangeListeners) {
            try {
                listener.run();
            } catch (Exception e) {
                System.err.println("Error in language change listener: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // ⭐ NEW: Create observable string properties for UI binding
    public StringProperty createObservableText(String key) {
        StringProperty property = new SimpleStringProperty(getText(key));

        // Add a listener that updates this property when language changes
        addLanguageChangeListener(() -> property.set(getText(key)));

        return property;
    }

    public boolean isEnglish() {
        return "en".equals(currentLocale.getLanguage());
    }

    public boolean isCroatian() {
        return "hr".equals(currentLocale.getLanguage());
    }

    public Locale getCurrentLocale() {
        return currentLocale;
    }

    public ResourceBundle getResourceBundle() {
        return bundleProperty.get();
    }

    public ObjectProperty<ResourceBundle> getResourceBundleProperty() {
        return bundleProperty;
    }
}