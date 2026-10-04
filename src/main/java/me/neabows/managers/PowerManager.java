package me.neabows.managers;

import me.neabows.NeaBows;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Gestiona la carga y acceso a los poderes definidos en poderes.yml
 */
public class PowerManager {

    private final NeaBows plugin;
    private FileConfiguration poderesConfig;
    private File poderesFile;

    public PowerManager(NeaBows plugin) {
        this.plugin = plugin;
        loadPoderesConfig();
    }

    private void loadPoderesConfig() {
        poderesFile = new File(plugin.getDataFolder(), "poderes.yml");
        if (!poderesFile.exists()) {
            plugin.saveResource("poderes.yml", false);
        }
        poderesConfig = YamlConfiguration.loadConfiguration(poderesFile);
    }

    /**
     * Recarga el archivo poderes.yml desde disco.
     */
    public void reload() {
        loadPoderesConfig();
    }

    public FileConfiguration getPoderesConfig() {
        return poderesConfig;
    }

    /**
     * Devuelve la lista de poderes habilitados.
     */
    public List<String> getAvailablePowers() {
        if (!poderesConfig.contains("poderes")) return new ArrayList<>();
        Set<String> keys = poderesConfig.getConfigurationSection("poderes").getKeys(false);
        List<String> list = new ArrayList<>();
        for (String key : keys) {
            if (poderesConfig.getBoolean("poderes." + key + ".habilitado", true)) {
                list.add(key);
            }
        }
        return list;
    }

    /**
     * Devuelve el nivel máximo de un poder.
     */
    public int getMaxLevel(String powerName) {
        return poderesConfig.getInt("poderes." + powerName.toLowerCase() + ".max-nivel", 1);
    }

    /**
     * Comprueba si existe un poder con ese nombre.
     */
    public boolean powerExists(String powerName) {
        return poderesConfig.contains("poderes." + powerName.toLowerCase());
    }

    /**
     * Comprueba si el poder está habilitado.
     */
    public boolean isEnabled(String powerName) {
        return poderesConfig.getBoolean("poderes." + powerName.toLowerCase() + ".habilitado", true);
    }

    /**
     * Devuelve el nombre bonito del poder (ej: "Fuego").
     */
    public String getPowerDisplayName(String powerName) {
        return poderesConfig.getString("poderes." + powerName.toLowerCase() + ".nombre", powerName);
    }

    /**
     * Devuelve el color principal (hex) del poder.
     */
    public String getPowerColor(String powerName) {
        return poderesConfig.getString("poderes." + powerName.toLowerCase() + ".color", "#FFFFFF");
    }

    /**
     * Devuelve el color secundario (hex) del poder.
     */
    public String getPowerSecondaryColor(String powerName) {
        return poderesConfig.getString("poderes." + powerName.toLowerCase() + ".color-secundario", "#CCCCCC");
    }

    /**
     * Devuelve la descripción de un nivel concreto.
     */
    public String getLevelDescription(String powerName, int level) {
        return poderesConfig.getString(
            "poderes." + powerName.toLowerCase() + ".niveles." + level + ".descripcion",
            "Nivel " + level
        );
    }
}
