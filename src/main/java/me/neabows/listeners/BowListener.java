package me.neabows.listeners;

import java.util.UUID;
import me.neabows.NeaBows;
import me.neabows.effects.EffectHandler;
import me.neabows.effects.ParticleEffects;
import me.neabows.managers.CooldownManager;
import me.neabows.utils.ItemUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.inventory.Inventory;
import org.bukkit.util.Vector;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * Intercepta el disparo del arco y el impacto de la flecha.
 *
 * FLUJO:
 *   1. EntityShootBowEvent  → revisa cooldown → etiqueta la flecha en PDC
 *                           → inicia rastro de partículas
 *   2. ProjectileHitEvent   → lee etiqueta PDC → aplica efectos al objetivo
 *                           → lanza partículas de impacto
 */
public class BowListener implements Listener {

    private final NeaBows plugin;
    private final MiniMessage mm;

    // Claves PDC que se guardan EN LA FLECHA (entidad)
    private final NamespacedKey arrowPowerKey;
    private final NamespacedKey arrowLevelKey;

    public BowListener(NeaBows plugin) {
        this.plugin = plugin;
        this.mm = plugin.getMiniMessage();
        this.arrowPowerKey = new NamespacedKey(plugin, "arrow_power");
        this.arrowLevelKey = new NamespacedKey(plugin, "arrow_level");
    }

    // ── Disparo ───────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onShoot(EntityShootBowEvent event) {
        // Solo interesa cuando un jugador dispara
        if (!(event.getEntity() instanceof Player player)) return;
        // Solo flechas (no fuegos artificiales de crossbow)
        if (!(event.getProjectile() instanceof AbstractArrow arrow)) return;

        ItemStack bow = event.getBow();
        if (bow == null || !ItemUtils.hasPower(bow)) return;

        String powerName = ItemUtils.getPower(bow);
        int level = ItemUtils.getLevel(bow);
        if (powerName == null || level <= 0) return;

        // Verificar que el poder esté habilitado en poderes.yml
        if (!plugin.getPowerManager().isEnabled(powerName)) return;

        // ── Cooldown ──────────────────────────────────────────────────────────
        CooldownManager cd = plugin.getCooldownManager();
        UUID uid = player.getUniqueId();

        if (cd.isOnCooldown(uid, powerName)) {
            double remaining = cd.getRemainingSeconds(uid, powerName);
            // No cancelar el disparo, simplemente no aplicar el poder
            String colorHex = plugin.getPowerManager().getPowerColor(powerName);
            player.sendActionBar(
                mm.deserialize(
                    "<red>⏳ Cooldown <color:" +
                        colorHex +
                        "><bold>" +
                        powerName +
                        "</bold></color>" +
                        " <red>→ <yellow>" +
                        String.format("%.1f", remaining) +
                        "s"
                )
            );
            return;
        }

        // Registrar el cooldown ANTES de dejar pasar el disparo
        cd.setCooldown(uid, powerName);

        // ── Lógica especial para Amatista (Triple disparo) ────────────────────
        if (powerName.equalsIgnoreCase("amatista")) {
            if (player.getGameMode() != GameMode.CREATIVE) {
                // Comprobar y consumir 3 flechas estrictamente
                if (!consumeArrows(player, 3)) {
                    event.setCancelled(true);
                    player.sendActionBar(mm.deserialize("<red>¡Necesitas al menos 3 flechas para usar Amatista!"));
                    // Quitar cooldown porque no pudo disparar
                    cd.removeCooldown(uid, powerName);
                    return;
                }
                // Como ya consumimos nosotros las flechas manualmente, evitamos que el evento consuma otra
                event.setConsumeItem(false);
            }

            // Disparar las 2 flechas extra (el evento ya dispara la del centro)
            Vector velocity = arrow.getVelocity();
            double speed = velocity.length();
            Vector direction = velocity.clone().normalize();

            // Rotar aprox 10 grados a izquierda y derecha
            double angle = Math.toRadians(10);
            Vector leftDir = rotateY(direction, angle).multiply(speed);
            Vector rightDir = rotateY(direction, -angle).multiply(speed);

            AbstractArrow leftArrow = player.launchProjectile(arrow.getClass(), leftDir);
            AbstractArrow rightArrow = player.launchProjectile(arrow.getClass(), rightDir);

            // Prevenir que se puedan recoger y duplicar
            leftArrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
            rightArrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
            arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED); // centro tampoco

            // Etiquetar las flechas extra
            leftArrow.getPersistentDataContainer().set(arrowPowerKey, PersistentDataType.STRING, powerName);
            leftArrow.getPersistentDataContainer().set(arrowLevelKey, PersistentDataType.INTEGER, level);
            rightArrow.getPersistentDataContainer().set(arrowPowerKey, PersistentDataType.STRING, powerName);
            rightArrow.getPersistentDataContainer().set(arrowLevelKey, PersistentDataType.INTEGER, level);

            // Rastrear las flechas extra
            plugin.getArrowTracker().trackArrow(leftArrow.getUniqueId(), powerName, level);
            plugin.getArrowTracker().trackArrow(rightArrow.getUniqueId(), powerName, level);
        }

        // ── Etiquetar la flecha con PDC (persistente en la entidad) ───────────
        PersistentDataContainer pdc = arrow.getPersistentDataContainer();
        pdc.set(arrowPowerKey, PersistentDataType.STRING, powerName);
        pdc.set(arrowLevelKey, PersistentDataType.INTEGER, level);

        // ── Empezar a rastrear para partículas de rastro ──────────────────────
        plugin
            .getArrowTracker()
            .trackArrow(arrow.getUniqueId(), powerName, level);

        // ── Sonido de disparo ─────────────────────────────────────────────────
        playSound(
            player.getLocation(),
            "sounds." + powerName + ".shoot",
            player.getWorld()
        );
    }

    // ── Impacto ───────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onArrowHit(ProjectileHitEvent event) {
        // Solo flechas etiquetadas por NeaBows
        if (!(event.getEntity() instanceof AbstractArrow arrow)) return;

        PersistentDataContainer pdc = arrow.getPersistentDataContainer();
        String powerName = pdc.get(arrowPowerKey, PersistentDataType.STRING);
        Integer level = pdc.get(arrowLevelKey, PersistentDataType.INTEGER);
        if (powerName == null || level == null) return;

        // Dejar de rastrear la flecha
        plugin.getArrowTracker().untrack(arrow.getUniqueId());

        // ── Partículas de impacto ─────────────────────────────────────────────
        ParticleEffects.spawnImpact(arrow.getLocation(), powerName, level);

        // ── Aplicar efectos solo si golpea a una entidad viva ─────────────────
        if (!(event.getHitEntity() instanceof LivingEntity target)) return;

        // Comprobar restricciones de configuración
        boolean affectSelf = plugin
            .getConfig()
            .getBoolean("settings.affect-self", false);
        boolean affectPlayers = plugin
            .getConfig()
            .getBoolean("settings.affect-players", true);

        // Shooter puede ser null (si la flecha fue generada de otra forma)
        if (arrow.getShooter() instanceof Player shooter) {
            if (!affectSelf && target.equals(shooter)) return;
            if (
                !affectPlayers &&
                target instanceof Player &&
                !target.equals(shooter)
            ) return;
        }

        // Aplicar efectos configurados en poderes.yml
        EffectHandler.applyPower(target, powerName, level);

        // ── Sonido de impacto ─────────────────────────────────────────────────
        playSound(
            arrow.getLocation(),
            "sounds." + powerName + ".hit",
            arrow.getLocation().getWorld()
        );
    }

    // ── Utilidad de sonido ────────────────────────────────────────────────────

    private void playSound(
        org.bukkit.Location loc,
        String configPath,
        org.bukkit.World world
    ) {
        if (world == null) return;
        String name = plugin.getConfig().getString(configPath);
        if (name == null || name.isBlank()) return;

        // Intentar con la enum Sound (nombre estilo ENTITY_BLAZE_SHOOT)
        try {
            Sound sound = Sound.valueOf(name.toUpperCase());
            world.playSound(loc, sound, 1.0f, 1.0f);
            return;
        } catch (IllegalArgumentException ignored) {}

        // Fallback: buscar por key de Minecraft (entity.blaze.shoot)
        try {
            String key = name.toLowerCase().replace("_", ".");
            world.playSound(
                loc,
                org.bukkit.Registry.SOUNDS.get(NamespacedKey.minecraft(key)),
                1.0f,
                1.0f
        } catch (Exception ignored) {}
    }

    // ── Utilidades para Amatista ─────────────────────────────────────────────

    private boolean consumeArrows(Player player, int count) {
        Inventory inv = player.getInventory();
        int totalArrows = 0;

        // Contar flechas totales (cualquier tipo de flecha)
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getType().name().contains("ARROW")) {
                totalArrows += item.getAmount();
            }
        }

        if (totalArrows < count) return false;

        int remainingToRemove = count;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType().name().contains("ARROW")) {
                if (item.getAmount() <= remainingToRemove) {
                    remainingToRemove -= item.getAmount();
                    inv.setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - remainingToRemove);
                    remainingToRemove = 0;
                }
                if (remainingToRemove <= 0) break;
            }
        }
        return true;
    }

    private Vector rotateY(Vector vector, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x = vector.getX() * cos - vector.getZ() * sin;
        double z = vector.getX() * sin + vector.getZ() * cos;
        return new Vector(x, vector.getY(), z);
    }
}
