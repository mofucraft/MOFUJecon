package jp.jyn.mecon.command;

import jp.jyn.jbukkitlib.command.SubCommand;
import jp.jyn.jbukkitlib.config.parser.template.variable.StringVariable;
import jp.jyn.jbukkitlib.config.parser.template.variable.TemplateVariable;
import jp.jyn.jbukkitlib.uuid.UUIDRegistry;
import jp.jyn.mecon.repository.AbstractRepository;
import jp.jyn.mecon.repository.BalanceRepository;
import jp.jyn.mecon.config.MessageConfig;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

public class Top extends SubCommand {
    private final static int ENTRY_PER_PAGE = 10;
    private final MessageConfig message;
    private final UUIDRegistry registry;
    private final BalanceRepository repository;

    public Top(MessageConfig message, UUIDRegistry registry, BalanceRepository repository) {
        this.message = message;
        this.registry = registry;
        this.repository = repository;
    }

    @Override
    protected Result onCommand(CommandSender sender, Queue<String> args) {
        final int page;
        try {
            page = args.isEmpty() ? 1 : Integer.parseInt(args.element());
        } catch (NumberFormatException e) {
            sender.sendMessage(message.invalidArgument.toString("value", args.element()));
            return Result.ERROR;
        }

        int offset = (page - 1) * ENTRY_PER_PAGE;

        TemplateVariable variable = StringVariable.init().put("page", page);
        sender.sendMessage(message.topFirst.toString(variable));

        // Use topWithNames() to get player names directly from database
        // This is more efficient and reliable for Minecraft 1.21.4+
        if (repository instanceof AbstractRepository) {
            Map<UUID, Object[]> topWithNames = ((AbstractRepository) repository).topWithNames(ENTRY_PER_PAGE, offset);

            int i = offset;
            for (Map.Entry<UUID, Object[]> entry : topWithNames.entrySet()) {
                String playerName = (String) entry.getValue()[0]; // Name from database
                BigDecimal balance = (BigDecimal) entry.getValue()[1]; // Balance

                // If name is null, try to fetch it from Bukkit and update database
                if (playerName == null || playerName.isEmpty()) {
                    UUID uuid = entry.getKey();

                    // Try to get name from OfflinePlayer
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                    String fetchedName = offlinePlayer.getName();

                    if (fetchedName != null && !fetchedName.isEmpty()) {
                        playerName = fetchedName;
                        // Update database with the fetched name
                        ((AbstractRepository) repository).updatePlayerName(uuid, fetchedName);
                    } else {
                        // Final fallback to UUID string
                        playerName = uuid.toString();
                    }
                }

                variable.put("name", playerName);
                variable.put("uuid", entry.getKey()); // Secret variable
                variable.put("balance", repository.format(balance));
                variable.put("rank", ++i);
                sender.sendMessage(message.topEntry.toString(variable));
            }
        } else {
            // Fallback to old method if not AbstractRepository
            Map<UUID, BigDecimal> top = repository.top(ENTRY_PER_PAGE, offset);

            int i = offset;
            for (Map.Entry<UUID, BigDecimal> entry : top.entrySet()) {
                variable.put("name", entry.getKey().toString());
                variable.put("uuid", entry.getKey()); // Secret variable
                variable.put("balance", repository.format(entry.getValue()));
                variable.put("rank", ++i);
                sender.sendMessage(message.topEntry.toString(variable));
            }
        }

        return Result.OK;
    }

    @Override
    protected List<String> onTabComplete(CommandSender sender, Deque<String> args) {
        return Collections.emptyList();
    }

    @Override
    protected String requirePermission() {
        return "mecon.top";
    }

    @Override
    public CommandHelp getHelp() {
        return new CommandHelp(
            "/money top [page]",
            message.help.top.toString(),
            "/money top",
            "/money top 1"
        );
    }
}
