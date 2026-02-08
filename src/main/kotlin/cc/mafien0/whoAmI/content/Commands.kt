package cc.mafien0.whoAmI.content

import cc.mafien0.whoAmI.content.PlayerControl.requestInput
import dev.jorel.commandapi.CommandAPICommand
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
            .executes(CommandExecutor { sender, _ ->
                Game.start()
            })
            .register()
    }
}