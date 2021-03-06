package me.calebbassham.helpop

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class HelpopPlugin : JavaPlugin() {

    override fun onEnable() {
        val helpop = HelpopModule()
        val helpopCmd = helpop.Cmd()
        val helpopAnswerCmd = helpop.AnswerCmd()

        Bukkit.getPluginManager().registerEvents(helpop, this)
        Bukkit.getPluginCommand("helpop").executor = helpopCmd
        Bukkit.getPluginCommand("answerhelpop").executor = helpopAnswerCmd
    }

}