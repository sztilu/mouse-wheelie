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

import de.siphalor.mousewheelie.client.util.ItemStackUtils;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@SuppressWarnings({"WeakerAccess", "unused"})
public class MWConfig {
    
    public static ConfigClassHandler<MWConfig> HANDLER = ConfigClassHandler.createBuilder(MWConfig.class)
            .id(new Identifier("mouse-wheelie-config", "mousewheelie"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("mouse-wheelie-config.json5"))
                    .setJson5(true)
                    .build()
            )
            .build();
    
    public static Screen createConfigScreen(Screen parent) {
        return YetAnotherConfigLib.createBuilder()
                .title(Text.translatable("config.screen.mousewheelie"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.translatable("config.screen.mousewheelie.general"))
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("config.screen.mousewheelie.general.interaction-rate"))
                                .binding(10, () -> interactionRate, value -> interactionRate = value)
                                .controller(opt -> IntegerFieldControllerBuilder.create(opt)
                                        .min(1).max(Integer.MAX_VALUE)
                                )
                                .build()
                        )
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("config.screen.mousewheelie.general.integrated-interaction-rate"))
                                .description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.general.integrated-interaction-rate.description")))
                                .binding(1, () -> integratedInteractionRate, value -> integratedInteractionRate = value)
                                .controller(opt -> IntegerFieldControllerBuilder.create(opt)
                                        .min(1).max(Integer.MAX_VALUE)
                                )
                                .build()
                        )
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
                                .controller(opt -> EnumControllerBuilder.create(opt)
                                        .enumClass(ItemStackUtils.ComponentTypeMatchMode.class))
                                .build()
                        )
                        .option(Option.<HotbarScoping>createBuilder()
                                .name(Text.translatable("config.screen.mousewheelie.general.hotbar-scoping"))
                                .description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.general.hotbar-scoping.description")))
                                .binding(HotbarScoping.SOFT, () -> hotbarScoping, value -> hotbarScoping = value)
                                .controller(opt -> EnumControllerBuilder.create(opt)
                                        .enumClass(HotbarScoping.class))
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
                        .build()
                )
                .category(ConfigCategory.createBuilder()
                        .name(Text.translatable("config.screen.mousewheelie.scrolling"))
                        .tooltip(Text.translatable("config.screen.mousewheelie.scrolling.description"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("config.screen.mousewheelie.scrolling.enable"))
                                .binding(true, () -> enableScrolling, value -> enableScrolling = value)
                                .controller(TickBoxControllerBuilder::create)
                                .build()
                        )
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("config.screen.mousewheelie.scrolling.invert"))
                                .description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.scrolling.invert.description")))
                                .binding(false, () -> invertScrolling, value -> invertScrolling = value)
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
                        .build()
                )
                .category(ConfigCategory.createBuilder()
                        .name(Text.translatable("config.screen.mousewheelie.sort"))
                        .tooltip(Text.translatable("config.screen.mousewheelie.sort.description"))
                        .option(Option.<SortModes>createBuilder()
                                .name(Text.translatable("config.screen.mousewheelie.sort.primary-sort"))
                                .description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.sort.primary-sort.description")))
                                .binding(SortModes.CREATIVE, () -> primarySort, value -> primarySort = value)
                                .controller(opt -> EnumControllerBuilder.create(opt)
                                        .enumClass(SortModes.class))
                                .build()
                        )
                        .option(Option.<SortModes>createBuilder()
                                .name(Text.translatable("config.screen.mousewheelie.sort.shift-sort"))
                                .description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.sort.shift-sort.description")))
                                .binding(SortModes.QUANTITY, () -> shiftSort, value -> shiftSort = value)
                                .controller(opt -> EnumControllerBuilder.create(opt)
                                        .enumClass(SortModes.class))
                                .build()
                        )
                        .option(Option.<SortModes>createBuilder()
                                .name(Text.translatable("config.screen.mousewheelie.sort.control-sort"))
                                .description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.sort.control-sort.description")))
                                .binding(SortModes.ALPHABET, () -> controlSort, value -> controlSort = value)
                                .controller(opt -> EnumControllerBuilder.create(opt)
                                        .enumClass(SortModes.class))
                                .build()
                        )
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("config.screen.mousewheelie.sort.server-accelerated-sorting"))
                                .description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.sort.server-accelerated-sorting.description")))
                                .binding(true, () -> serverAcceleratedSorting, value -> serverAcceleratedSorting = value)
                                .controller(TickBoxControllerBuilder::create)
                                .build()
                        )
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("config.screen.mousewheelie.sort.optimize-creative-search-sort"))
                                .description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.sort.optimize-creative-search-sort.description")))
                                .binding(true, () -> optimizeCreativeSearchSort, value -> optimizeCreativeSearchSort = value)
                                .controller(TickBoxControllerBuilder::create)
                                .build()
                        )
                        .build()
                )
                .category(ConfigCategory.createBuilder()
                        .name(Text.translatable("config.screen.mousewheelie.refill"))
                        .tooltip(Text.translatable("config.screen.mousewheelie.refill.description"))
                        .group(OptionGroup.createBuilder()
                                .name(Text.translatable("config.screen.mousewheelie.refill.rules"))
                                .description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.refill.rules.description")))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("config.screen.mousewheelie.refill.rules.any-block"))
                                        .description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.refill.rules.any-block.description")))
                                        .binding(false, () -> refillAnyBlock, value -> refillAnyBlock = value)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build()
                                )
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("config.screen.mousewheelie.refill.rules.itemgroup"))
                                        .description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.refill.rules.itemgroup.description")))
                                        .binding(false, () -> refillItemgroup, value -> refillItemgroup = value)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build()
                                )
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("config.screen.mousewheelie.refill.rules.item-hierarchy"))
                                        .description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.refill.rules.item-hierarchy.description")))
                                        .binding(false, () -> refillItemHierarchy, value -> refillItemHierarchy = value)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build()
                                )
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("config.screen.mousewheelie.refill.rules.block-hierarchy"))
                                        .description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.refill.rules.block-hierarchy.description")))
                                        .binding(false, () -> refillBlockHierarchy, value -> refillBlockHierarchy = value)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build()
                                )
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("config.screen.mousewheelie.refill.rules.food"))
                                        .description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.refill.rules.food.description")))
                                        .binding(false, () -> refillAnyFood, value -> refillAnyFood = value)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build()
                                )
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("config.screen.mousewheelie.refill.rules.equal-items"))
                                        .description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.refill.rules.equal-items.description")))
                                        .binding(true, () -> refillEqualItems, value -> refillEqualItems = value)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build()
                                )
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("config.screen.mousewheelie.refill.rules.equal-stacks"))
                                        .description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.refill.rules.equal-stacks.description")))
                                        .binding(true, () -> refillEqualStacks, value -> refillEqualStacks = value)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build()
                                )
                                .build()
                        )
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("config.screen.mousewheelie.refill.enable"))
                                .description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.refill.enable.description")))
                                .binding(true, () -> enableRefill, value -> enableRefill = value)
                                .controller(TickBoxControllerBuilder::create)
                                .build()
                        )
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("config.screen.mousewheelie.refill.play-sound"))
                                .description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.refill.play-sound.description")))
                                .binding(true, () -> playSound, value -> playSound = value)
                                .controller(TickBoxControllerBuilder::create)
                                .build()
                        )
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("config.screen.mousewheelie.refill.off-hand"))
                                .description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.refill.off-hand.description")))
                                .binding(true, () -> offHandRefill, value -> offHandRefill = value)
                                .controller(TickBoxControllerBuilder::create)
                                .build()
                        )
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("config.screen.mousewheelie.refill.restore-selected-slot"))
                                .description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.refill.restore-selected-slot.description")))
                                .binding(false, () -> restoreSelectedSlot, value -> restoreSelectedSlot = value)
                                .controller(TickBoxControllerBuilder::create)
                                .build()
                        )
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("config.screen.mousewheelie.refill.item-changes"))
                                .description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.refill.item-changes.description")))
                                .binding(true, () -> itemChanges, value -> itemChanges = value)
                                .controller(TickBoxControllerBuilder::create)
                                .build()
                        )
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("config.screen.mousewheelie.refill.eat"))
                                .description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.refill.eat.description")))
                                .binding(true, () -> refillOnEat, value -> refillOnEat = value)
                                .controller(TickBoxControllerBuilder::create)
                                .build()
                        )
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("config.screen.mousewheelie.refill.drop"))
                                .description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.refill.drop.description")))
                                .binding(true, () -> refillOnDrop, value -> refillOnDrop = value)
                                .controller(TickBoxControllerBuilder::create)
                                .build()
                        )
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("config.screen.mousewheelie.refill.use"))
                                .description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.refill.use.description")))
                                .binding(true, () -> refillOnUse, value -> refillOnUse = value)
                                .controller(TickBoxControllerBuilder::create)
                                .build()
                        )
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("config.screen.mousewheelie.refill.other"))
                                .description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.refill.other.description")))
                                .binding(true, () -> refillOnOtherOccasions, value -> refillOnOtherOccasions = value)
                                .controller(TickBoxControllerBuilder::create)
                                .build()
                        )
                        .build()
                )
                .category(ConfigCategory.createBuilder()
                        .name(Text.translatable("config.screen.mousewheelie.tool-picking"))
                        .tooltip(Text.translatable("config.screen.mousewheelie.tool-picking.description"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("config.screen.mousewheelie.tool-picking.hold-tool"))
                                .description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.tool-picking.hold-tool.description")))
                                .binding(true, () -> holdTool, value -> holdTool = value)
                                .controller(TickBoxControllerBuilder::create)
                                .build()
                        )
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("config.screen.mousewheelie.tool-picking.hold-block"))
                                .description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.tool-picking.hold-block.description")))
                                .binding(false, () -> holdBlock, value -> holdBlock = value)
                                .controller(TickBoxControllerBuilder::create)
                                .build()
                        )
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("config.screen.mousewheelie.tool-picking.pick-from-inventory"))
                                .description(OptionDescription.of(Text.translatable("config.screen.mousewheelie.tool-picking.pick-from-inventory.description")))
                                .binding(true, () -> pickFromInventory, value -> pickFromInventory = value)
                                .controller(TickBoxControllerBuilder::create)
                                .build()
                        )
                        .build())
                .save(() -> HANDLER.save())
                .build()
                .generateScreen(parent);
    }
    
    
    // General
    @SerialEntry
    public static int interactionRate = 10;
	
	@SerialEntry
    public static int integratedInteractionRate = 1;
	
	@SerialEntry
    public static boolean enableQuickArmorSwapping = true;
	
	@SerialEntry
    public static boolean enableDropModifier = true;
	
	@SerialEntry
    public static boolean enableQuickCraft = true;
	
	@SerialEntry
    public static ItemStackUtils.ComponentTypeMatchMode itemComponentMatchMode = ItemStackUtils.ComponentTypeMatchMode.SOME;
    
    public enum HotbarScoping {HARD, SOFT, NONE}
	
	@SerialEntry
    public static HotbarScoping hotbarScoping = HotbarScoping.SOFT;
	
	@SerialEntry
    public static boolean betterFastDragging = false;
	
	@SerialEntry
    public static boolean enableBundleDragging = true;
    
    
    // Scrolling
	@SerialEntry
    public static boolean enableScrolling = true;
	
	@SerialEntry
    public static boolean invertScrolling = false;
	
	@SerialEntry
    public static boolean directionalScrolling = true;
	
	@SerialEntry
    public static boolean scrollCreativeMenuItems = true;
	
	@SerialEntry
    public static boolean scrollCreativeMenuTabs = true;
    
    
    // Sort
    public enum SortModes {
        NONE, ALPHABET, CREATIVE, QUANTITY, RAW_ID
    }
	
	@SerialEntry
    public static SortModes primarySort = SortModes.CREATIVE;
	
	@SerialEntry
    public static SortModes shiftSort = SortModes.QUANTITY;
	
	@SerialEntry
    public static SortModes controlSort = SortModes.ALPHABET;
	
	@SerialEntry
    public static boolean serverAcceleratedSorting = true;
	
	@SerialEntry
    public static boolean optimizeCreativeSearchSort = true;
    
    
    // Refill
	@SerialEntry
    public static boolean enableRefill = true;
	
	@SerialEntry
    public static boolean playSound = true;
	
	@SerialEntry
    public static boolean offHandRefill = true;
	
	@SerialEntry
    public static boolean restoreSelectedSlot = false;
	
	@SerialEntry
    public static boolean itemChanges = true;
	
	@SerialEntry
    public static boolean refillOnEat = true;
	
	@SerialEntry
    public static boolean refillOnDrop = true;
	
	@SerialEntry
    public static boolean refillOnUse = true;
	
	@SerialEntry
    public static boolean refillOnOtherOccasions = true;
    
    
    // Refill Rules
	@SerialEntry
    public static boolean refillAnyBlock = false;
	
	@SerialEntry
    public static boolean refillItemgroup = false;
	
	@SerialEntry
    public static boolean refillItemHierarchy = false;
	
	@SerialEntry
    public static boolean refillBlockHierarchy = false;
	
	@SerialEntry
    public static boolean refillAnyFood = false;
	
	@SerialEntry
    public static boolean refillEqualItems = true;
	
	@SerialEntry
    public static boolean refillEqualStacks = true;
    
    
    // Tool Picking
	@SerialEntry
    public static boolean holdTool = true;
	
	@SerialEntry
    public static boolean holdBlock = false;
	
	@SerialEntry
    public static boolean pickFromInventory = true;
}
