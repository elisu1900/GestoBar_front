package com.elias.gestobar.config;

import java.io.IOException;
import java.util.Properties;

public class AppConfig {

    private static final Properties props = new Properties();

    static {
        try (var is = AppConfig.class.getResourceAsStream("/app.properties")) {
            if (is == null) throw new RuntimeException("No se encontró app.properties");
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar app.properties", e);
        }
    }

    private AppConfig() {}

    public static String getApiBaseUrl()       { return get("api.base.url"); }
    public static int    getConnectTimeout()   { return getInt("api.timeout.connect", 10); }
    public static int    getReadTimeout()      { return getInt("api.timeout.read", 15); }

    public static String getLoginEndpoint()    { return get("api.endpoint.auth.login"); }
    public static String getMeEndpoint()       { return get("api.endpoint.auth.me"); }
    public static String getLogoutEndpoint()   { return get("api.endpoint.auth.logout"); }

    public static String getBalanceDailyEndpoint() { return get("api.endpoint.balance.daily"); }
    public static String getBalanceResetEndpoint() { return get("api.endpoint.balance.reset"); }

    public static String getCategoriesEndpoint()       { return get("api.endpoint.categories"); }
    public static String getCategoriesSearchEndpoint() { return get("api.endpoint.categories.search"); }

    public static String getProductsEndpoint()          { return get("api.endpoint.products"); }
    public static String getProductsSearchEndpoint()    { return get("api.endpoint.products.search"); }
    public static String getProductsActiveEndpoint()    { return get("api.endpoint.products.active"); }
    public static String getProductsInactiveEndpoint()  { return get("api.endpoint.products.inactive"); }
    public static String getProductsPriceRangeEndpoint(){ return get("api.endpoint.products.price.range"); }
    public static String getProductsByCategoryEndpoint(){ return get("api.endpoint.products.by.category"); }

    public static String getTablesEndpoint()   { return get("api.endpoint.tables"); }

    public static String getTicketsEndpoint()  { return get("api.endpoint.tickets"); }

    public static String getUsersEndpoint()    { return get("api.endpoint.users"); }

    public static String  getWindowTitle()     { return get("app.window.title"); }
    public static int     getWindowWidth()     { return getInt("app.window.width", 1280); }
    public static int     getWindowHeight()    { return getInt("app.window.height", 800); }
    public static boolean isMaximized()        { return getBool("app.window.maximized", false); }
    public static int     getRefreshInterval() { return getInt("app.ui.refresh.interval", 30); }

    public static boolean isDevMode()          { return "dev".equalsIgnoreCase(get("app.env")); }

    public static String fullUrl(String endpoint) {
        return getApiBaseUrl() + endpoint;
    }

    private static String get(String key) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank())
            throw new RuntimeException("Propiedad no encontrada: " + key);
        return value.trim();
    }

    private static int getInt(String key, int def) {
        try { return Integer.parseInt(props.getProperty(key, String.valueOf(def)).trim()); }
        catch (NumberFormatException e) { return def; }
    }

    private static boolean getBool(String key, boolean def) {
        String val = props.getProperty(key);
        return val != null ? Boolean.parseBoolean(val.trim()) : def;
    }
}