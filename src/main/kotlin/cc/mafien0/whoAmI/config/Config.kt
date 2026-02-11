package cc.mafien0.whoAmI.config

import cc.mafien0.whoAmI.game.GamePlayer
import org.bukkit.Location
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File

object Config {
    private lateinit var plugin: Plugin
    private lateinit var configFile: File
    private lateinit var config: FileConfiguration

    // Initialize config
    fun init(pluginInstance: Plugin) {
        plugin = pluginInstance
        configFile = File(plugin.dataFolder, "config.yml")
        
        // Create config file if it doesn't exist
        if (!configFile.exists()) {
            plugin.saveDefaultConfig()
        }
        
        config = YamlConfiguration.loadConfiguration(configFile)
    }

    fun getValue(location: String): Any? = config.get(location)
    fun isDebug(): Boolean = getValue("debug") as Boolean // No setters, set manually in the config

    // Save a position to config
    fun setPosition(index: Int, location: Location) {
        config.set("positions.$index", location)
        saveConfig()
    }

    // Save config to file
    private fun saveConfig() {
        config.save(configFile)
    }

    // Get a position from config
    fun getPosition(index: Int): Location? {
        return config.getLocation("positions.$index")
    }

    fun getMaxPlayers(): Int = getValue("max-players") as Int
    fun setMaxPlayers(maxPlayers: Int) {
        config.set("max-players", maxPlayers)
        GamePlayer.maxPlayers = maxPlayers
        saveConfig()
    }
}