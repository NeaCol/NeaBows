package me.neabows.commands;

import me.neabows.NeaBows;
import me.neabows.managers.PowerManager;
import me.neabows.utils.ItemUtils;
import me.neabows.utils.Messages;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Comando principal: /neabow (alias: /nb)
 *
 * Subcomandos:
 *   set    <poder> [nivel]             → aplica poder al arco en mano
 *   remove                             → quita el poder del arco en mano
 *   list                               → lista todos los poderes disponibles
 *   give   <jugador> <poder> [nivel]   → da un arco con poder a un jugador
 *   info                               → muestra el poder del arco en mano
 *   reload                             → recarga configs (neabows.admin)
 */
public class NeaBowCommand implements CommandExecutor, TabCompleter {

    private final NeaBows plugin;
    private final MiniMessage mm;

    public NeaBowCommand(NeaBows plugin) {
        this.plugin = plugin;
        this.mm     = plugin.getMiniMessage();
    }

    // ── Ejecutar ──────────────────────────────────────────────────────────────

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("neabows.use")) {
            Messages.send(sender, Messages.NO_PERMISSION);
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "set"    -> handleSet(sender, args);
            case "remove" -> handleRemove(sender);
            case "list"   -> handleList(sender);
            case "give"   -> handleGive(sender, args);
            case "info"   -> handleInfo(sender);
            case "reload" -> handleReload(sender);
            default       -> sendHelp(sender);
        }
        return true;
    }

    // ── /neabow set <poder> [nivel] ───────────────────────────────────────────

    private void handleSet(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            Messages.send(sender, "<red>✗ Solo se puede usar en juego.");
            return;
        }
        if (args.length < 2) {
            Messages.send(sender, "<red>✗ Uso: <yellow>/neabow set <poder> [nivel]");
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!ItemUtils.isBow(item)) {
            Messages.send(sender, Messages.NO_BOW);
            return;
        }

        String powerName = args[1].toLowerCase();
        PowerManager pm  = plugin.getPowerManager();

        if (!pm.powerExists(powerName)) {
            Messages.send(sender, Messages.replace(Messages.POWER_NOT_FOUND, "poder", args[1]));
            return;
        }
        if (!pm.isEnabled(powerName)) {
            Messages.send(sender, "<red>✗ El poder <yellow>" + args[1] + "</yellow> está deshabilitado.");
            return;
        }

        int level = 1;
        if (args.length >= 3) {
            try {
                level = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                Messages.send(sender, "<red>✗ El nivel debe ser un número entero.");
                return;
            }
        }

        int maxLevel = pm.getMaxLevel(powerName);
        if (level < 1 || level > maxLevel) {
            Messages.send(sender, Messages.replace(Messages.INVALID_LEVEL, "max", String.valueOf(maxLevel)));
            return;
        }

        // Aplicar poder (preserva todo el NBT existente)
        ItemUtils.applyPower(item, powerName, level);

        String color       = pm.getPowerColor(powerName);
        String displayName = pm.getPowerDisplayName(powerName);
        String msg = Messages.replace(
            Messages.replace(
                Messages.replace(Messages.POWER_SET, "color", color),
                "poder", displayName),
            "nivel", ItemUtils.toRoman(level));
        Messages.send(sender, msg);
    }

    // ── /neabow remove ────────────────────────────────────────────────────────

    private void handleRemove(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            Messages.send(sender, "<red>✗ Solo se puede usar en juego.");
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!ItemUtils.isBow(item)) {
            Messages.send(sender, Messages.NO_BOW);
            return;
        }
        if (!ItemUtils.hasPower(item)) {
            Messages.send(sender, Messages.NO_POWER_ON_BOW);
            return;
        }

        ItemUtils.removePower(item);
        Messages.send(sender, Messages.POWER_REMOVED);
    }

    // ── /neabow list ──────────────────────────────────────────────────────────

    private void handleList(CommandSender sender) {
        PowerManager pm     = plugin.getPowerManager();
        List<String> powers = pm.getAvailablePowers();

        sender.sendMessage(mm.deserialize("<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        sender.sendMessage(mm.deserialize(
            "<gradient:#FF6B35:#FF0000><bold>  ⚡ NeaBows — Poderes Disponibles</bold></gradient>"
        ));
        sender.sendMessage(mm.deserialize("<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));

        for (String power : powers) {
            String color       = pm.getPowerColor(power);
            String displayName = pm.getPowerDisplayName(power);
            int    maxLevel    = pm.getMaxLevel(power);
            sender.sendMessage(mm.deserialize(
                " <color:" + color + ">◆</color> <white>" + displayName + "</white>"
                + " <dark_gray>(<gray>niveles: <yellow>1–" + maxLevel + "</yellow></gray>)</dark_gray>"
                + "  <dark_gray>»</dark_gray> <gray>/neabow set <yellow>" + power
                + "</yellow> <yellow>[1–" + maxLevel + "]</yellow>"
            ));
        }

        sender.sendMessage(mm.deserialize("<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    // ── /neabow give <jugador> <poder> [nivel] ────────────────────────────────

    private void handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("neabows.give")) {
            Messages.send(sender, Messages.NO_PERMISSION);
            return;
        }
        if (args.length < 3) {
            Messages.send(sender, "<red>✗ Uso: <yellow>/neabow give <jugador> <poder> [nivel]");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            Messages.send(sender, Messages.replace(Messages.PLAYER_NOT_FOUND, "jugador", args[1]));
            return;
        }

        String powerName = args[2].toLowerCase();
        PowerManager pm  = plugin.getPowerManager();

        if (!pm.powerExists(powerName)) {
            Messages.send(sender, Messages.replace(Messages.POWER_NOT_FOUND, "poder", args[2]));
            return;
        }

        int level = 1;
        if (args.length >= 4) {
            try {
                level = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                Messages.send(sender, "<red>✗ El nivel debe ser un número entero.");
                return;
            }
        }

        int maxLevel = pm.getMaxLevel(powerName);
        if (level < 1 || level > maxLevel) {
            Messages.send(sender, Messages.replace(Messages.INVALID_LEVEL, "max", String.valueOf(maxLevel)));
            return;
        }

        // Crear arco nuevo limpio con el poder
        ItemStack poweredBow = ItemUtils.createPoweredBow(powerName, level);
        target.getInventory().addItem(poweredBow);

        String color       = pm.getPowerColor(powerName);
        String displayName = pm.getPowerDisplayName(powerName);
        String roman       = ItemUtils.toRoman(level);

        Messages.send(sender,
            Messages.replace(
                Messages.replace(
                    Messages.replace(Messages.BOW_GIVEN, "color", color),
                    "poder", displayName),
                "jugador", target.getName())
        );
        Messages.send(target,
            Messages.replace(
                Messages.replace(
                    Messages.replace(Messages.BOW_RECEIVED, "color", color),
                    "poder", displayName),
                "nivel", roman)
        );
    }

    // ── /neabow info ──────────────────────────────────────────────────────────

    private void handleInfo(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            Messages.send(sender, "<red>✗ Solo se puede usar en juego.");
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!ItemUtils.isBow(item)) {
            Messages.send(sender, Messages.NO_BOW);
            return;
        }
        if (!ItemUtils.hasPower(item)) {
            Messages.send(sender, Messages.NO_POWER_ON_BOW);
            return;
        }

        PowerManager pm    = plugin.getPowerManager();
        String powerName   = ItemUtils.getPower(item);
        int    level       = ItemUtils.getLevel(item);
        String color       = pm.getPowerColor(powerName);
        String displayName = pm.getPowerDisplayName(powerName);
        String desc        = pm.getLevelDescription(powerName, level);
        int    maxLevel    = pm.getMaxLevel(powerName);

        sender.sendMessage(mm.deserialize("<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        sender.sendMessage(mm.deserialize(
            " <white>⚡ <color:" + color + "><bold>Poder de Arco</bold></color>"
            + "  <dark_gray>◆ <color:" + color + ">" + displayName + " " + ItemUtils.toRoman(level) + "</color>"
        ));
        sender.sendMessage(mm.deserialize(
            " <gray>▸ <italic>" + desc + "</italic>"
        ));
        sender.sendMessage(mm.deserialize(
            " <gray>Nivel " + level + " de " + maxLevel
        ));
        // Mostrar cooldown restante
        double remaining = plugin.getCooldownManager()
                                 .getRemainingSeconds(player.getUniqueId(), powerName);
        if (remaining > 0) {
            sender.sendMessage(mm.deserialize(
                " <gray>Cooldown: <red>" + String.format("%.1f", remaining) + "s"
            ));
        } else {
            sender.sendMessage(mm.deserialize(" <gray>Cooldown: <green>Listo ✔"));
        }
        sender.sendMessage(mm.deserialize("<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    // ── /neabow reload ────────────────────────────────────────────────────────

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("neabows.admin")) {
            Messages.send(sender, Messages.NO_PERMISSION);
            return;
        }
        plugin.reloadPluginConfig();
        Messages.send(sender, Messages.RELOAD_SUCCESS);
    }

    // ── Ayuda ─────────────────────────────────────────────────────────────────

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(mm.deserialize("<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        sender.sendMessage(mm.deserialize(
            "<gradient:#FF6B35:#FF0000><bold>  ⚡ NeaBows — Comandos</bold></gradient>"
        ));
        sender.sendMessage(mm.deserialize("<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        sender.sendMessage(mm.deserialize(" <yellow>/neabow set <poder> [nivel]</yellow> <dark_gray>»</dark_gray> <gray>Aplica un poder al arco en mano"));
        sender.sendMessage(mm.deserialize(" <yellow>/neabow remove</yellow>               <dark_gray>»</dark_gray> <gray>Quita el poder del arco en mano"));
        sender.sendMessage(mm.deserialize(" <yellow>/neabow list</yellow>                 <dark_gray>»</dark_gray> <gray>Muestra todos los poderes disponibles"));
        sender.sendMessage(mm.deserialize(" <yellow>/neabow info</yellow>                 <dark_gray>»</dark_gray> <gray>Muestra el poder del arco que tienes"));
        sender.sendMessage(mm.deserialize(" <yellow>/neabow give <jugador> <poder> [nivel]</yellow> <dark_gray>»</dark_gray> <gray>Da un arco con poder"));
        sender.sendMessage(mm.deserialize(" <yellow>/neabow reload</yellow>               <dark_gray>»</dark_gray> <gray>Recarga la configuración"));
        sender.sendMessage(mm.deserialize("<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    // ── Tab Completion ────────────────────────────────────────────────────────

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd,
                                      String alias, String[] args) {
        if (!sender.hasPermission("neabows.use")) return List.of();

        // args[0] → subcomando
        if (args.length == 1) {
            return filterStart(
                List.of("set", "remove", "list", "give", "info", "reload"),
                args[0]
            );
        }

        String sub = args[0].toLowerCase();

        // args[1] según subcomando
        if (args.length == 2) {
            if (sub.equals("set") || sub.equals("give")) {
                // "set" → poderes | "give" → jugadores
                if (sub.equals("give")) {
                    return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                }
                return filterStart(plugin.getPowerManager().getAvailablePowers(), args[1]);
            }
        }

        // args[2]: para "set" → nivel | para "give" → poder
        if (args.length == 3) {
            if (sub.equals("set")) {
                return levelCompletions(args[1], args[2]);
            }
            if (sub.equals("give")) {
                return filterStart(plugin.getPowerManager().getAvailablePowers(), args[2]);
            }
        }

        // args[3]: para "give" → nivel
        if (args.length == 4 && sub.equals("give")) {
            return levelCompletions(args[2], args[3]);
        }

        return List.of();
    }

    // ── Helpers de tab ────────────────────────────────────────────────────────

    private List<String> filterStart(List<String> options, String prefix) {
        return options.stream()
            .filter(o -> o.toLowerCase().startsWith(prefix.toLowerCase()))
            .collect(Collectors.toList());
    }

    private List<String> levelCompletions(String powerName, String prefix) {
        if (!plugin.getPowerManager().powerExists(powerName.toLowerCase())) return List.of();
        int max = plugin.getPowerManager().getMaxLevel(powerName.toLowerCase());
        List<String> levels = new ArrayList<>();
        for (int i = 1; i <= max; i++) levels.add(String.valueOf(i));
        return filterStart(levels, prefix);
    }
}
