/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createdragonsplus.client.ponder.scenes;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.math.Pointing;
import com.zurrtum.create.client.foundation.ponder.CreateSceneBuilder;
import com.zurrtum.create.client.ponder.api.PonderPalette;
import com.zurrtum.create.client.ponder.api.scene.SceneBuilder;
import com.zurrtum.create.client.ponder.api.scene.SceneBuildingUtil;
import com.zurrtum.create.content.decoration.placard.PlacardBlock;
import com.zurrtum.create.content.decoration.placard.PlacardBlockEntity;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import com.zurrtum.create.content.logistics.depot.DepotBlockEntity;
import com.zurrtum.create.content.processing.basin.BasinBlockEntity;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import java.util.List;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import plus.dragons.createdragonsplus.fluid.FluidUnits;
import plus.dragons.createdragonsplus.registry.CDPFluids;
import plus.dragons.createdragonsplus.registry.CDPTags;

public final class CDPPonderScenesImpl {
    private static final Identifier LIME = Identifier.withDefaultNamespace("lime");

    private CDPPonderScenesImpl() {}

    public static void bulkColoring(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("bulk_coloring", "Bulk Coloring");
        scene.configureBasePlate(1, 0, 5);
        var base = util.select().layer(0).add(util.select().position(6, 3, 1));
        scene.world().showSection(base, Direction.UP);
        scene.idle(5);
        var fan = util.select().fromTo(5, 2, 1, 6, 2, 2);
        scene.world().showSection(fan, Direction.DOWN);
        scene.idle(5);
        var fanBase = util.select().fromTo(4, 1, 2, 5, 1, 2);
        scene.world().showSection(fanBase, Direction.UP);
        scene.idle(10);
        var blockInFront = scene.world().makeSectionIndependent(util.select().position(3, 1, 2));
        scene.world().moveSection(blockInFront, util.vector().of(1, 1, 0), 0);
        scene.world().setBlock(util.grid().at(4, 2, 2), dyeBlock(), false);
        scene.idle(10);

        var airCurrent = util.select().fromTo(4, 2, 2, 1, 2, 2);
        scene.overlay().showOutline(PonderPalette.GREEN, airCurrent, airCurrent, 20);
        scene.idle(40);

        var belt = util.select().fromTo(0, 1, 1, 2, 2, 4);
        scene.world().showSection(belt, Direction.DOWN);
        var transported = scene.world()
                .createItemOnBelt(util.grid().at(2, 1, 3), Direction.SOUTH, Items.RED_WOOL.getDefaultInstance());
        scene.overlay()
                .showText(80)
                .pointAt(util.vector().topOf(2, 1, 2))
                .attachKeyFrame()
                .text("Air Flows passing through Liquid Dye create a Coloring Setup");
        scene.idle(80);
        scene.world().changeBeltItemTo(transported, Items.LIME_WOOL.getDefaultInstance());
        scene.idle(80);
        scene.world().removeItemsFromBelt(util.grid().at(2, 1, 1));
        scene.idle(10);

        ItemStack dough = AllItems.DOUGH.getDefaultInstance();
        ItemStack slimeBall = Items.SLIME_BALL.getDefaultInstance();
        var placard = AllBlocks.PLACARD.defaultBlockState()
                .setValue(PlacardBlock.FACE, AttachFace.WALL)
                .setValue(PlacardBlock.FACING, Direction.NORTH);
        var placardPos0 = util.grid().at(2, 2, 3);
        var placardPos1 = util.grid().at(1, 1, 1);
        scene.world().setBlock(placardPos0, placard, true);
        scene.world().modifyBlockEntity(placardPos0, PlacardBlockEntity.class, entity -> entity.setHeldItem(dough));
        scene.world().setBlock(placardPos1, placard, true);
        scene.world().modifyBlockEntity(placardPos1, PlacardBlockEntity.class, entity -> entity.setHeldItem(slimeBall));
        transported = scene.world().createItemOnBelt(util.grid().at(2, 1, 3), Direction.SOUTH, dough);
        scene.overlay()
                .showText(80)
                .pointAt(util.vector().topOf(2, 1, 2))
                .attachKeyFrame()
                .text("Like Bulk Blasting and Bulk Smoking, Bulk Coloring is automatically compatible with certain crafting recipes using dyes");
        scene.idle(80);
        scene.world().changeBeltItemTo(transported, slimeBall);
        scene.idle(80);
        scene.world().removeItemsFromBelt(util.grid().at(2, 1, 1));
        scene.idle(10);

        scene.world().setKineticSpeed(belt.add(util.select().position(0, 0, 4)), 0);
        BlockPos entityPos = util.grid().at(1, 2, 2);
        var sheep = scene.world().createEntity(level -> createSheep(level, entityPos.getBottomCenter()));
        scene.overlay()
                .showText(80)
                .pointAt(util.vector().topOf(entityPos))
                .attachKeyFrame()
                .text("Coloring Air Flow can also affect entities, changing their color.");
        scene.idle(80);
        scene.world().modifyEntity(sheep, entity -> ((Sheep) entity).setColor(DyeColor.LIME));
        scene.idle(80);
        scene.world().modifyEntity(sheep, Entity::discard);
        scene.idle(10);

        var armorStand = scene.world().createEntity(level -> createLeatherArmorStand(level, entityPos.getBottomCenter()));
        scene.overlay()
                .showText(80)
                .pointAt(util.vector().topOf(entityPos))
                .attachKeyFrame()
                .text("Equipped armor will also be colored.");
        scene.idle(80);
        scene.world().modifyEntity(armorStand, CDPPonderScenesImpl::dyeArmorStand);
        scene.idle(80);
        scene.world().modifyEntity(armorStand, Entity::discard);
        scene.idle(20);
    }

    public static void bulkFreezing(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("bulk_freezing", "Bulk Freezing");
        scene.configureBasePlate(0, 1, 5);
        scene.world().showSection(util.select().layer(0), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().fromTo(2, 1, 4, 2, 1, 5), Direction.UP);
        scene.world().showSection(util.select().fromTo(2, 2, 5, 2, 2, 7), Direction.DOWN);
        scene.idle(10);

        scene.overlay()
                .showControls(util.vector().centerOf(2, 3, 4), Pointing.DOWN, 20)
                .withItem(Items.POWDER_SNOW_BUCKET.getDefaultInstance())
                .rightClick();
        var snow = scene.world().showIndependentSection(util.select().position(2, 3, 4), Direction.DOWN);
        scene.world().moveSection(snow, new Vec3(0, -1, 0), 0);

        scene.world().setKineticSpeed(util.select().position(3, 0, 0), 4);
        scene.world().setKineticSpeed(util.select().fromTo(2, 2, 5, 2, 2, 6), -8);
        scene.idle(30);
        var airCurrent = util.select().fromTo(2, 2, 1, 2, 2, 4);
        scene.overlay().showOutline(PonderPalette.WHITE, airCurrent, airCurrent, 20);
        scene.idle(40);

        scene.overlay()
                .showText(80)
                .pointAt(util.vector().topOf(2, 1, 2))
                .attachKeyFrame()
                .text("Air Flows passing through Powder Snow create a Freezing Setup");
        var belt = util.select()
                .fromTo(0, 1, 2, 5, 1, 2)
                .add(util.select().fromTo(4, 1, 1, 4, 1, 0));
        scene.world().setKineticSpeed(belt, -4);
        scene.world().showSection(belt, Direction.DOWN);
        var transported = scene.world()
                .createItemOnBelt(util.grid().at(1, 1, 2), Direction.DOWN, Items.BLAZE_ROD.getDefaultInstance());
        scene.idle(160);
        scene.world().changeBeltItemTo(transported, Items.BREEZE_ROD.getDefaultInstance());
        scene.idle(30);
    }

    public static void bulkSanding(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        BlockState catalyst = Blocks.SAND.defaultBlockState();
        for (var holder : BuiltInRegistries.BLOCK.getTagOrEmpty(CDPTags.FAN_SANDING_CATALYSTS)) {
            catalyst = holder.value().defaultBlockState();
            break;
        }
        scene.world().setBlock(util.grid().at(3, 2, 3), catalyst, false);

        scene.title("bulk_sanding", "Bulk Sanding");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().fromTo(3, 1, 3, 4, 1, 3), Direction.UP);
        scene.idle(3);
        scene.world().showSection(util.select().fromTo(3, 2, 3, 4, 2, 3), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().position(5, 2, 3), Direction.WEST);
        scene.idle(5);
        scene.world().setKineticSpeed(util.select().fromTo(4, 2, 3, 5, 2, 3), -8);
        scene.idle(5);

        var airCurrent = util.select().fromTo(3, 2, 3, 0, 2, 3);
        scene.overlay().showOutline(PonderPalette.OUTPUT, airCurrent, airCurrent, 20);
        scene.idle(40);
        scene.overlay()
                .showText(80)
                .pointAt(util.vector().topOf(1, 1, 3))
                .attachKeyFrame()
                .text("Air Flows passing through Bulk Sanding Catalysts (Example: Quicksand) create a Sanding Setup");
        BlockPos firstDepot = util.grid().at(1, 1, 3);
        scene.world().showSection(util.select().position(firstDepot), Direction.DOWN);
        scene.idle(10);
        scene.world().modifyBlockEntity(
                firstDepot,
                DepotBlockEntity.class,
                depot -> setDepotItem(depot, Blocks.DIORITE.asItem().getDefaultInstance()));
        scene.idle(80);
        scene.world().modifyBlockEntity(
                firstDepot,
                DepotBlockEntity.class,
                depot -> setDepotItem(depot, Blocks.POLISHED_DIORITE.asItem().getDefaultInstance()));
        scene.idle(10);

        scene.world().setKineticSpeed(util.select().fromTo(4, 2, 3, 5, 2, 3), 0);
        scene.idle(3);
        scene.world().hideSection(util.select().position(5, 2, 3), Direction.EAST);
        scene.idle(3);
        scene.world().hideSection(util.select().position(4, 2, 3), Direction.UP);
        scene.idle(3);
        scene.world().hideSection(util.select().position(4, 1, 3), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(3, 1, 4), Direction.UP);
        scene.idle(3);
        scene.world().showSection(util.select().position(3, 2, 4), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().position(3, 2, 5), Direction.NORTH);
        scene.idle(3);
        scene.world().setKineticSpeed(util.select().fromTo(3, 2, 4, 3, 2, 6), -8);
        scene.idle(3);

        scene.overlay()
                .showText(80)
                .pointAt(util.vector().topOf(3, 1, 1))
                .attachKeyFrame()
                .text("Like Bulk Blasting and Bulk Smoking, Bulk Sanding is automatically compatible with Sand Paper polishing recipes");
        BlockPos secondDepot = util.grid().at(3, 1, 1);
        scene.world().showSection(util.select().position(secondDepot), Direction.DOWN);
        scene.idle(10);
        scene.world().modifyBlockEntity(
                secondDepot,
                DepotBlockEntity.class,
                depot -> setDepotItem(depot, AllItems.ROSE_QUARTZ.getDefaultInstance()));
        scene.idle(80);
        scene.world().modifyBlockEntity(
                secondDepot,
                DepotBlockEntity.class,
                depot -> setDepotItem(depot, AllItems.POLISHED_ROSE_QUARTZ.getDefaultInstance()));
        scene.idle(10);
    }

    public static void bulkEnding(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("bulk_ending", "Bulk Ending");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(
                util.select().layer(0).substract(util.select().position(1, 0, 5)), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().fromTo(3, 1, 3, 3, 1, 4), Direction.UP);
        scene.idle(3);
        scene.world().showSection(util.select().fromTo(1, 1, 3, 1, 1, 4), Direction.UP);
        scene.idle(3);
        scene.world().showSection(util.select().fromTo(3, 2, 3, 3, 2, 4), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(util.select().fromTo(1, 2, 3, 1, 2, 4), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(0, 0, 5, 4, 2, 5), Direction.NORTH);
        scene.idle(5);
        var bigCog = util.select().position(2, 1, 5);
        scene.world().setKineticSpeed(bigCog, 4);
        scene.world().setKineticSpeed(util.select().everywhere().substract(bigCog), -8);
        scene.idle(5);

        var airCurrent = util.select()
                .fromTo(1, 2, 0, 1, 2, 3)
                .add(util.select().fromTo(3, 2, 0, 3, 2, 3));
        scene.overlay().showOutline(PonderPalette.BLACK, airCurrent, airCurrent, 20);
        scene.idle(40);
        scene.overlay()
                .showText(80)
                .pointAt(util.vector().topOf(1, 1, 1))
                .attachKeyFrame()
                .text("Air Flows passing through Dragon Head or Dragon Breath create an Ending Setup");
        BlockPos stoneDepot = util.grid().at(1, 1, 1);
        BlockPos leatherDepot = util.grid().at(3, 1, 1);
        scene.world().showSection(
                util.select().position(stoneDepot).add(util.select().position(leatherDepot)), Direction.DOWN);
        scene.idle(10);
        scene.world().modifyBlockEntity(
                stoneDepot,
                DepotBlockEntity.class,
                depot -> setDepotItem(depot, Items.COBBLESTONE.getDefaultInstance()));
        scene.world().modifyBlockEntity(
                leatherDepot,
                DepotBlockEntity.class,
                depot -> setDepotItem(depot, Items.LEATHER.getDefaultInstance()));
        scene.idle(80);
        scene.world().modifyBlockEntity(
                stoneDepot,
                DepotBlockEntity.class,
                depot -> setDepotItem(depot, Items.END_STONE.getDefaultInstance()));
        scene.world().modifyBlockEntity(
                leatherDepot,
                DepotBlockEntity.class,
                depot -> setDepotItem(depot, Items.PHANTOM_MEMBRANE.getDefaultInstance()));
        scene.idle(40);
    }

    public static void dyeFluids(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("dye_fluids", "Liquid Dye");
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.85f);
        scene.showBasePlate();
        scene.idle(5);

        BlockPos basin = util.grid().at(1, 2, 2);
        BlockPos mixer = util.grid().at(1, 4, 2);
        var section = scene.world().showIndependentSection(
                util.select().position(basin).add(util.select().position(mixer)), Direction.DOWN);
        scene.world().moveSection(section, new Vec3(0, -1, 0), 0);
        scene.idle(10);
        scene.overlay()
                .showText(70)
                .attachKeyFrame()
                .pointAt(util.vector().topOf(basin.below()))
                .placeNearTarget()
                .text("Mixing dye with water creates Liquid Dye");
        scene.overlay()
                .showControls(util.vector().topOf(basin.below()).add(-0.55, 0, 0), Pointing.DOWN, 40)
                .withItem(Items.LIME_DYE.getDefaultInstance());
        scene.overlay()
                .showControls(util.vector().topOf(basin.below()).add(0.55, 0, 0), Pointing.DOWN, 40)
                .withItem(Items.WATER_BUCKET.getDefaultInstance());
        scene.world().createItemOnBeltLike(basin, Direction.UP, Items.LIME_DYE.getDefaultInstance());
        scene.world().modifyBlockEntity(basin, BasinBlockEntity.class, entity -> setBasinFluid(entity, Fluids.WATER));
        scene.idle(10);
        scene.world().setKineticSpeed(util.select().position(mixer), 32);
        scene.world().modifyBlockEntity(
                mixer, MechanicalMixerBlockEntity.class, MechanicalMixerBlockEntity::startProcessingBasin);
        scene.idle(40);
        scene.world().modifyBlockEntity(basin, BasinBlockEntity.class, entity -> {
            entity.itemCapability.clearContent();
            setBasinFluid(entity, CDPFluids.DYES_BY_VARIANT.get(LIME).getSource());
        });
        scene.idle(45);

        BlockPos burner = util.grid().at(1, 1, 2);
        scene.world().moveSection(section, new Vec3(0, 1, 0), 10);
        scene.idle(10);
        scene.world().showSection(util.select().position(burner), Direction.UP);
        scene.overlay()
                .showText(70)
                .attachKeyFrame()
                .pointAt(util.vector().topOf(basin))
                .placeNearTarget()
                .text("Heated mixing can recover the dye item from the fluid");
        scene.world().modifyBlockEntity(
                mixer, MechanicalMixerBlockEntity.class, MechanicalMixerBlockEntity::startProcessingBasin);
        scene.idle(40);
        scene.world().modifyBlockEntity(basin, BasinBlockEntity.class, entity -> {
            setBasinFluid(entity, Fluids.WATER);
            entity.itemCapability.setItem(0, Items.LIME_DYE.getDefaultInstance());
            entity.itemCapability.setChanged();
        });
        scene.idle(40);

        BlockPos pool = util.grid().at(3, 1, 2);
        scene.world().setBlock(pool, dyeBlock(), false);
        scene.world().showSection(util.select().position(pool), Direction.DOWN);
        scene.overlay()
                .showText(70)
                .attachKeyFrame()
                .pointAt(util.vector().topOf(pool))
                .placeNearTarget()
                .text("Items and living entities touching Liquid Dye are colored");
        scene.idle(5);
        var sheep = scene.world().createEntity(level -> createSheep(level, util.vector().centerOf(pool)));
        scene.idle(35);
        scene.world().modifyEntity(sheep, entity -> ((Sheep) entity).setColor(DyeColor.LIME));
        scene.idle(40);
        scene.world().modifyEntity(sheep, Entity::discard);
        scene.idle(20);

        BlockPos lava = util.grid().at(4, 1, 2);
        scene.world().setBlock(lava, Blocks.LAVA.defaultBlockState(), false);
        scene.world().showSection(util.select().position(lava), Direction.DOWN);
        scene.overlay()
                .showText(70)
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(lava))
                .placeNearTarget()
                .text("When Liquid Dye meets lava, it forms matching concrete");
        scene.idle(5);
        scene.world().setBlock(pool, Blocks.LIME_CONCRETE.defaultBlockState(), true);
        scene.idle(20);
        scene.world().showSection(util.select().position(4, 1, 3), Direction.DOWN);
        scene.idle(5);
        scene.world().setBlock(lava, Blocks.LIME_CONCRETE.defaultBlockState(), true);
        scene.idle(45);
    }

    public static void dragonBreathFluid(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("dragon_breath_fluid", "Dragon's Breath Fluid");
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.85f);
        scene.showBasePlate();
        scene.idle(5);

        var cauldron = util.grid().at(2, 1, 2);
        scene.world().showSection(util.select().position(cauldron), Direction.DOWN);
        scene.idle(3);
        scene.world().showSection(
                util.select()
                        .fromTo(2, 2, 2, 2, 3, 2)
                        .add(util.select().fromTo(1, 4, 1, 3, 4, 3)),
                Direction.DOWN);
        scene.overlay()
                .showText(75)
                .attachKeyFrame()
                .pointAt(util.vector().topOf(cauldron))
                .placeNearTarget()
                .text("Dragon's Breath supports Cauldron behaviour");
        scene.idle(85);

        var left = util.grid().at(3, 1, 0);
        scene.world().showSection(util.select().position(left), Direction.DOWN);
        scene.idle(10);
        var lava = util.grid().at(3, 2, 0);
        scene.world().setBlock(lava, Blocks.LAVA.defaultBlockState(), false);
        scene.world().showSection(util.select().position(lava), Direction.DOWN);
        scene.overlay()
                .showText(70)
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(lava))
                .placeNearTarget()
                .text("When flowing Dragon's Breath meets lava, it forms End Stone");
        scene.idle(5);
        scene.world().setBlock(left, Blocks.END_STONE.defaultBlockState(), true);
        scene.idle(75);
    }

    private static BlockState dyeBlock() {
        return CDPFluids.DYES_BY_VARIANT.get(LIME).getSource().defaultFluidState().createLegacyBlock();
    }

    private static Sheep createSheep(Level level, Vec3 position) {
        Sheep sheep = new Sheep(EntityType.SHEEP, level);
        sheep.setColor(DyeColor.WHITE);
        sheep.setPos(position.x, position.y, position.z);
        sheep.xo = position.x;
        sheep.yo = position.y;
        sheep.zo = position.z;
        WalkAnimationState animation = sheep.walkAnimation;
        animation.setSpeed(1);
        sheep.yRotO = 210;
        sheep.setYRot(210);
        sheep.yHeadRotO = 210;
        sheep.yHeadRot = 210;
        return sheep;
    }

    private static ArmorStand createLeatherArmorStand(Level level, Vec3 position) {
        ArmorStand armorStand = new ArmorStand(level, position.x, position.y, position.z);
        armorStand.setItemSlot(EquipmentSlot.FEET, Items.LEATHER_BOOTS.getDefaultInstance());
        armorStand.setItemSlot(EquipmentSlot.LEGS, Items.LEATHER_LEGGINGS.getDefaultInstance());
        armorStand.setItemSlot(EquipmentSlot.CHEST, Items.LEATHER_CHESTPLATE.getDefaultInstance());
        armorStand.setItemSlot(EquipmentSlot.HEAD, Items.LEATHER_HELMET.getDefaultInstance());
        armorStand.lookAt(Anchor.EYES, armorStand.getEyePosition().add(0, 0, -1));
        return armorStand;
    }

    private static void dyeArmorStand(Entity entity) {
        ArmorStand armorStand = (ArmorStand) entity;
        for (EquipmentSlot slot : List.of(
                EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD)) {
            ItemStack armor = armorStand.getItemBySlot(slot);
            armorStand.setItemSlot(slot, DyedItemColor.applyDyes(armor, List.of(DyeColor.LIME)));
        }
    }

    private static void setDepotItem(DepotBlockEntity depot, ItemStack stack) {
        depot.setHeldItem(new TransportedItemStack(stack));
    }

    private static void setBasinFluid(BasinBlockEntity basin, Fluid fluid) {
        basin.fluidCapability.clearContent();
        basin.fluidCapability.insert(new FluidStack(fluid, Math.toIntExact(FluidUnits.BUCKET)));
    }
}
