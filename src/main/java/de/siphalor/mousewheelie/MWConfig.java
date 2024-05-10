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

package de.siphalor.mousewheelie;

import com.google.common.base.CaseFormat;
import com.terraformersmc.modmenu.config.option.BooleanConfigOption;
import de.siphalor.mousewheelie.client.MWClient;
import de.siphalor.mousewheelie.client.network.InteractionManager;
import de.siphalor.mousewheelie.client.util.CreativeSearchOrder;
import de.siphalor.mousewheelie.client.util.ItemStackUtils;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.autogen.TickBox;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.gui.controllers.cycling.EnumController;
import dev.isxander.yacl3.gui.controllers.dropdown.AbstractDropdownController;
import dev.isxander.yacl3.gui.controllers.string.number.IntegerFieldController;
import dev.isxander.yacl3.impl.controller.AbstractControllerBuilderImpl;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.*;

@SuppressWarnings({"WeakerAccess", "unused"})
public class MWConfig {
	
	public static ConfigClassHandler<MWConfig> HANDLER = ConfigClassHandler.createBuilder(MWConfig.class)
			.id(new Identifier("mouse wheelie config"))
			.serializer(config -> GsonConfigSerializerBuilder.create(config)
					.setPath(FabricLoader.getInstance().getConfigDir().resolve("my_mod.json5"))
					.setJson5(true)
					.build())
			.build();
	
	public static Screen createConfigScreen(Screen parent) {
		var yaclScreen = YetAnotherConfigLib.createBuilder()
				.title(Text.translatable("config.screen.mousewheelie"))
				.category(ConfigCategory.createBuilder()
						.name(Text.translatable("config.screen.mousewheelie.general"))
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("config.screen.mousewheelie.general.interaction-rate"))
								.binding(10, () -> interactionRate, value -> interactionRate = value)
								.controller(opt -> IntegerFieldControllerBuilder.create(opt)
										.min(1).max(Integer.MAX_VALUE)
								)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("config.screen.mousewheelie.general.integrated-interaction-rate"))
								.description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.general.integrated-interaction-rate.description")))
								.binding(1, () -> integratedInteractionRate, value -> integratedInteractionRate = value)
								.controller(opt -> IntegerFieldControllerBuilder.create(opt)
										.min(1).max(Integer.MAX_VALUE)
								)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("config.screen.mousewheelie.general.enable-quick-armor-swapping"))
								.description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.general.enable-quick-armor-swapping.description")))
								.binding(true, () -> enableQuickArmorSwapping, value -> enableQuickArmorSwapping = value)
								.controller(TickBoxControllerBuilder::create)
								.build()
						)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("config.screen.mousewheelie.general.enable-drop-modifier"))
								.description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.general.enable-drop-modifier.description")))
								.binding(true, () -> enableDropModifier, value -> enableDropModifier = value)
								.controller(TickBoxControllerBuilder::create)
								.build()
						)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("config.screen.mousewheelie.general.enable-quick-craft"))
								.description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.general.enable-quick-craft.description")))
								.binding(true, () -> enableQuickCraft, value -> enableQuickCraft = value)
								.controller(TickBoxControllerBuilder::create)
								.build()
						)
						.option(Option.<ItemStackUtils.ComponentTypeMatchMode>createBuilder()
								.name(Text.translatable("config.screen.mousewheelie.general.item-kinds-nbt-match-mode"))
								.description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.general.item-kinds-nbt-match-mode.description")))
								.binding(ItemStackUtils.ComponentTypeMatchMode.SOME, () -> itemComponentMatchMode, value -> itemComponentMatchMode = value)
								.controller(EnumDropdownControllerBuilder::create)
								.build()
						)
						.option(Option.<HotbarScoping>createBuilder()
								.name(Text.translatable("config.screen.mousewheelie.general.hotbar-scoping"))
								.description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.general.hotbar-scoping.description")))
								.binding(HotbarScoping.SOFT, () -> hotbarScoping, value -> hotbarScoping = value)
								.controller(EnumDropdownControllerBuilder::create)
								.build()
						)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("config.screen.mousewheelie.general.better-fast-dragging"))
								.description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.general.better-fast-dragging.description")))
								.binding(false, () -> betterFastDragging, value -> betterFastDragging = value)
								.controller(TickBoxControllerBuilder::create)
								.build()
						)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("config.screen.mousewheelie.general.enable-bundle-dragging"))
								.description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.general.enable-bundle-dragging.description")))
								.binding(true, () -> enableBundleDragging, value -> enableBundleDragging = value)
								.controller(TickBoxControllerBuilder::create)
								.build()
						)
						.build())
				.category(ConfigCategory.createBuilder()
						.name(Text.translatable("config.screen.mousewheelie.scrolling"))
						.tooltip(Text.translatable("config.screen.mousewheelie.scrolling.description"))
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("config.screen.mousewheelie.scrolling.enable"))
								.binding(true, () -> enable, value -> enable = value)
								.controller(TickBoxControllerBuilder::create)
								.build()
						)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("config.screen.mousewheelie.scrolling.invert"))
								.description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.scrolling.invert.description")))
								.binding(false, () -> invert, value -> invert = value)
								.controller(TickBoxControllerBuilder::create)
								.build()
						)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("config.screen.mousewheelie.scrolling.directional-scrolling"))
								.description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.scrolling.directional-scrolling.description")))
								.binding(true, () -> directionalScrolling, value -> directionalScrolling = value)
								.controller(TickBoxControllerBuilder::create)
								.build()
						)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("config.screen.mousewheelie.scrolling.scroll-creative-menu-items"))
								.description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.scrolling.scroll-creative-menu-items.description")))
								.binding(true, () -> scrollCreativeMenuItems, value -> scrollCreativeMenuItems = value)
								.controller(TickBoxControllerBuilder::create)
								.build()
						)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("config.screen.mousewheelie.scrolling.scroll-creative-menu-tabs"))
								.description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.scrolling.scroll-creative-menu-tabs.description")))
								.binding(true, () -> scrollCreativeMenuTabs, value -> scrollCreativeMenuTabs = value)
								.controller(TickBoxControllerBuilder::create)
								.build()
						)
						.build())
				.category(ConfigCategory.createBuilder()
						.name(Text.translatable("config.screen.mousewheelie.sort"))
						.tooltip(Text.translatable("config.screen.mousewheelie.sort.description"))
						.option(Option.<SORTMODES>createBuilder()
								.name(Text.translatable("config.screen.mousewheelie.sort.primary-sort"))
								.description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.sort.primary-sort.description")))
								.binding(SORTMODES.CREATIVE, () -> primarySort, value -> primarySort = value)
								.controller(EnumDropdownControllerBuilder::create)
								.build()
						)
						.build())
				.build()
				.generateScreen(parent);
	
		return null;
	}
	
	
	
	// General
	@SerialEntry
	public static int interactionRate = 10;
	
	public static int integratedInteractionRate = 1;
	
	public static boolean enableQuickArmorSwapping = true;
	
	public static boolean enableDropModifier = true;
	
	public static boolean enableQuickCraft = true;
	
	public static ItemStackUtils.ComponentTypeMatchMode itemComponentMatchMode = ItemStackUtils.ComponentTypeMatchMode.SOME;
	
	public enum HotbarScoping {HARD, SOFT, NONE}
	
	public static HotbarScoping hotbarScoping = HotbarScoping.SOFT;
	
	public static boolean betterFastDragging = false;
	
	public static boolean enableBundleDragging = true;
	
	
	// Scrolling
	public static boolean enable = true;
	
	public static boolean invert = false;
	
	public static boolean directionalScrolling = true;
	
	public static boolean scrollCreativeMenuItems = true;
	
	public static boolean scrollCreativeMenuTabs = true;
	
	
	// Sort
	public enum SORTMODES {
		NONE, ALPHABET, CREATIVE, QUANTITY, RAW_ID
	}
	
	public static SORTMODES primarySort = SORTMODES.CREATIVE;
	
	public static SORTMODES shiftSort = SORTMODES.QUANTITY;
	
	public static SORTMODES controlSort = SORTMODES.ALPHABET;
	
	public static boolean serverAcceleratedSorting = true;
	
	public static boolean optimizeCreativeSearchSort = true;
	
	

	public static Refill refill = new Refill();

	@AConfigBackground("textures/block/horn_coral_block.png")
	public static class Refill {
		public boolean enable = true;

		public boolean playSound = true;

		public boolean offHand = true;
		public boolean restoreSelectedSlot = false;

		public boolean itemChanges = true;

		public boolean eat = true;
		public boolean drop = true;
		public boolean use = true;
		public boolean other = true;

		public Rules rules = new Rules();

		@AConfigBackground("textures/block/yellow_terracotta.png")
		public static class Rules {
			public boolean anyBlock = false;
			public boolean itemgroup = false;
			public boolean itemHierarchy = false;
			public boolean blockHierarchy = false;
			public boolean food = false;
			public boolean equalItems = true;
			public boolean equalStacks = true;
		}
	}

	public static ToolPicking toolPicking = new ToolPicking();

	@AConfigBackground("textures/block/coarse_dirt.png")
	public static class ToolPicking {
		public boolean holdTool = true;
		public boolean holdBlock = false;
		public boolean pickFromInventory = true;
	}

	@AConfigFixer
	public <V extends DataValue<V, L, O>, L extends DataList<V, L, O>, O extends DataObject<V, L, O>>
	void fixConfig(O dataObject, O rootObject) {
		if (dataObject.has("general") && dataObject.get("general").isObject()) {
			O general = dataObject.get("general").asObject();

			moveConfigEntry(dataObject, general, "enable-item-scrolling", "scrolling");
			moveConfigEntry(dataObject, general, "scroll-factor", "scrolling");
			moveConfigEntry(dataObject, general, "directional-scrolling", "scrolling");

			if (dataObject.has("scrolling") && dataObject.get("scrolling").isObject()) {
				O scrolling = dataObject.get("scrolling").asObject();

				if (scrolling.has("scroll-creative-menu") && scrolling.get("scroll-creative-menu").isBoolean()) {
					scrolling.set("scroll-creative-menu-items", !scrolling.get("scroll-creative-menu").asBoolean());
					scrolling.remove("scroll-creative-menu");
				}
				if (scrolling.has("scroll-factor") && scrolling.get("scroll-factor").isNumber()) {
					scrolling.set("invert", scrolling.get("scroll-factor").asFloat() < 0);
					scrolling.remove("scroll-factor");
				}
			}

			moveConfigEntry(dataObject, general, "hold-tool-pick", "tool-picking", "hold-tool");
			moveConfigEntry(dataObject, general, "hold-block-tool-pick", "tool-picking", "hold-block");

			moveConfigEntry(dataObject, general, "enable-alt-dropping", "general", "enable-drop-modifier");

			general.remove("hotbar-scope");
		}
	}

	@AConfigFixer("sort")
	public <V extends DataValue<V, L, O>, L extends DataList<V, L, O>, O extends DataObject<V, L, O>>
	void fixSortModes(O sort, O mainConfig) {
		if (!sort.has("optimize-creative-search-sort")) {
			if (sort.getString("primary-sort", "").equalsIgnoreCase("raw_id")) {
				sort.set("primary-sort", "creative");
			}
			if (sort.getString("shift-sort", "").equalsIgnoreCase("raw_id")) {
				sort.set("shift-sort", "creative");
			}
			if (sort.getString("control-sort", "").equalsIgnoreCase("raw_id")) {
				sort.set("control-sort", "creative");
			}
		}
	}

	@SuppressWarnings("SameParameterValue")
	private <V extends DataValue<V, L, O>, L extends DataList<V, L, O>, O extends DataObject<V, L, O>>
	void moveConfigEntry(O root, O origin, String name, String destCat) {
		moveConfigEntry(root, origin, name, destCat, name);
	}

	private <V extends DataValue<V, L, O>, L extends DataList<V, L, O>, O extends DataObject<V, L, O>>
	void moveConfigEntry(O root, O origin, String name, String destCat, String newName) {
		if (origin.has(name)) {
			O dest;
			if (root.has(destCat) && root.get(destCat).isObject()) {
				dest = root.get(destCat).asObject();
			} else {
				dest = root.addObject(destCat);
			}
			dest.set(newName, origin.get(name));
			origin.remove(name);
		}
	}
}
