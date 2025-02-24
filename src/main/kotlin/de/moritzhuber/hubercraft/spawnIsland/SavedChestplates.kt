package de.moritzhuber.hubercraft.spawnIsland

import kotlinx.coroutines.*
import org.bukkit.inventory.ItemStack
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*


class SavedChestplates {
    private lateinit var chestPlateData: MutableMap<UUID, ItemStack?>
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val file = File("hubercraft/chestplates.data")

    init {
        runBlocking {
            loadFromDisk()
        }
    }

    fun save(uuid: UUID, itemStack: ItemStack?) {
        chestPlateData[uuid] = itemStack

        coroutineScope.launch {
            persistToDisk()
        }
    }

    fun get(uuid: UUID): ItemStack? {
        return chestPlateData[uuid]
    }

    fun remove(uuid: UUID) = chestPlateData.remove(uuid)

    suspend fun persistToDisk() {
        val serialized: Map<UUID, ByteArray?> = chestPlateData.mapValues { it.value?.serializeAsBytes() }

        withContext(Dispatchers.IO) {
            file.parentFile.mkdirs()
            ObjectOutputStream(file.outputStream()).use { it.writeObject(serialized) }
        }
    }

    private suspend fun loadFromDisk() {
        if (file.exists()) {
            @Suppress("UNCHECKED_CAST")
            val deserialized = withContext(Dispatchers.IO) {
                ObjectInputStream(file.inputStream()).use { it.readObject() as Map<UUID, ByteArray?> }
            }
            chestPlateData = deserialized.mapValues {
                if (it.value != null) ItemStack.deserializeBytes(it.value!!)
                else null
            }.toMutableMap()
        } else chestPlateData = mutableMapOf()
    }
}