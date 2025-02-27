package de.moritzhuber.hubercraft.spawnIsland

import de.moritzhuber.hubercraft.helper.serializer.ItemStackSerializer
import de.moritzhuber.hubercraft.helper.serializer.UUIDSerializer
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.*

@Serializable
private data class InventoryDataContainer(
    val chestplateData: MutableMap<
            @Serializable(with = UUIDSerializer::class) UUID,
            @Serializable(with = ItemStackSerializer::class) ItemStack?>,
    val boosterSlotData: MutableMap<
            @Serializable(with = UUIDSerializer::class) UUID,
            @Serializable(with = ItemStackSerializer::class) ItemStack?>
)

class SavedInventorySlots(private val plugin: JavaPlugin) {
    private lateinit var chestPlateData: MutableMap<UUID, ItemStack?>
    private lateinit var boosterSlotData: MutableMap<UUID, ItemStack?>

    private val file = File("hubercraft/chestplates.json")

    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var job: Job? = null

    init {
        plugin.logger.info("Loading saved Chestplates")
        runBlocking {
            loadFromDisk()
        }
    }

    fun saveChestplate(uuid: UUID, itemStack: ItemStack?) {
        chestPlateData[uuid] = itemStack
        initSave()
    }

    fun saveBooster(uuid: UUID, itemStack: ItemStack?) {
        boosterSlotData[uuid] = itemStack
        initSave()
    }

    fun removeChestplate(uuid: UUID): ItemStack? {
        val data = chestPlateData.remove(uuid)
        initSave()
        return data
    }

    fun removeBooster(uuid: UUID): ItemStack? {
        val data = boosterSlotData.remove(uuid)
        initSave()
        return data
    }

    private fun initSave() {
        if (job?.isActive == true) return

        job = coroutineScope.launch {
            delay(500)
            persistToDisk()
            job = null
        }
    }


    suspend fun persistToDisk() {
        plugin.logger.info("Persisting Spawn InventorySlots to Disk")
        val data = InventoryDataContainer(
            chestPlateData,
            boosterSlotData,
        )

        val json = Json.encodeToString(data)

        withContext(Dispatchers.IO) {
            file.parentFile.mkdirs()
            file.writeText(json)
        }

    }

    private suspend fun loadFromDisk() {
        plugin.logger.info("Restoring Spawn InventorySlots from Disk")

        if (file.exists()) {
            try {
                val fileContent: String = withContext(Dispatchers.IO) {
                    file.readText()
                }
                val container = Json.decodeFromString<InventoryDataContainer>(fileContent)
                chestPlateData = container.chestplateData
                boosterSlotData = container.boosterSlotData
            } catch (e: SerializationException) {
                chestPlateData = mutableMapOf()
                boosterSlotData = mutableMapOf()
            }
        } else {
            chestPlateData = mutableMapOf()
            boosterSlotData = mutableMapOf()
        }
    }
}
