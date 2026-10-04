package me.neabows.utils;

import java.util.ArrayList;
import java.util.List;
import me.neabows.NeaBows;
import me.neabows.managers.PowerManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * Utilidades para gestionar poderes en items de arco.
 *
 * FILOSOFÍA DE NBT:
 *   - Nunca se modifica el displayName original del item.
 *   - El lore original se preserva íntegro; las líneas del poder
 *     se AÑADEN al final y se eliminan limpiamente al quitar el poder.
 *   - Los datos del poder se guardan en el PersistentDataContainer,
 *     que es invisible para vanilla y no interfiere con otros plugins.
 */
public final class ItemUtils {

    private ItemUtils() {}

    private static final MiniMessage MM = MiniMessage.miniMessage();

    // ── Claves PDC ────────────────────────────────────────────────────────────
    /** Nombre del poder almacenado (String, en minúsculas). */
    public static NamespacedKey POWER_KEY;
    /** Nivel del poder almacenado (Integer). */
    public static NamespacedKey LEVEL_KEY;
    /**
     * Número de líneas de lore ORIGINALES antes de que NeaBows añadiese las suyas.
     * Permite restaurar el lore original al quitar el poder.
     */
    public static NamespacedKey ORIG_LORE_KEY;

    /** Llamar una sola vez desde NeaBows#onEnable. */
    public static void init(NeaBows plugin) {
        POWER_KEY = new NamespacedKey(plugin, "bow_power");
        LEVEL_KEY = new NamespacedKey(plugin, "bow_level");
        ORIG_LORE_KEY = new NamespacedKey(plugin, "bow_orig_lore");
    }

    // ── Aplicar poder ─────────────────────────────────────────────────────────

    /**
     * Añade (o actualiza) el poder en el arco:
     *  1. Guarda las claves PDC.
     *  2. Restaura el lore original (si ya tenía un poder previo).
     *  3. Añade la sección de poder al final del lore.
     *  El displayName del item NUNCA se modifica.
     */
    public static void applyPower(ItemStack bow, String powerName, int level) {
        if (!isBow(bow)) return;

        ItemMeta meta = bow.getItemMeta();
        if (meta == null) return;

        PowerManager pm = NeaBows.getInstance().getPowerManager();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        // ── 1. Obtener / calcular tamaño original del lore ────────────────────
        List<Component> baseLore;
        if (pdc.has(ORIG_LORE_KEY, PersistentDataType.INTEGER)) {
            // Ya tenía poder: recortar al lore original
            int origSize = pdc.get(ORIG_LORE_KEY, PersistentDataType.INTEGER);
            List<Component> current = meta.lore();
            if (current == null) current = new ArrayList<>();
            baseLore = new ArrayList<>(
                current.subList(0, Math.min(origSize, current.size()))
            );
        } else {
            // Primera vez: guardar tamaño actual como "original"
            List<Component> current = meta.lore();
            baseLore =
                current != null ? new ArrayList<>(current) : new ArrayList<>();
            pdc.set(ORIG_LORE_KEY, PersistentDataType.INTEGER, baseLore.size());
        }

        // ── 2. Guardar datos del poder en PDC ─────────────────────────────────
        pdc.set(POWER_KEY, PersistentDataType.STRING, powerName.toLowerCase());
        pdc.set(LEVEL_KEY, PersistentDataType.INTEGER, level);

        // ── 3. Construir sección de poder ─────────────────────────────────────
        String displayName = pm.getPowerDisplayName(powerName);
        String color = pm.getPowerColor(powerName);
        String roman = toRoman(level);

        List<Component> powerLore = new ArrayList<>();
        // Separador visual si ya había lore antes
        if (!baseLore.isEmpty()) {
            powerLore.add(MM.deserialize("<reset>"));
        }

        powerLore.add(MM.deserialize("<reset><gray>Efectos del Arco:</gray>"));
        powerLore.add(
            MM.deserialize(
                "<reset><gray>- </gray><color:" +
                    color +
                    ">⚡ " +
                    displayName +
                    " " +
                    roman +
                    "</color>"
            )
        );

        // ── 4. Combinar y aplicar ─────────────────────────────────────────────
        List<Component> finalLore = new ArrayList<>(baseLore);
        finalLore.addAll(powerLore);
        meta.lore(finalLore);
        bow.setItemMeta(meta);
    }

    // ── Quitar poder ──────────────────────────────────────────────────────────

    /**
     * Elimina el poder del arco y restaura el lore original exactamente.
     * No toca enchants, custom model data ni ningún otro dato.
     */
    public static void removePower(ItemStack bow) {
        if (!isBow(bow)) return;
        ItemMeta meta = bow.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        // Restaurar lore original
        if (pdc.has(ORIG_LORE_KEY, PersistentDataType.INTEGER)) {
            int origSize = pdc.get(ORIG_LORE_KEY, PersistentDataType.INTEGER);
            List<Component> current = meta.lore();
            if (current != null) {
                List<Component> restored = new ArrayList<>(
                    current.subList(0, Math.min(origSize, current.size()))
                );
                meta.lore(restored.isEmpty() ? null : restored);
            }
        }

        // Limpiar claves PDC del plugin
        pdc.remove(POWER_KEY);
        pdc.remove(LEVEL_KEY);
        pdc.remove(ORIG_LORE_KEY);

        bow.setItemMeta(meta);
    }

    // ── Lectura de datos ──────────────────────────────────────────────────────

    public static String getPower(ItemStack item) {
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta
            .getPersistentDataContainer()
            .get(POWER_KEY, PersistentDataType.STRING);
    }

    public static int getLevel(ItemStack item) {
        if (item == null) return 0;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;
        Integer lvl = meta
            .getPersistentDataContainer()
            .get(LEVEL_KEY, PersistentDataType.INTEGER);
        return lvl != null ? lvl : 0;
    }

    public static boolean hasPower(ItemStack item) {
        return getPower(item) != null;
    }

    // ── Crear arco con poder ──────────────────────────────────────────────────

    /** Crea un arco nuevo (Material.BOW) con el poder aplicado. */
    public static ItemStack createPoweredBow(String powerName, int level) {
        ItemStack bow = new ItemStack(Material.BOW);
        applyPower(bow, powerName, level);
        return bow;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public static boolean isBow(ItemStack item) {
        if (item == null) return false;
        Material t = item.getType();
        return t == Material.BOW || t == Material.CROSSBOW;
    }

    /** Número a numeral romano (1-5). */
    public static String toRoman(int n) {
        return switch (n) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(n);
        };
    }

    /** Barra visual de nivel. Ej. nivel 2 de 3 → ██░ */
    private static String buildLevelBar(int level, int max) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= max; i++) {
            sb.append(
                i <= level ? "<yellow>█</yellow>" : "<dark_gray>█</dark_gray>"
            );
        }
        return sb.toString();
    }
}
