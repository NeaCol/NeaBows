package me.neabows;

import me.neabows.commands.NeaBowCommand;
import me.neabows.effects.ArrowTracker;
import me.neabows.listeners.BowListener;
import me.neabows.managers.CooldownManager;
import me.neabows.managers.PowerManager;
import me.neabows.utils.ItemUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;

public final class NeaBows extends JavaPlugin {

    private static NeaBows instance;

    private PowerManager powerManager;
    private CooldownManager cooldownManager;
    private ArrowTracker arrowTracker;

    private final MiniMessage mm = MiniMessage.miniMessage();

    @Override
    public void onEnable() {
        instance = this;

        // Guardar configs por defecto si no existen aún
        saveDefaultConfig();
        saveResource("poderes.yml", false);

        // Inicializar claves PDC (necesitan la instancia del plugin)
        ItemUtils.init(this);

        // Inicializar managers
        this.powerManager = new PowerManager(this);
        this.cooldownManager = new CooldownManager(this);
        this.arrowTracker = new ArrowTracker(this);

        // Registrar listener de eventos
        getServer()
            .getPluginManager()
            .registerEvents(new BowListener(this), this);

        // Registrar comando /neabow (alias: /nb)
        NeaBowCommand cmd = new NeaBowCommand(this);
        getCommand("neabow").setExecutor(cmd);
        getCommand("neabow").setTabCompleter(cmd);

        // Iniciar tarea de rastro de flechas
        arrowTracker.startTracking();

        getServer()
            .getConsoleSender()
            .sendMessage(
                mm.deserialize(
                    "<dark_gray>[<gradient:#FF6B35:#FF0000><bold>NeaBows</bold></gradient><dark_gray>]" +
                        " <green>✦ Activado correctamente — v" +
                        getDescription().getVersion()
                )
            );
    }

    @Override
    public void onDisable() {
        if (arrowTracker != null) arrowTracker.stopTracking();

        getServer()
            .getConsoleSender()
            .sendMessage(
                mm.deserialize(
                    "<dark_gray>[<gradient:#FF6B35:#FF0000><bold>NeaBows</bold></gradient><dark_gray>]" +
                        " <red>✦ Desactivado."
                )
            );
    }

    /** Recarga configs y reinicia componentes que dependen de ellas. */
    public void reloadPluginConfig() {
        reloadConfig();
        powerManager.reload();
        arrowTracker.stopTracking();
        arrowTracker.startTracking();
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public static NeaBows getInstance() {
        return instance;
    }

    public PowerManager getPowerManager() {
        return powerManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public ArrowTracker getArrowTracker() {
        return arrowTracker;
    }

    public MiniMessage getMiniMessage() {
        return mm;
    }
}
