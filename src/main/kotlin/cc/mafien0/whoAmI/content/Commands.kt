package cc.mafien0.whoAmI.content

import cc.mafien0.whoAmI.content.PlayerControl.requestInput
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.GreedyStringArgument
import dev.jorel.commandapi.executors.CommandExecutor
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player

class Commands {
    fun load() {
        // Debug
        CommandAPICommand("spawntextdisplay")
            .executes(CommandExecutor { sender, _ ->
                Game.spawnTextDisplay(sender as Player)
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

        // Config
        CommandAPICommand("addposition")
           .withArguments(GreedyStringArgument("index"))
            .withPermission(CommandPermission.OP)
            .executes(CommandExecutor { sender, args ->
                val index = (args["index"] as String).toInt()
                val player = sender as Player
                Config.setPosition(index, player.location)
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
                Game.start()
            })
            .register()
    }
}