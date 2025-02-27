package de.moritzhuber.hubercraft.helper.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.inventory.ItemStack
import java.util.*

class ItemStackSerializer : KSerializer<ItemStack?> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("hubercraft.ItemStack", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ItemStack?) {
        val bytes = value?.serializeAsBytes()
        if (bytes != null)
            encoder.encodeString(Base64.getEncoder().encodeToString(bytes))
    }

    override fun deserialize(decoder: Decoder): ItemStack {
        val string = decoder.decodeString()
        val bytes = Base64.getDecoder().decode(string)
        val itemStack = ItemStack.deserializeBytes(bytes)
        return itemStack
    }
}
