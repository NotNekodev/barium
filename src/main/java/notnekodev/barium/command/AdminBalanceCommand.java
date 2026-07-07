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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * {@code /eco} administrative command, intended to be used by players with the correct permissions or server operators
 */
public class AdminBalanceCommand {

    /**
     * Creates the {@code /eco} command tree with arguments and installs the following handlers. For specifics refer to the Paper documentation
     * @return {@code LiteralArgumentBuilder<CommandSourceStack>} that can be registered using
     *         {@link io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager#registerEventHandler(LifecycleEventHandlerConfiguration)}
     */
    public static LiteralArgumentBuilder<CommandSourceStack> createCommand() {

        // create root command (/eco) and send a detailed response if run without a subcommand.
        // This always shows if the player has any of the 4 admin command permissions, but it will only show the to the player available subcommands
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("eco")
                .requires(sender -> sender.getSender().hasPermission("barium.economy.admin.get") ||
                        sender.getSender().hasPermission("barium.economy.admin.set") ||
                        sender.getSender().hasPermission("barium.economy.admin.add") ||
                        sender.getSender().hasPermission("barium.economy.admin.remove"))
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();

                    List<String> available = new ArrayList<>();

                    if (sender.hasPermission("barium.economy.admin.get")) {
                        available.add("get");
                    }
                    if (sender.hasPermission("barium.economy.admin.set")) {
                        available.add("set");
                    }
                    if (sender.hasPermission("barium.economy.admin.add")) {
                        available.add("add");
                    }
                    if (sender.hasPermission("barium.economy.admin.remove")) {
                        available.add("remove");
                    }

                    if (available.isEmpty()) {
                        sender.sendMessage(Component.text("<red>Usage: /eco (no available subcommands)"));
                        return Command.SINGLE_SUCCESS;
                    }

                    String usage = "Usage: /eco <" + String.join("|", available) + ">";

                    sender.sendMessage(Component.text(usage));

                    return Command.SINGLE_SUCCESS;
                });

        // registers the /eco get command, see #handleGetSubcommand for more info
        root.then(Commands.literal("get")
                        .requires(sender -> sender.getSender().hasPermission("barium.economy.admin.get"))
                        .then(Commands.argument("player", ArgumentTypes.entity())
                                .executes(AdminBalanceCommand::handleGetSubcommand))
                        .executes(ctx -> {
                            ctx.getSource().getSender().sendRichMessage("<red>Usage: /eco get <player>");
                            return Command.SINGLE_SUCCESS;
                        })
        );

        // registers the /eco set command, see #handleSetSubcommand for more info
        root.then(Commands.literal("set")
                .requires(sender -> sender.getSender().hasPermission("barium.economy.admin.set"))
                .then(Commands.argument("player", ArgumentTypes.entity())
                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                            .executes(AdminBalanceCommand::handleSetSubcommand))
                        .executes(ctx -> {
                            ctx.getSource().getSender().sendRichMessage("<red>Usage: /eco set <player> <amount>");
                            return Command.SINGLE_SUCCESS;
                        }))

                .executes(ctx -> {
                    ctx.getSource().getSender().sendRichMessage("<red>Usage: /eco set <player> <amount>");
                    return Command.SINGLE_SUCCESS;
                })
        );

        // registers the /eco add command, see #handleAddSubcommand for more info
        root.then(Commands.literal("add")
                .requires(sender -> sender.getSender().hasPermission("barium.economy.admin.add"))
                .then(Commands.argument("player", ArgumentTypes.entity())
                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                .executes(AdminBalanceCommand::handleAddSubcommand))
                        .executes(ctx -> {
                            ctx.getSource().getSender().sendRichMessage("<red>Usage: /eco add <player> <amount>");
                            return Command.SINGLE_SUCCESS;
                        }))

                .executes(ctx -> {
                    ctx.getSource().getSender().sendRichMessage("<red>Usage: /eco add <player> <amount>");
                    return Command.SINGLE_SUCCESS;
                })
        );

        // registers the /eco remove command, see #handleRemoveSubcommand for more info
        root.then(Commands.literal("remove")
                .requires(sender -> sender.getSender().hasPermission("barium.economy.admin.remove"))
                .then(Commands.argument("player", ArgumentTypes.entity())
                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                .executes(AdminBalanceCommand::handleRemoveSubcommand))
                        .executes(ctx -> {
                            ctx.getSource().getSender().sendRichMessage("<red>Usage: /eco remove <player> <amount>");
                            return Command.SINGLE_SUCCESS;
                        }))

                .executes(ctx -> {
                    ctx.getSource().getSender().sendRichMessage("<red>Usage: /eco remove <player> <amount>");
                    return Command.SINGLE_SUCCESS;
                })
        );

        return root;
    }

    /**
     * Handles the {@code /eco get <player>} subcommand, registered in {@link #createCommand()}
     * @param ctx {@link CommandContext} given by Paper on command invokation
     * @return {@link Command#SINGLE_SUCCESS}, don't ask me, ask Mojang
     * @throws CommandSyntaxException can be thrown by {@link CommandContext#getArgument(String, Class)}
     */
    private static int handleGetSubcommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final EntitySelectorArgumentResolver entitySelectorArgumentResolver = ctx.getArgument("player", EntitySelectorArgumentResolver.class);
        final List<Entity> entities = entitySelectorArgumentResolver.resolve(ctx.getSource());

        // if the entity isnt a player, this wont work
        if (!(entities.getFirst() instanceof Player target)) {
            ctx.getSource().getSender().sendRichMessage("<red>This command can only be run on players!");
            return Command.SINGLE_SUCCESS;
        }

        // get the UUID of the player
        UUID targetUUID = target.getUniqueId();

        // interact with the economy service
        long bal = Barium.INSTANCE.economyService.getBalance(targetUUID);

        // send an update message to the sender, with the new balance
        ctx.getSource().getSender().sendRichMessage("Balance of <green><entityname><white> is <gold><balance><currencysign>",
                Placeholder.component("entityname", entities.getFirst().name()),
                Placeholder.component("balance", Component.text().content(Long.toString(bal))),
                Placeholder.component("currencysign",
                        Component.text().content(Barium.INSTANCE.getConfig().getString("currency.symbol", "$")))
        );

        return Command.SINGLE_SUCCESS;
    }

    /**
     * Handles the {@code /eco set <player> <amount>} subcommand registered in {@link #createCommand()}
     * @param ctx {@link CommandContext} given by Paper on command invokation
     * @return {@link Command#SINGLE_SUCCESS}
     * @throws CommandSyntaxException can be thrown by {@link CommandContext#getArgument(String, Class)}
     */
    private static int handleSetSubcommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final EntitySelectorArgumentResolver entitySelectorArgumentResolver = ctx.getArgument("player", EntitySelectorArgumentResolver.class);
        final List<Entity> entities = entitySelectorArgumentResolver.resolve(ctx.getSource());
        final int amount = ctx.getArgument("amount", int.class);

        // again, this command can only be run on players
        if (!(entities.getFirst() instanceof Player target)) {
            ctx.getSource().getSender().sendRichMessage("<red>This command can only be run on players!");
            return Command.SINGLE_SUCCESS;
        }

        // get the uuid and current balance
        UUID targetUUID = target.getUniqueId();
        long balance = Barium.INSTANCE.economyService.getBalance(targetUUID);

        // update the balance using the economy service
        Barium.INSTANCE.economyService.setBalance(targetUUID, amount);

        // send a detailed message to the sender
        ctx.getSource().getSender().sendRichMessage(
                "Updated balance <green><entityname><white> from <gold><balance><currencysign><white> to <gold><newbalance><currencysign>",
                Placeholder.component("entityname", entities.getFirst().name()),
                Placeholder.component("balance", Component.text().content(Long.toString(balance))),
                Placeholder.component("currencysign",
                        Component.text().content(Barium.INSTANCE.getConfig().getString("currency.symbol", "$"))),
                Placeholder.component("newbalance", Component.text().content(Integer.toString(amount)))
        );

        return Command.SINGLE_SUCCESS;
    }

    /**
     * Handles the {@code /eco add <player> <amount>} subcommand registered in {@link #createCommand()}
     * @param ctx {@link CommandContext} given by Paper on command invokation
     * @return {@link Command#SINGLE_SUCCESS}
     * @throws CommandSyntaxException can be thrown by {@link CommandContext#getArgument(String, Class)}
     */
    private static int handleAddSubcommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final EntitySelectorArgumentResolver entitySelectorArgumentResolver = ctx.getArgument("player", EntitySelectorArgumentResolver.class);
        final List<Entity> entities = entitySelectorArgumentResolver.resolve(ctx.getSource());
        final int amount = ctx.getArgument("amount", int.class);

        // target must be a player
        if (!(entities.getFirst() instanceof Player target)) {
            ctx.getSource().getSender().sendRichMessage("<red>This command can only be run on players!");
            return Command.SINGLE_SUCCESS;
        }

        // get the players uuid
        UUID targetUUID = target.getUniqueId();

        // update the players balance using the economy service
        Barium.INSTANCE.economyService.addBalance(targetUUID, amount);

        // send a detailed response to the sender
        ctx.getSource().getSender().sendRichMessage(
                "Added <gold><amount><currencysign><white> to <green><entityname><white>. New balance is " +
                        "<gold><newbalance><currencysign>",
                Placeholder.component("entityname", entities.getFirst().name()),
                Placeholder.component("currencysign",
                        Component.text().content(Barium.INSTANCE.getConfig().getString("currency.symbol", "$"))),
                Placeholder.component("newbalance",
                        Component.text().content(Long.toString( Barium.INSTANCE.economyService.getBalance(targetUUID)))),
                Placeholder.component("amount", Component.text().content(Integer.toString(amount)))
        );

        return Command.SINGLE_SUCCESS;
    }

    /**
     * Handles the {@code /eco remove <player> <amount>} subcommand registered in {@link #createCommand()}
     * @param ctx {@link CommandContext} given by Paper on command invokation
     * @return {@link Command#SINGLE_SUCCESS}
     * @throws CommandSyntaxException can be thrown by {@link CommandContext#getArgument(String, Class)}
     */
    private static int handleRemoveSubcommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final EntitySelectorArgumentResolver entitySelectorArgumentResolver = ctx.getArgument("player", EntitySelectorArgumentResolver.class);
        final List<Entity> entities = entitySelectorArgumentResolver.resolve(ctx.getSource());
        final int amount = ctx.getArgument("amount", int.class);

        // check if the target is a player one last time
        if (!(entities.getFirst() instanceof Player target)) {
            ctx.getSource().getSender().sendRichMessage("<red>This command can only be run on players!");
            return Command.SINGLE_SUCCESS;
        }

        // get the players UUID and current balance
        UUID targetUUID = target.getUniqueId();
        long balance = Barium.INSTANCE.economyService.getBalance(targetUUID);

        // calculate the new balance and clamp to zero if needed. i dont want debt in this plugin at the moment
        long new_balance = balance - amount;
        if (new_balance < 0) {
            new_balance = 0;
        }

        // update the balance using the economyService
        Barium.INSTANCE.economyService.setBalance(targetUUID, new_balance);

        // send a detailed response to the sender
        ctx.getSource().getSender().sendRichMessage(
                "Removed <gold><amount><currencysign><white> from <green><entityname><white>. New balance is " +
                        "<gold><newbalance><currencysign>",
                Placeholder.component("entityname", entities.getFirst().name()),
                Placeholder.component("currencysign",
                        Component.text().content(Barium.INSTANCE.getConfig().getString("currency.symbol", "$"))),
                Placeholder.component("newbalance", Component.text().content(Long.toString(new_balance))),
                Placeholder.component("amount", Component.text().content(Integer.toString(amount)))
        );

        return Command.SINGLE_SUCCESS;
    }
}
