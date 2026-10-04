package me.neabows.effects;

import me.neabows.NeaBows;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Rastrea las flechas con poder que están en vuelo y les aplica
 * una estela de partículas mientras no han aterrizado.
 *
 * El tracking es automático: se añade la flecha al disparar
 * y se elimina cuando aterriza, muere o es inválida.
 */
public class ArrowTracker {

    private final NeaBows plugin;

    /** Flechas actualmente en vuelo: UUID → datos del poder. */
    private final Map<UUID, ArrowData> tracked = new HashMap<>();

    private BukkitTask task;

    public ArrowTracker(NeaBows plugin) {
        this.plugin = plugin;
    }

    // ── API pública ───────────────────────────────────────────────────────────

    /** Empieza a rastrear una flecha con poder. */
    public void trackArrow(UUID arrowId, String powerName, int level) {
        tracked.put(arrowId, new ArrowData(powerName, level));
    }

    /** Deja de rastrear una flecha (se llama al impactar). */
    public void untrack(UUID arrowId) {
        tracked.remove(arrowId);
    }

    /** Devuelve los datos de la flecha si está siendo rastreada, o null. */
    public ArrowData getData(UUID arrowId) {
        return tracked.get(arrowId);
    }

    // ── Tarea periódica ───────────────────────────────────────────────────────

    public void startTracking() {
        boolean enabled  = plugin.getConfig().getBoolean("settings.trail-particles", true);
        if (!enabled) return;

        int interval = plugin.getConfig().getInt("settings.trail-interval", 2);

        task = new BukkitRunnable() {
            @Override
            public void run() {
                tracked.entrySet().removeIf(entry -> {
                    Entity entity = plugin.getServer().getEntity(entry.getKey());

                    // Eliminar si la entidad ya no existe o está muerta
                    if (entity == null || !entity.isValid() || entity.isDead()) return true;
                    if (!(entity instanceof AbstractArrow arrow)) return true;

                    // Eliminar si ya aterrizó o entró en agua (efecto visual inútil)
                    if (arrow.isOnGround() || arrow.isInWater()) return true;

                    // Spawn de partícula de rastro
                    ArrowData data = entry.getValue();
                    ParticleEffects.spawnTrail(arrow.getLocation(), data.powerName(), data.level());
                    return false;
                });
            }
        }.runTaskTimer(plugin, 0L, interval);
    }

    public void stopTracking() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
        tracked.clear();
    }

    // ── Data record ───────────────────────────────────────────────────────────

    /** Datos asociados a una flecha en vuelo. */
    public record ArrowData(String powerName, int level) {}
}
