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

package de.siphalor.mousewheelie.client.inventory.sort;

import de.siphalor.mousewheelie.MWConfig;
import de.siphalor.mousewheelie.client.inventory.ContainerScreenHelper;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import de.siphalor.mousewheelie.client.network.MWClientNetworking;
import de.siphalor.mousewheelie.client.util.CreativeSearchOrder;
import de.siphalor.mousewheelie.client.util.ItemStackUtils;
import de.siphalor.mousewheelie.client.util.StackMatcher;
import de.siphalor.mousewheelie.client.util.inject.ISlot;
import de.siphalor.mousewheelie.common.network.ReorderInventoryPayload;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.*;
import java.util.concurrent.locks.Lock;

@Environment(EnvType.CLIENT)
public class InventorySorter {
	private final ContainerScreenHelper<? extends HandledScreen<?>> screenHelper;
	private final HandledScreen<?> containerScreen;
	private Slot[] inventorySlots;
	private final ItemStack[] stacks;

	/**
	 * @deprecated Use {@link #InventorySorter(ContainerScreenHelper, HandledScreen, Slot)} instead
	 */
	@Deprecated
	public InventorySorter(HandledScreen<?> containerScreen, Slot originSlot) {
		this(
				ContainerScreenHelper.of(
						containerScreen,
						(slot, action, slotActionType) -> new InteractionManager.ClickEvent(
								containerScreen.getScreenHandler().syncId,
								((ISlot) slot).mouseWheelie_getIdInContainer(), action, slotActionType
						)
				),
				containerScreen,
				originSlot
		);
	}

	public InventorySorter(ContainerScreenHelper<? extends HandledScreen<?>> screenHelper, HandledScreen<?> containerScreen, Slot originSlot) {
		this.screenHelper = screenHelper;
		this.containerScreen = containerScreen;

		collectSlots(originSlot);

		this.stacks = new ItemStack[inventorySlots.length];
		for (int i = 0; i < inventorySlots.length; i++) {
			stacks[i] = inventorySlots[i].getStack();
		}
	}

	private void collectSlots(Slot originSlot) {
		int originScope = screenHelper.getScope(originSlot);
		if (originScope == ContainerScreenHelper.INVALID_SCOPE) {
			this.inventorySlots = new Slot[0];
			return;
		}
		ArrayList<Slot> slotsInScope = new ArrayList<>();
		for (Slot slot : containerScreen.getScreenHandler().slots) {
			if (originScope == screenHelper.getScope(slot, true)) {
				slotsInScope.add(slot);
			}
		}
		this.inventorySlots = slotsInScope.toArray(new Slot[0]);
	}

	private void combineStacks() {
		ItemStack stack;
		ArrayDeque<InteractionManager.InteractionEvent> clickEvents = new ArrayDeque<>();
		for (int i = stacks.length - 1; i >= 0; i--) {
			stack = stacks[i];
			if (stack.isEmpty()) continue;
			int stackSize = stack.getCount();
			if (stackSize >= stack.getItem().getMaxCount()) continue;
			clickEvents.add(screenHelper.createClickEvent(inventorySlots[i], 0, SlotActionType.PICKUP));
			for (int j = 0; j < i; j++) {
				ItemStack targetStack = stacks[j];
				if (targetStack.isEmpty()) continue;
				if (targetStack.getCount() >= targetStack.getItem().getMaxCount()) continue;
				if (ItemStack.areItemsAndComponentsEqual(stack, targetStack)) {
					int delta = targetStack.getItem().getMaxCount() - targetStack.getCount();
					delta = Math.min(delta, stackSize);
					stackSize -= delta;
					targetStack.setCount(targetStack.getCount() + delta);
					clickEvents.add(screenHelper.createClickEvent(inventorySlots[j], 0, SlotActionType.PICKUP));
					if (stackSize <= 0) break;
				}
			}
			if (clickEvents.size() <= 1) {
				clickEvents.clear();
				continue;
			}
			InteractionManager.pushAll(clickEvents);
			InteractionManager.triggerSend(InteractionManager.TriggerType.GUI_CONFIRM);
			clickEvents.clear();
			if (stackSize > 0) {
				InteractionManager.push(screenHelper.createClickEvent(inventorySlots[i], 0, SlotActionType.PICKUP));
				stack.setCount(stackSize);
			} else {
				stacks[i] = ItemStack.EMPTY;
			}
		}
	}

	public void sort(MWConfig.SortModes sortMode) {
		if (inventorySlots.length <= 1) {
			return;
		}

		combineStacks();
		int[] sortIds = new int[stacks.length];
		for (int i = 0; i < sortIds.length; i++) {
			sortIds[i] = i;
		}

		switch (sortMode) {
			case NONE -> {}
			case ALPHABET -> {
				String[] strings = new String[sortIds.length];
				for (int i = 0; i < sortIds.length; i++) {
					ItemStack stack = stacks[i];
					strings[i] = stack.isEmpty() ? "" : stack.getName().getString();
				}
				
				IntArrays.quickSort(sortIds, (a, b) -> {
					if (strings[a].equals("")) {
						if (strings[b].equals(""))
							return 0;
						return 1;
					}
					if (strings[b].equals("")) return -1;
					int comp = strings[a].compareToIgnoreCase(strings[b]);
					if (comp == 0) {
						return ItemStackUtils.compareEqualItems(stacks[a], stacks[b]);
					}
					return comp;
				});
			}
			case CREATIVE -> {
				int[] sortValues = new int[sortIds.length];
				if (MWConfig.optimizeCreativeSearchSort) {
					Lock lock = CreativeSearchOrder.getReadLock();
					lock.lock();
					for (int i = 0; i < stacks.length; i++) {
						sortValues[i] = CreativeSearchOrder.getStackSearchPosition(stacks[i]);
					}
					lock.unlock();
				} else {
					Collection<ItemStack> displayStacks = ItemGroups.getSearchGroup().getDisplayStacks();
					List<ItemStack> displayStackList;
					if (displayStacks instanceof List) {
						displayStackList = (List<ItemStack>) displayStacks;
					} else {
						displayStackList = new ArrayList<>(displayStacks);
					}
					Object2IntMap<StackMatcher> lookup = new Object2IntOpenHashMap<>(stacks.length);
					for (int i = 0; i < stacks.length; i++) {
						final ItemStack stack = stacks[i];
						sortValues[i] = lookup.computeIfAbsent(StackMatcher.of(stack), matcher -> {
							int index = displayStackList.indexOf(matcher);
							if (index == -1) {
								return lookup.computeIfAbsent(StackMatcher.ignoreNbt(stack), matcher2 -> {
									int plainIndex = displayStackList.indexOf(matcher2);
									if (plainIndex == -1) {
										return Integer.MAX_VALUE;
									}
									return plainIndex;
								});
							}
							return index;
						});
					}
				}
				sortByValues(sortIds, stacks, sortValues);
			}
			case QUANTITY -> {
				HashMap<Item, Integer> itemToAmountMap = new HashMap<>();
				
				for (ItemStack stack : stacks) {
					if (stack.isEmpty()) continue;
					if (!itemToAmountMap.containsKey(stack.getItem())) {
						itemToAmountMap.put(stack.getItem(), stack.getCount());
					} else {
						itemToAmountMap.put(stack.getItem(), itemToAmountMap.get(stack.getItem()) + stack.getCount());
					}
				}
				
				IntArrays.quickSort(sortIds, (a, b) -> {
					ItemStack stack = stacks[a];
					ItemStack stack2 = stacks[b];
					if (stack.isEmpty()) {
						return stack2.isEmpty() ? 0 : 1;
					}
					if (stack2.isEmpty()) {
						return -1;
					}
					Integer amountA = itemToAmountMap.get(stack.getItem());
					Integer amountB = itemToAmountMap.get(stack2.getItem());
					int cmp = Integer.compare(amountB, amountA);
					if (cmp != 0) {
						return cmp;
					}
					return ItemStackUtils.compareEqualItems(stack, stack2);
				});
			}
			case RAW_ID -> {
				int[] rawIds = Arrays.stream(stacks).mapToInt(stack -> stack.isEmpty() ? Integer.MAX_VALUE : Registries.ITEM.getRawId(stack.getItem())).toArray();
				sortByValues(sortIds, stacks, rawIds);
			}
		}

		if (MWConfig.serverAcceleratedSorting && MWClientNetworking.canSendReorderPacket()) {
			this.reorderInventory(sortIds);
		} else {
			this.sortOnClient(sortIds);
		}
	}
	
	private static void sortByValues(int[] sortIds, ItemStack[] stacks, int[] values) {
		IntArrays.quickSort(sortIds, (a, b) -> {
			int cmp = Integer.compare(values[a], values[b]);
			if (cmp != 0) {
				return cmp;
			}
			return ItemStackUtils.compareEqualItems(stacks[a], stacks[b]);
		});
	}

	protected void reorderInventory(int[] sortedIds) {
		int[] slotMappings = new int[sortedIds.length * 2];
		for (int i = 0; i < sortedIds.length; i++) {
			Slot from = inventorySlots[sortedIds[i]];
			Slot to = inventorySlots[i];
			slotMappings[i * 2] = ((ISlot) from).mouseWheelie_getIdInContainer();
			slotMappings[i * 2 + 1] = ((ISlot) to).mouseWheelie_getIdInContainer();
		}
		InteractionManager.push(() -> {
			MWClientNetworking.send(new ReorderInventoryPayload(containerScreen.getScreenHandler().syncId, slotMappings));
			return InteractionManager.TICK_WAITER;
		});
	}

	protected void sortOnClient(int[] sortedIds) {
		ItemStack currentStack;
		final int slotCount = stacks.length;

		// sortedIds now maps the slot index (the target id) to which slot's contents should be moved there (the origin id)
		int[] origin2Target = new int[slotCount];
		for (int i = 0; i < origin2Target.length; i++) {
			origin2Target[sortedIds[i]] = i;
		}

		// This is a combined bitset to save whether eac slot is done or empty.
		// It consists of all bits for the done states in the first half and the empty states in the second half.
		BitSet doneSlashEmpty = new BitSet(slotCount * 2);
		for (int i = 0; i < slotCount; i++) { // Iterate all slots to set up the state bit set
			if (i == sortedIds[i]) { // If the target slot is equal to the origin,
				doneSlashEmpty.set(i); // then we're done with that slot already.
				continue;
			}
			if (stacks[i].isEmpty()) doneSlashEmpty.set(slotCount + i); // mark if it's empty
		}
		// Iterate all slots, with i as the target slot index
		// sortedIds[i] is therefore the origin slot
		for (int i = 0; i < slotCount; i++) {
			if (doneSlashEmpty.get(i)) { // See if we're already done,
				continue; // and skip.
			}
			if (doneSlashEmpty.get(slotCount + sortedIds[i])) { // If the origin is empty,
				doneSlashEmpty.set(sortedIds[i]); // we can mark it as done
				continue; // and skip.
			}

			// This is where the action happens.
			// Pick up the stack at the origin slot.
			InteractionManager.push(screenHelper.createClickEvent(inventorySlots[sortedIds[i]], 0, SlotActionType.PICKUP));
			doneSlashEmpty.set(slotCount + sortedIds[i]); // Mark the origin slot as empty (because we picked the stack up, duh)
			currentStack = stacks[sortedIds[i]]; // Save the stack we're currently working with
			Slot workingSlot = inventorySlots[sortedIds[i]]; // A slot that we can use when fiddling around with swapping stacks
			int id = i; // id will reflect the target slot in the following loop
			do { // This loop follows chained stack moves (e.g. 1->2->5->1).
				if (
						stacks[id].getItem() == currentStack.getItem()
								//&& stacks[id].getCount() == currentStack.getCount()
								&& !doneSlashEmpty.get(slotCount + id)
								&& ItemStack.areItemsAndComponentsEqual(stacks[id], currentStack)
				) {
					// If the current stack and the target stack are completely equal, then we can skip this step in the chain
					if (stacks[id].getCount() == currentStack.getCount()) {
						doneSlashEmpty.set(id); // mark the current target as done
						id = origin2Target[id];
						continue;
					}
					if (currentStack.getCount() < stacks[id].getCount()) { // Clicking with a low stack on a full stack does nothing
						// The workaround is: click working slot, click target slot, click working slot, click target slot, click working slot
						Slot targetSlot = inventorySlots[id];
						InteractionManager.push(screenHelper.createClickEvent(workingSlot, 0, SlotActionType.PICKUP));
						InteractionManager.push(screenHelper.createClickEvent(targetSlot, 0, SlotActionType.PICKUP));
						InteractionManager.push(screenHelper.createClickEvent(workingSlot, 0, SlotActionType.PICKUP));
						InteractionManager.push(screenHelper.createClickEvent(targetSlot, 0, SlotActionType.PICKUP));
						InteractionManager.push(screenHelper.createClickEvent(workingSlot, 0, SlotActionType.PICKUP));

						currentStack = stacks[id];
						doneSlashEmpty.set(id); // mark the current target as done
						id = origin2Target[id];
						continue;
					}
				}

				// swap the current stack with the target stack
				InteractionManager.push(screenHelper.createClickEvent(inventorySlots[id], 0, SlotActionType.PICKUP));
				currentStack = stacks[id];
				doneSlashEmpty.set(id); // mark the current target as done
				// If the target that we just swapped with was empty before, then this breaks the chain.
				if (doneSlashEmpty.get(slotCount + id)) {
					break;
				}
				id = origin2Target[id];
			} while (!doneSlashEmpty.get(id)); // If we find a target that is marked as done already, then we can break the chain.
		}
	}
}
