package me.neabows.effects;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;

/**
 * Gestiona todas las partículas de NeaBows.
 *
 * Usa solo partículas disponibles en Paper 1.21.1 (post-rename 1.20.5+):
 *   FLAME, LAVA, LARGE_SMOKE, SMOKE, SNOWFLAKE, SOUL_FIRE_FLAME,
 *   ASH, WHITE_ASH, CLOUD, POOF, WARPED_SPORE, CRIMSON_SPORE,
 *   DUST (ex-REDSTONE, requiere DustOptions), BLOCK (requiere BlockData)
 */
public final class ParticleEffects {

    private ParticleEffects() {}

    // ── API pública ───────────────────────────────────────────────────────────

    /** Una partícula de rastro mientras la flecha está en vuelo. */
    public static void spawnTrail(Location loc, String powerName, int level) {
        spawn(loc, powerName, level, true);
    }

    /** Explosión de partículas en el punto de impacto. */
    public static void spawnImpact(Location loc, String powerName, int level) {
        spawn(loc, powerName, level, false);
    }

    // ── Lógica por poder ──────────────────────────────────────────────────────

    private static void spawn(
        Location loc,
        String powerName,
        int level,
        boolean trail
    ) {
        World w = loc.getWorld();
        if (w == null) return;

        switch (powerName.toLowerCase()) {
            // ── FUEGO ──────────────────────────────────────────────────────────
            case "fuego" -> {
                if (trail) {
                    w.spawnParticle(
                        Particle.FLAME,
                        loc,
                        4,
                        0.08,
                        0.08,
                        0.08,
                        0.02
                    );
                } else {
                    w.spawnParticle(
                        Particle.FLAME,
                        loc,
                        30 + level * 5,
                        0.4,
                        0.4,
                        0.4,
                        0.10
                    );
                    w.spawnParticle(
                        Particle.LAVA,
                        loc,
                        6 + level * 2,
                        0.3,
                        0.3,
                        0.3,
                        0.0
                    );
                    w.spawnParticle(
                        Particle.LARGE_SMOKE,
                        loc,
                        10,
                        0.3,
                        0.3,
                        0.3,
                        0.04
                    );
                }
            }
            // ── HIELO ──────────────────────────────────────────────────────────
            case "hielo" -> {
                Particle.DustOptions iceDust = new Particle.DustOptions(
                    Color.fromRGB(173, 216, 230),
                    1.2f
                );
                if (trail) {
                    w.spawnParticle(
                        Particle.SNOWFLAKE,
                        loc,
                        4,
                        0.08,
                        0.08,
                        0.08,
                        0.02
                    );
                } else {
                    w.spawnParticle(
                        Particle.SNOWFLAKE,
                        loc,
                        40 + level * 8,
                        0.5,
                        0.5,
                        0.5,
                        0.08
                    );
                    w.spawnParticle(
                        Particle.DUST,
                        loc,
                        15,
                        0.3,
                        0.3,
                        0.3,
                        0,
                        iceDust
                    );
                    w.spawnParticle(
                        Particle.WHITE_ASH,
                        loc,
                        10,
                        0.4,
                        0.4,
                        0.4,
                        0.03
                    );
                }
            }
            // ── MUERTOS ────────────────────────────────────────────────────────
            case "muertos" -> {
                if (trail) {
                    w.spawnParticle(
                        Particle.SOUL_FIRE_FLAME,
                        loc,
                        3,
                        0.08,
                        0.08,
                        0.08,
                        0.02
                    );
                    w.spawnParticle(Particle.ASH, loc, 3, 0.1, 0.1, 0.1, 0.02);
                } else {
                    w.spawnParticle(
                        Particle.SOUL_FIRE_FLAME,
                        loc,
                        25 + level * 5,
                        0.4,
                        0.4,
                        0.4,
                        0.07
                    );
                    w.spawnParticle(Particle.ASH, loc, 30, 0.5, 0.5, 0.5, 0.08);
                    w.spawnParticle(
                        Particle.LARGE_SMOKE,
                        loc,
                        12,
                        0.3,
                        0.3,
                        0.3,
                        0.04
                    );
                }
            }
            // ── VIENTO ─────────────────────────────────────────────────────────
            case "viento" -> {
                if (trail) {
                    w.spawnParticle(
                        Particle.CLOUD,
                        loc,
                        3,
                        0.08,
                        0.08,
                        0.08,
                        0.02
                    );
                } else {
                    w.spawnParticle(
                        Particle.CLOUD,
                        loc,
                        35 + level * 5,
                        0.5,
                        0.5,
                        0.5,
                        0.12
                    );
                    w.spawnParticle(
                        Particle.POOF,
                        loc,
                        15,
                        0.4,
                        0.4,
                        0.4,
                        0.08
                    );
                    w.spawnParticle(
                        Particle.WHITE_ASH,
                        loc,
                        20,
                        0.4,
                        0.4,
                        0.4,
                        0.04
                    );
                }
            }
            // ── ESCORPIÓN ──────────────────────────────────────────────────────
            case "escorpion" -> {
                Particle.DustOptions greenDust = new Particle.DustOptions(
                    Color.fromRGB(50, 205, 50),
                    1.3f
                );
                Particle.DustOptions darkGreen = new Particle.DustOptions(
                    Color.fromRGB(0, 128, 0),
                    1.0f
                );
                if (trail) {
                    w.spawnParticle(
                        Particle.DUST,
                        loc,
                        4,
                        0.08,
                        0.08,
                        0.08,
                        0,
                        greenDust
                    );
                } else {
                    w.spawnParticle(
                        Particle.DUST,
                        loc,
                        30 + level * 5,
                        0.4,
                        0.4,
                        0.4,
                        0,
                        greenDust
                    );
                    w.spawnParticle(
                        Particle.DUST,
                        loc,
                        15,
                        0.3,
                        0.3,
                        0.3,
                        0,
                        darkGreen
                    );
                    w.spawnParticle(
                        Particle.WARPED_SPORE,
                        loc,
                        20,
                        0.4,
                        0.4,
                        0.4,
                        0.05
                    );
                }
            }
            // ── AMATISTA ──────────────────────────────────────────────────────
            case "amatista" -> {
                Particle.DustOptions purpleDust = new Particle.DustOptions(
                    Color.fromRGB(153, 102, 204),
                    1.2f
                );
                if (trail) {
                    w.spawnParticle(
                        Particle.DUST,
                        loc,
                        3,
                        0.05,
                        0.05,
                        0.05,
                        0,
                        purpleDust
                    );
                    // Use AMETHYST_BLOCK as block dust for nice crystal effect
                    w.spawnParticle(
                        Particle.BLOCK,
                        loc,
                        2,
                        0.05,
                        0.05,
                        0.05,
                        0,
                        Material.AMETHYST_BLOCK.createBlockData()
                    );
                } else {
                    w.spawnParticle(
                        Particle.BLOCK,
                        loc,
                        35 + level * 5,
                        0.4,
                        0.4,
                        0.4,
                        0.1,
                        Material.AMETHYST_BLOCK.createBlockData()
                    );
                    w.spawnParticle(
                        Particle.DUST,
                        loc,
                        20,
                        0.3,
                        0.3,
                        0.3,
                        0,
                        purpleDust
                    );
                    w.spawnParticle(
                        Particle.FIREWORK,
                        loc,
                        10,
                        0.2,
                        0.2,
                        0.2,
                        0.1
                    );
                }
            }
            // ── HUESO ──────────────────────────────────────────────────────────
            case "hueso" -> {
                Particle.DustOptions boneDust = new Particle.DustOptions(
                    Color.fromRGB(245, 245, 220),
                    1.4f
                );
                if (trail) {
                    w.spawnParticle(
                        Particle.WHITE_ASH,
                        loc,
                        4,
                        0.08,
                        0.08,
                        0.08,
                        0.02
                    );
                } else {
                    w.spawnParticle(
                        Particle.WHITE_ASH,
                        loc,
                        40 + level * 5,
                        0.5,
                        0.5,
                        0.5,
                        0.07
                    );
                    w.spawnParticle(
                        Particle.DUST,
                        loc,
                        20,
                        0.3,
                        0.3,
                        0.3,
                        0,
                        boneDust
                    );
                    w.spawnParticle(
                        Particle.CLOUD,
                        loc,
                        10,
                        0.3,
                        0.3,
                        0.3,
                        0.04
                    );
                }
            }
            // ── TIERRA ─────────────────────────────────────────────────────────
            case "tierra" -> {
                Particle.DustOptions brownDust = new Particle.DustOptions(
                    Color.fromRGB(139, 69, 19),
                    1.5f
                );
                if (trail) {
                    w.spawnParticle(
                        Particle.BLOCK,
                        loc,
                        4,
                        0.08,
                        0.08,
                        0.08,
                        0.05,
                        Material.DIRT.createBlockData()
                    );
                } else {
                    w.spawnParticle(
                        Particle.BLOCK,
                        loc,
                        40 + level * 5,
                        0.5,
                        0.5,
                        0.5,
                        0.08,
                        Material.DIRT.createBlockData()
                    );
                    w.spawnParticle(
                        Particle.DUST,
                        loc,
                        20,
                        0.4,
                        0.4,
                        0.4,
                        0,
                        brownDust
                    );
                    w.spawnParticle(
                        Particle.LARGE_SMOKE,
                        loc,
                        10,
                        0.3,
                        0.3,
                        0.3,
                        0.04
                    );
                }
            }
            // ── Genérico (poder personalizado sin partícula específica) ─────────
            default -> {
                if (!trail) {
                    w.spawnParticle(
                        Particle.POOF,
                        loc,
                        20,
                        0.3,
                        0.3,
                        0.3,
                        0.08
                    );
                }
            }
        }
    }
}
