package notnekodev.barium.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.handler.configuration.LifecycleEventHandlerConfiguration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import notnekodev.barium.Barium;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of the {@code /transfer <player> <amount>}
 */
public class TransferCommand {

    /**
     * Creates the {@code /transfer <player> <amount>} command
     * @return {@code LiteralArgumentBuilder<CommandSourceStack>} that can be registered using
     *         {@link io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager#registerEventHandler(LifecycleEventHandlerConfiguration)}
     */
    public static LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("transfer")
                .requires(sender -> sender.getSender().hasPermission("barium.economy.transfer"))
                .then(Commands.argument("player", ArgumentTypes.entity())
                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                .executes(TransferCommand::handleTransferCommand))
                        .executes(ctx -> {
                            ctx.getSource().getSender().sendRichMessage("<red>Usage: /transfer <player> <amount>");
                            return Command.SINGLE_SUCCESS;
                        }))

                .executes(ctx -> {
                    ctx.getSource().getSender().sendRichMessage("<red>Usage: /transfer <player> <amount>");
                    return Command.SINGLE_SUCCESS;
                });
    }

    /**
     * Handles the {@code /transfer <player> <amount>} command that a players can run
     * @param ctx {@link CommandContext} given by Paper on command invokation
     * @return {@link Command#SINGLE_SUCCESS}
     * @throws CommandSyntaxException can be thrown by {@link CommandContext#getArgument(String, Class)}
     */
    private static int handleTransferCommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final EntitySelectorArgumentResolver entitySelectorArgumentResolver = ctx.getArgument("player", EntitySelectorArgumentResolver.class);
        final List<Entity> entities = entitySelectorArgumentResolver.resolve(ctx.getSource());
        final int amount = ctx.getArgument("amount", int.class);

        if (!(ctx.getSource().getSender() instanceof Player target)) {
            ctx.getSource().getSender().sendRichMessage("<red>Only players can run this command!");
            return 0;
        }

        if (!(entities.getFirst() instanceof Player receiver)) {
            ctx.getSource().getSender().sendRichMessage("<red>You can only send money to other players!");
            return 0;
        }

        UUID targetUUID = target.getUniqueId();
        UUID receiverUUID = receiver.getUniqueId();

        if (targetUUID.equals(receiverUUID)) {
            ctx.getSource().getSender().sendRichMessage("<red>You can't transfer money to yourself!");
            return 0;
        }

        final long balance = Barium.INSTANCE.economyService.getBalance(targetUUID);

        if (amount <= 0) {
            ctx.getSource().getSender().sendRichMessage("<red>The transferred amount has to be at least 1<currency_sign>!",
                    Placeholder.component("currency_sign",
                            Component.text().content(Barium.INSTANCE.getConfig().getString("currency.symbol", "$"))));
            return 0;
        }

        int ret = Barium.INSTANCE.economyService.transfer(targetUUID, receiverUUID, amount);
        switch (ret) {
            case 0:
                break;
            case 1:
                ctx.getSource().getSender().sendRichMessage("<red>Transfer failed due to internal error. Code: EcoServiceTransferInvalidUUID");
                Barium.LOGGER.error("Invalid player UUIDs during transfer! Invoker UUID: \"{}\", Target UUID: \"{}\"", targetUUID, receiverUUID);
                return 0;
            case 2:
                ctx.getSource().getSender().sendRichMessage("<red>You do not have enough <currency_name>(s)! " +
                        "You need <gold><needed_amount><currency_sign><red> but you only have <gold><actual_amount><currency_sign><red>" +
                                " (<gold><difference><currency_sign><red> missing)",
                        Placeholder.component("currency_name",
                                Component.text().content(Barium.INSTANCE.getConfig().getString("currency.name", "Dollar"))),
                        Placeholder.component("needed_amount", Component.text().content(Long.toString(amount))),
                        Placeholder.component("currency_sign",
                                Component.text().content(Barium.INSTANCE.getConfig().getString("currency.symbol", "$"))),
                        Placeholder.component("actual_amount", Component.text().content(Long.toString(balance))),
                        Placeholder.component("difference", Component.text().content(Long.toString(amount - balance)))
                );
                return 0;
            case 3:
                ctx.getSource().getSender().sendRichMessage("<red>The transferred amount has to be at least 1<currency_sign>!",
                        Placeholder.component("currency_sign",
                                Component.text().content(Barium.INSTANCE.getConfig().getString("currency.symbol", "$"))));
                return 0;
        }

        ctx.getSource().getSender().sendRichMessage("You transferred <gold><amount><currency_sign> to <green><receiver>",
                Placeholder.component("amount", Component.text().content(Long.toString(amount))),
                Placeholder.component("currency_sign",
                        Component.text().content(Barium.INSTANCE.getConfig().getString("currency.symbol", "$"))),
                Placeholder.component("receiver", Component.text().content(receiver.getName()))
        );

        receiver.sendRichMessage("<green><sender><white> just send you <gold><amount><currency_sign>",
                Placeholder.component("sender", Component.text().content(target.getName())),
                Placeholder.component("amount", Component.text().content(Long.toString(amount))),
                Placeholder.component("currency_sign",
                        Component.text().content(Barium.INSTANCE.getConfig().getString("currency.symbol", "$")))
        );

        return Command.SINGLE_SUCCESS;
    }
}
