package me.neabows.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

/**
 * Utilidad centralizada para mensajes del plugin con soporte MiniMessage.
 */
public final class Messages {

    private Messages() {}

    private static final MiniMessage MM = MiniMessage.miniMessage();
    public static final String PREFIX = "<dark_gray>[<gradient:#FF6B35:#FF0000><bold>NeaBows</bold></gradient><dark_gray>]<reset> ";

    // ── Mensajes de error ─────────────────────────────────────────────────────

    public static final String NO_BOW =
        "<red>✗ <white>Debes tener un arco en la mano principal!";

    public static final String NO_PERMISSION =
        "<red>✗ <white>No tienes permiso para hacer esto.";

    public static final String POWER_NOT_FOUND =
        "<red>✗ <white>El poder <yellow>'{poder}'</yellow> no existe. Usa <yellow>/neabow list</yellow>.";

    public static final String INVALID_LEVEL =
        "<red>✗ <white>Nivel inválido. Debe ser entre <yellow>1</yellow> y <yellow>{max}</yellow>.";

    public static final String PLAYER_NOT_FOUND =
        "<red>✗ <white>El jugador <yellow>'{jugador}'</yellow> no está conectado.";

    public static final String NO_POWER_ON_BOW =
        "<yellow>⚠ <white>Este arco no tiene ningún poder asignado.";

    // ── Mensajes de éxito ─────────────────────────────────────────────────────

    public static final String POWER_SET =
        "<green>✦ <white>Poder <color:{color}><bold>{poder} {nivel}</bold></color> <white>aplicado al arco!";

    public static final String POWER_REMOVED =
        "<yellow>✦ <white>Poder <gray>removido</gray> del arco correctamente.";

    public static final String RELOAD_SUCCESS =
        "<green>✦ <white>Configuración recargada correctamente.";

    public static final String BOW_GIVEN =
        "<green>✦ <white>Arco con poder <color:{color}><bold>{poder}</bold></color> enviado a <yellow>{jugador}</yellow>.";

    public static final String BOW_RECEIVED =
        "<green>✦ <white>Recibiste un arco con poder <color:{color}><bold>{poder} {nivel}</bold></color><white>!";

    // ── Helpers ───────────────────────────────────────────────────────────────

    public static Component parse(String miniMessageText) {
        return MM.deserialize(miniMessageText);
    }

    public static Component prefixed(String miniMessageText) {
        return MM.deserialize(PREFIX + miniMessageText);
    }

    public static void send(CommandSender sender, String message) {
        sender.sendMessage(prefixed(message));
    }

    public static void sendRaw(CommandSender sender, String message) {
        sender.sendMessage(parse(message));
    }

    /**
     * Reemplaza un placeholder {key} con un valor en el mensaje.
     */
    public static String replace(String message, String key, String value) {
        return message.replace("{" + key + "}", value);
    }
}
