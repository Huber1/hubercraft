@file:Suppress("UnstableApiUsage")

package de.moritzhuber.hubercraft.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.tree.LiteralCommandNode
import de.moritzhuber.hubercraft.devmode.DevmodeService
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player


fun devmodeCommand(): LiteralCommandNode<CommandSourceStack> {
    return Commands.literal("devmode")
        .requires { it.sender.hasPermission("hubercraft.devmode") }
        .executes { ctx ->
            val sender: CommandSender = ctx.source.sender
            val executor: Entity? = ctx.source.executor

            if (executor != null && executor is Player)
                DevmodeService.toggleDevmode(executor)
            else if (sender is Player)
                DevmodeService.toggleDevmode(sender)
            else
                sender.sendMessage("This Command must be run as Player")


            Command.SINGLE_SUCCESS
        }
        .build()
}


