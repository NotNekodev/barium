package notnekodev.barium.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.handler.configuration.LifecycleEventHandlerConfiguration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import notnekodev.barium.Barium;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Implementation of the {@code /bal} command that can be run my players
 */
public class BalanceCommand {

    /**
     * Creates the {@code /bal} command
     * @return {@code LiteralArgumentBuilder<CommandSourceStack>} that can be registered using
     *         {@link io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager#registerEventHandler(LifecycleEventHandlerConfiguration)}
     */
    public static LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("bal")
                .requires(sender -> sender.getSender().hasPermission("barium.economy.balance"))
                .executes(BalanceCommand::handleBalCommand);
    }

    /**
     * Handles the {@code /bal} command that a players can run
     * @param ctx {@link CommandContext} given by Paper on command invokation
     * @return {@link Command#SINGLE_SUCCESS}
     */
    private static int handleBalCommand(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getSender() instanceof Player target)) {
            ctx.getSource().getSender().sendRichMessage("<red>Only players can run this command!");
            return Command.SINGLE_SUCCESS;
        }

        UUID targetUUID = target.getUniqueId();
        long balance = Barium.INSTANCE.economyService.getBalance(targetUUID);

        ctx.getSource().getSender().sendRichMessage("Your current balance is <gold><balance><currency_sign>",
                Placeholder.component("balance", Component.text().content(Long.toString(balance))),
                Placeholder.component("currency_sign",
                        Component.text().content(Barium.INSTANCE.getConfig().getString("currency.symbol", "$")))
        );

        return Command.SINGLE_SUCCESS;
    }
}
