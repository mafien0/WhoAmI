package cc.mafien0.whoAmI

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIPaperConfig
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.CommandExecutor
import org.bukkit.plugin.java.JavaPlugin
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("WhoAmI")
class WhoAmI : JavaPlugin() {

    override fun onLoad() {
        CommandAPI.onLoad(CommandAPIPaperConfig(this).verboseOutput(true)) // Load with verbose output

        CommandAPICommand("ping")
            .executes(CommandExecutor { sender, _ ->
                sender.sendMessage("pong!")
            })
            .register()
    }

    override fun onEnable() {
        CommandAPI.onEnable()
        // Register commands, listeners etc.
    }

    override fun onDisable() {
        CommandAPI.onDisable()
    }
}
