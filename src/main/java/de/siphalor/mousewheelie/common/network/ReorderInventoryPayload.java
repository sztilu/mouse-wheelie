/*
 * Copyright 2020-2022 Siphalor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

package de.siphalor.mousewheelie.common.network;

import de.siphalor.mousewheelie.MouseWheelie;
import lombok.CustomLog;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@CustomLog
public record ReorderInventoryPayload(int syncId, int[] slotMappings) implements CustomPayload {

	public static final PacketCodec<PacketByteBuf, ReorderInventoryPayload> CODEC =
			PacketCodec.of(ReorderInventoryPayload::write, ReorderInventoryPayload::read);
	public  static  final Id<ReorderInventoryPayload> ID = CustomPayload.id(MouseWheelie.MOD_ID + ":reorder_inventory_c2s");

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

	public void write(@NotNull PacketByteBuf buf) {
		buf.writeVarInt(syncId);
		buf.writeIntArray(slotMappings);
	}

	public static @Nullable ReorderInventoryPayload read(PacketByteBuf buf) {
		int syncId = buf.readVarInt();
		int[] reorderedIndices = buf.readIntArray();

		if (reorderedIndices.length % 2 != 0) {
			log.warn("Received reorder inventory packet with invalid data!");
			return null;
		}

		return new ReorderInventoryPayload(syncId, reorderedIndices);
	}

    public int getSyncId() {
		return syncId;
    }

	public int[] getSlotMappings() {
		return slotMappings;
	}
}
