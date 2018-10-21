package me.calebbassham.helpop

import org.bukkit.ChatColor
import org.bukkit.scheduler.BukkitRunnable

fun String.translateAlternateColorCodes(altColorChar: Char = '&'): String = ChatColor.translateAlternateColorCodes(altColorChar, this)

fun bracket(text: String, bracketColor: String) = "$bracketColor[$text$bracketColor]"

fun bukkitRunnable(f: () -> Unit) = object : BukkitRunnable() {
    override fun run() {
        f()
    }
}