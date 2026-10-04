package me.neabows.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.neabows.NeaBows;

/**
 * Gestiona cooldowns por jugador para evitar abusos de spam con poderes.
 * Almacena el timestamp (ms) del último disparo para cada par (jugador, poder).
 */
public class CooldownManager {

    private final NeaBows plugin;

    // Map<playerUUID, Map<powerName, lastUsedTimestampMs>>
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public CooldownManager(NeaBows plugin) {
        this.plugin = plugin;
    }

    // ── Consultar cooldown ────────────────────────────────────────────────────

    /**
     * Devuelve true si el jugador todavía está en cooldown para ese poder.
     */
    public boolean isOnCooldown(UUID playerId, String powerName) {
        return getRemainingMs(playerId, powerName) > 0;
    }

    /**
     * Milisegundos restantes de cooldown. 0 = listo para disparar.
     */
    public long getRemainingMs(UUID playerId, String powerName) {
        Map<String, Long> map = cooldowns.get(playerId);
        if (map == null) return 0;
        Long lastUse = map.get(powerName.toLowerCase());
        if (lastUse == null) return 0;
        long cooldownMs = getCooldownMs(powerName);
        long remaining = cooldownMs - (System.currentTimeMillis() - lastUse);
        return Math.max(0, remaining);
    }

    /**
     * Segundos restantes con un decimal. Útil para mensajes al jugador.
     */
    public double getRemainingSeconds(UUID playerId, String powerName) {
        return getRemainingMs(playerId, powerName) / 1000.0;
    }

    // ── Registrar disparo ─────────────────────────────────────────────────────

    /**
     * Registra el momento actual como el último disparo del jugador con ese poder.
     */
    public void setCooldown(UUID playerId, String powerName) {
        cooldowns
            .computeIfAbsent(playerId, k -> new HashMap<>())
            .put(powerName.toLowerCase(), System.currentTimeMillis());
    }

    /**
     * Elimina el cooldown de un jugador para un poder específico.
     * Útil si un disparo fue cancelado y queremos devolverle el cooldown.
     */
    public void removeCooldown(UUID playerId, String powerName) {
        Map<String, Long> map = cooldowns.get(playerId);
        if (map != null) {
            map.remove(powerName.toLowerCase());
        }
    }

    // ── Limpieza ──────────────────────────────────────────────────────────────

    /** Elimina todos los cooldowns (útil al deshabilitar el plugin). */
    public void clear() {
        cooldowns.clear();
    }

    // ── Interno ───────────────────────────────────────────────────────────────

    private long getCooldownMs(String powerName) {
        // Buscar cooldown específico del poder, si no el global
        int specific = plugin
            .getConfig()
            .getInt("cooldowns.per-power." + powerName.toLowerCase(), -1);
        int seconds = (specific >= 0)
            ? specific
            : plugin.getConfig().getInt("cooldowns.global", 3);
        return seconds * 1000L;
    }
}
