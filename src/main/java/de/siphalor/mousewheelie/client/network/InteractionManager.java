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

package de.siphalor.mousewheelie.client.network;

import de.siphalor.mousewheelie.client.MWClient;
import lombok.CustomLog;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


@Environment(EnvType.CLIENT)
@CustomLog
public class InteractionManager {
	private static final Queue<InteractionEvent> interactionEventQueue = new ArrayDeque<>();
	private static final ScheduledThreadPoolExecutor scheduledExecutor = new ScheduledThreadPoolExecutor(1);
	private static ScheduledFuture<?> tickFuture;;


	public static final Waiter DUMMY_WAITER = (TriggerType triggerType) -> true;
	public static final Waiter TICK_WAITER = (TriggerType triggerType) -> triggerType == TriggerType.TICK;

	public static final PacketEvent SWAP_WITH_OFFHAND_EVENT = new PacketEvent(
			new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN),
			triggerType -> triggerType == InteractionManager.TriggerType.CONTAINER_SLOT_UPDATE && MWClient.lastUpdatedSlot == 45
	);

	private static Waiter waiter = null;

	public static void delay(Runnable action, Duration duration) {
		scheduledExecutor.schedule(action, duration.toMillis(), TimeUnit.MILLISECONDS);
	}


	public static void push(InteractionEvent interactionEvent) {
		if (interactionEvent == null) {
			return;
		}
		synchronized (interactionEventQueue) {
			interactionEventQueue.add(interactionEvent);
			if (waiter == null)
				triggerSend(TriggerType.INITIAL);
		}
	}

	public static void pushAll(Collection<InteractionEvent> interactionEvents) {
		if (interactionEvents == null) {
			return;
		}
		synchronized (interactionEventQueue) {
			interactionEventQueue.addAll(interactionEvents);
			if (waiter == null)
				triggerSend(TriggerType.INITIAL);
		}
	}

	public static void pushClickEvent(int containerSyncId, int slotId, int buttonId, SlotActionType slotAction) {
		push(new ClickEvent(containerSyncId, slotId, buttonId, slotAction));
	}

	public static void triggerSend(TriggerType triggerType) {
		synchronized (interactionEventQueue) {
			if (waiter == null || waiter.trigger(triggerType)) {
				do {
					InteractionEvent event = interactionEventQueue.poll();
					if (event == null) {
						waiter = null;
						break;
					}

					doSendEvent(event);
				} while (waiter.trigger(TriggerType.INITIAL));
			}
		}
	}

	private static void doSendEvent(InteractionEvent event) {
		if (event.shouldRunOnMainThread()) {
			runOnMainThread(event);
		} else {
			waiter = event.send();
		}
	}

	private static void runOnMainThread(InteractionEvent event) {
		Waiter blockingWaiter = tt -> false;
		waiter = blockingWaiter;
		MinecraftClient.getInstance().execute(() -> {
			synchronized (interactionEventQueue) {
				if (waiter == blockingWaiter) {
					waiter = event.send();
				}
			}
		});
	}

	public static void setTickRate(long milliSeconds) {
		if (tickFuture != null) {
			tickFuture.cancel(false);
		}
		tickFuture = scheduledExecutor.scheduleAtFixedRate(InteractionManager::tick, milliSeconds, milliSeconds, TimeUnit.MILLISECONDS);
	}

	public static void tick() {
		try {
			triggerSend(TriggerType.TICK);
		} catch (Exception e) {
			log.error("Error while ticking InteractionManager ", e);
		}
	}

	public static void setWaiter(Waiter waiter) {
		synchronized (interactionEventQueue) {
			InteractionManager.waiter = waiter;
		}
	}

	public static void clear() {
		synchronized (interactionEventQueue) {
			interactionEventQueue.clear();
			waiter = null;
		}
	}

	public static boolean isReady() {
		synchronized (interactionEventQueue) {
			return waiter == null && interactionEventQueue.isEmpty();
		}
	}

	@FunctionalInterface
	public interface Waiter {
		boolean trigger(TriggerType triggerType);

		static Waiter equal(TriggerType triggerType) {
			return triggerType::equals;
		}
	}

	@Deprecated
	public static class GuiConfirmWaiter implements Waiter {
		int triggers;

		public GuiConfirmWaiter(int triggers) {
			this.triggers = triggers;
		}

		@Override
		public boolean trigger(TriggerType triggerType) {
			return triggerType == TriggerType.GUI_CONFIRM && --triggers == 0;
		}
	}

	public static class SlotUpdateWaiter implements Waiter {
		int triggers;

		public SlotUpdateWaiter(int triggers) {
			this.triggers = triggers;
		}

		@Override
		public boolean trigger(TriggerType triggerType) {
			return triggerType == TriggerType.CONTAINER_SLOT_UPDATE && --triggers == 0;
		}
	}

	public enum TriggerType {
		INITIAL, CONTAINER_SLOT_UPDATE, GUI_CONFIRM, HELD_ITEM_CHANGE, TICK
	}

	@FunctionalInterface
	public interface InteractionEvent {
		/**
		 * Sends the interaction to the server
		 *
		 * @return the number of inventory packets to wait for
		 */
		Waiter send();
		default boolean shouldRunOnMainThread() {
			return false;
		}
	}

	public static class ClickEvent implements InteractionEvent {
		private final Waiter waiter;
		private final int containerSyncId;
		private final int slotId;
		private final int buttonId;
		private final SlotActionType slotAction;

		public ClickEvent(int containerSyncId, int slotId, int buttonId, SlotActionType slotAction) {
			this(containerSyncId, slotId, buttonId, slotAction, TICK_WAITER);
		}

		public ClickEvent(int containerSyncId, int slotId, int buttonId, SlotActionType slotAction, Waiter waiter) {
			this.containerSyncId = containerSyncId;
			this.slotId = slotId;
			this.buttonId = buttonId;
			this.slotAction = slotAction;
			this.waiter = waiter;
		}

		@Override
		public Waiter send() {
			MinecraftClient.getInstance().interactionManager.clickSlot(containerSyncId, slotId, buttonId, slotAction, MinecraftClient.getInstance().player);
			return waiter;
		}

		@Override
		public boolean shouldRunOnMainThread() {
			return true;
		}
	}

	public static class CallbackEvent implements InteractionEvent {
		private final Supplier<Waiter> callback;
		private final boolean shouldRunOnMainThread;

		public CallbackEvent(Supplier<Waiter> callback) {
			this(callback, false);
		}

		public CallbackEvent(Supplier<Waiter> callback, boolean shouldRunOnMainThread) {
			this.callback = callback;
			this.shouldRunOnMainThread = shouldRunOnMainThread;
		}

		@Override
		public Waiter send() {
			return callback.get();
		}

		@Override
		public boolean shouldRunOnMainThread() {
			return shouldRunOnMainThread;
		}
	}

	public static class PacketEvent implements InteractionEvent {
		private final Packet<?> packet;
		private final Waiter waiter;

		public PacketEvent(Packet<?> packet) {
			this(packet, DUMMY_WAITER);
		}

		public PacketEvent(Packet<?> packet, int triggers) {
			this(packet, new SlotUpdateWaiter(triggers));
		}

		public PacketEvent(Packet<?> packet, Waiter waiter) {
			this.packet = packet;
			this.waiter = waiter;
		}

		@Override
		public Waiter send() {
			MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);
			return waiter;
		}
	}
}
