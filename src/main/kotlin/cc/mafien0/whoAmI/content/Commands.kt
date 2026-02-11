package cc.mafien0.whoAmI.content

import cc.mafien0.whoAmI.content.PlayerControl.requestInput
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.GreedyStringArgument
import dev.jorel.commandapi.arguments.IntegerArgument
import dev.jorel.commandapi.arguments.LocationArgument
import dev.jorel.commandapi.executors.CommandExecutor
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.entity.Player

class Commands {
    fun load() {
        // Debug
        CommandAPICommand("spawntextdisplay")
            .executes(CommandExecutor { sender, _ ->
                Game.spawnTextDisplay(sender as Player, sender.location)
            })
            .register()
        CommandAPICommand("requestinput")
            .executes(CommandExecutor { sender, _ ->
                val player = sender as Player
                requestInput(player) { input ->
                    player.sendMessage(Component.text("You entered: $input", NamedTextColor.GREEN))
                }
            })
            .register()
        CommandAPICommand("step")
            .withArguments(GreedyStringArgument("index"))
            .executes(CommandExecutor { sender, args ->
                val player = sender as Player
                val index = (args["index"] as String).toInt()
                if (index == 1) Game.firstStep(player)
                if (index == 2) Game.secondStep(player)
            })
            .register()

        // Add position
        CommandAPICommand("addposition")
            .withArguments(IntegerArgument("index"), LocationArgument("location"))
            .withPermission(CommandPermission.OP)
            .executes(CommandExecutor { sender, args ->
                val index = args["index"] as Int
                val location = args["location"] as Location
                location.add(0.0, -1.0, 0.0)
                Config.setPosition(index-1, location)
                sender.sendMessage(Component.text("Position $index set to ${location.blockX}, ${location.blockY}, ${location.blockZ}", NamedTextColor.GREEN))
            })
            .register()

        // Join
        CommandAPICommand("join")
            .withAliases("j")
            .executes(CommandExecutor { sender, _ ->
                Game.joinPlayer(sender as Player)
            })
            .register()

        // leave
        CommandAPICommand("leave")
            .withAliases("l")
            .executes(CommandExecutor { sender, _ ->
                Game.leavePlayer(sender as Player)
            })
            .register()

        // Start
        CommandAPICommand("start")
            .withAliases("s", "restart")
            .withPermission(CommandPermission.OP)
            .executes(CommandExecutor { sender, _ ->
                Game.start(sender as Player)
            })
            .register()

        // Start
        CommandAPICommand("stopgame")
            .withAliases("s", "gamestop")
            .withPermission(CommandPermission.OP)
            .executes(CommandExecutor { sender, _ ->
                Game.stop(sender as Player)
            })
            .register()
    }
}