package jp.jyn.mecon.command;

import jp.jyn.jbukkitlib.command.SubCommand;
import jp.jyn.mecon.Mecon;
import jp.jyn.mecon.VersionChecker;
import jp.jyn.mecon.config.MessageConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.Queue;

public class Version extends SubCommand {
    private final MessageConfig message;
    private final VersionChecker checker;
    private final PluginDescriptionFile description;

    public Version(MessageConfig message, VersionChecker checker) {
        this.message = message;
        this.checker = checker;
        this.description = Mecon.getInstance().getDescription();
    }

    @Override
    protected Result onCommand(CommandSender sender, Queue<String> args) {
        sender.sendMessage(MessageConfig.HEADER);
        sender.sendMessage(description.getName() + " - " + description.getVersion());
        sender.sendMessage(description.getDescription());
        sender.sendMessage("Developer: " + String.join(",", description.getAuthors()));
        sender.sendMessage("SourceCode: " + description.getWebsite());
        checker.check(sender);
        return Result.OK;
    }

    @Override
    protected String requirePermission() {
        return "mecon.version";
    }

    @Override
    public CommandHelp getHelp() {
        return new CommandHelp(
            "/money version",
            message.help.version.toString()
        );
    }
}
