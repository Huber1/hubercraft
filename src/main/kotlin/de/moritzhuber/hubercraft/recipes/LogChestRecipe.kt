package de.moritzhuber.hubercraft.recipes

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.plugin.java.JavaPlugin

class LogChestRecipe(plugin: JavaPlugin) : ShapedRecipe(
    NamespacedKey(plugin, "LogChestRecipe"),
    ItemStack(Material.CHEST, 4),
) {
    init {
        shape("AAA", "A A", "AAA")
        setIngredient('A', RecipeChoice.MaterialChoice(Tag.LOGS))
    }
}