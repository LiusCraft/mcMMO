package com.gmail.nossr50.commands.chat;

import com.gmail.nossr50.commands.ToggleCommand;
import com.gmail.nossr50.datatypes.player.BukkitMMOPlayer;
import com.gmail.nossr50.mcMMO;
import org.bukkit.command.CommandSender;

public class ChatSpyCommand extends ToggleCommand {

    public ChatSpyCommand(mcMMO pluginRef) {
        super(pluginRef);
    }

    @Override
    protected boolean hasOtherPermission(CommandSender sender) {
        return pluginRef.getPermissionTools().adminChatSpyOthers(sender);
    }

    @Override
    protected boolean hasSelfPermission(CommandSender sender) {
        return pluginRef.getPermissionTools().adminChatSpy(sender);
    }

    @Override
    protected void applyCommandAction(BukkitMMOPlayer mcMMOPlayer) {
        mcMMOPlayer.getNative().sendMessage(pluginRef.getLocaleManager().getString("Commands.AdminChatSpy." + (mcMMOPlayer.isPartyChatSpying() ? "Disabled" : "Enabled")));
        mcMMOPlayer.togglePartyChatSpying();
    }

    @Override
    protected void sendSuccessMessage(CommandSender sender, String playerName) {
        sender.sendMessage(pluginRef.getLocaleManager().getString("Commands.AdminChatSpy.Toggle", playerName));
    }
}
