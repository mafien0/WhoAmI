package cc.mafien0.whoAmI.config

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

    // Save a position to config
    fun setPosition(index: Int, location: Location) {
        config.set("positions.$index", location)
        saveConfig()
    }

    // Save config to file
    private fun saveConfig() {
        config.save(configFile)
    }

    fun getValue(location: String): Any? = config.get(location)

    // Get a position from config
    fun getPosition(index: Int): Location? {
        return config.getLocation("positions.$index")
    }
}