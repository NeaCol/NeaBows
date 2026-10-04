package me.neabows.effects;

import me.neabows.NeaBows;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

/**
 * Aplica los efectos configurados en poderes.yml cuando una flecha con poder impacta.
 *
 * Soporta:
 *  - duracion-fuego    → setFireTicks
 *  - freeze-ticks      → setFreezeTicks (efecto nieve en polvo)
 *  - velocidad-y       → velocidad vertical (poder Viento)
 *  - efecto / efecto2 / efecto3 → PotionEffect con amplificador y duración
 */
public final class EffectHandler {

    private EffectHandler() {}

    // Mapeo de nombres del YAML a keys de Minecraft Registry (1.21.1)
    private static final Map<String, String> EFFECT_MAP = new HashMap<>();

    static {
        EFFECT_MAP.put("SLOWNESS",       "slowness");
        EFFECT_MAP.put("SLOW",           "slowness");          // nombre legacy
        EFFECT_MAP.put("MINING_FATIGUE", "mining_fatigue");
        EFFECT_MAP.put("SLOW_DIGGING",   "mining_fatigue");    // nombre legacy
        EFFECT_MAP.put("WITHER",         "wither");
        EFFECT_MAP.put("POISON",         "poison");
        EFFECT_MAP.put("BLINDNESS",      "blindness");
        EFFECT_MAP.put("DARKNESS",       "darkness");
        EFFECT_MAP.put("LEVITATION",     "levitation");
        EFFECT_MAP.put("WEAKNESS",       "weakness");
        EFFECT_MAP.put("NAUSEA",         "nausea");
        EFFECT_MAP.put("CONFUSION",      "nausea");            // nombre legacy
        EFFECT_MAP.put("HUNGER",         "hunger");
        EFFECT_MAP.put("ABSORPTION",     "absorption");
        EFFECT_MAP.put("SPEED",          "speed");
        EFFECT_MAP.put("STRENGTH",       "strength");
    }

    /**
     * Lee la sección poderes.<poder>.niveles.<nivel> del config
     * y aplica todos los efectos configurados al objetivo.
     */
    public static void applyPower(LivingEntity target, String powerName, int level) {
        FileConfiguration cfg = NeaBows.getInstance().getPowerManager().getPoderesConfig();
        String path = "poderes." + powerName.toLowerCase() + ".niveles." + level;
        ConfigurationSection sec = cfg.getConfigurationSection(path);

        if (sec == null) {
            NeaBows.getInstance().getLogger()
                   .warning("[NeaBows] Sección no encontrada en poderes.yml: " + path);
            return;
        }

        // ── Fuego ─────────────────────────────────────────────────────────────
        int fireTicks = sec.getInt("duracion-fuego", 0);
        if (fireTicks > 0) {
            target.setFireTicks(fireTicks);
        }

        // ── Hielo (nieve en polvo) ─────────────────────────────────────────────
        int freezeTicks = sec.getInt("freeze-ticks", 0);
        if (freezeTicks > 0) {
            target.setFreezeTicks(Math.min(freezeTicks, target.getMaxFreezeTicks()));
        }

        // ── Viento (velocidad vertical) ────────────────────────────────────────
        double velY = sec.getDouble("velocidad-y", 0.0);
        if (velY > 0.0) {
            Vector v = target.getVelocity();
            v.setY(velY);
            target.setVelocity(v);
        }

        // ── Efectos de poción (hasta 3) ────────────────────────────────────────
        applyEffect(target, sec, "efecto",  "duracion-efecto",  "amplificador");
        applyEffect(target, sec, "efecto2", "duracion-efecto2", "amplificador2");
        applyEffect(target, sec, "efecto3", "duracion-efecto3", "amplificador3");
    }

    // ── Interno ───────────────────────────────────────────────────────────────

    private static void applyEffect(LivingEntity target, ConfigurationSection sec,
                                    String efectoKey, String durKey, String ampKey) {
        String name = sec.getString(efectoKey);
        if (name == null || name.isBlank()) return;

        PotionEffectType type = resolveEffect(name);
        if (type == null) {
            NeaBows.getInstance().getLogger()
                   .warning("[NeaBows] Efecto desconocido en poderes.yml: '" + name + "'");
            return;
        }

        int duration  = sec.getInt(durKey, 100);
        int amplifier = sec.getInt(ampKey, 0);

        // ambient=false, particles=true, icon=true → se ve correctamente en pantalla
        target.addPotionEffect(new PotionEffect(type, duration, amplifier, false, true, true));
    }

    /**
     * Resuelve un nombre de efecto (puede ser el nombre del YAML o clave de Minecraft)
     * usando la Registry de 1.21.1.
     */
    private static PotionEffectType resolveEffect(String rawName) {
        // Primero intentar por el mapeo de compatibilidad
        String key = EFFECT_MAP.getOrDefault(rawName.toUpperCase(), rawName.toLowerCase());
        try {
            PotionEffectType type = Registry.EFFECT.get(NamespacedKey.minecraft(key));
            if (type != null) return type;
        } catch (Exception ignored) {}

        // Fallback: probar el nombre original en minúsculas directamente
        try {
            return Registry.EFFECT.get(NamespacedKey.minecraft(rawName.toLowerCase()));
        } catch (Exception ignored) {}

        return null;
    }
}
