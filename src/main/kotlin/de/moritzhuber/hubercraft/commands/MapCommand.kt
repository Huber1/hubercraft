@file:Suppress("UnstableApiUsage")

package de.moritzhuber.hubercraft.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

private enum class WorldType {
    world,
    world_nether,
    world_the_end,
}

fun mapCommand(): LiteralCommandNode<CommandSourceStack> {
    return Commands.literal("map")
        .requires { it.sender.hasPermission("hubercraft.map") }
        .then(
            Commands.argument("world", StringArgumentType.word())
                .suggests { ctx, builder ->
                    WorldType.entries.forEach { builder.suggest(it.name) }

                    builder.buildFuture()
                }
                .executes { ctx ->
                    val world = StringArgumentType.getString(ctx, "world")

                    if (WorldType.entries.none { it.name == world }) {
                        ctx.source.sender.sendMessage(
                            Component.text("<world> must be one of ", NamedTextColor.GOLD)
                                .append(Component.text(WorldType.entries.joinToString(", "), NamedTextColor.RED))
                        )
                        return@executes Command.SINGLE_SUCCESS
                    }

                    sendMessage(ctx, world)

                    Command.SINGLE_SUCCESS
                })
        .executes { ctx ->
            sendMessage(ctx)

            Command.SINGLE_SUCCESS
        }
        .build()
}

private fun sendMessage(ctx: CommandContext<CommandSourceStack>, world: String? = null) {
    val sender: CommandSender = ctx.source.sender
    val executor: Entity? = ctx.source.executor

    if (executor != null && executor is Player) {
        executor.sendMessage(getMessage(world))
    } else {
        sender.sendMessage(getMessage(world))
    }
}

private fun getMessage(world: String?): Component {
    val url = if (world != null) "https://huber.party/#$world" else "https://huber.party"
    return Component.text("Online-Map auf ")
        .color(NamedTextColor.GOLD)
        .append(
            Component.text(url)
                .color(NamedTextColor.RED)
                .clickEvent(ClickEvent.openUrl(url))
        )
}