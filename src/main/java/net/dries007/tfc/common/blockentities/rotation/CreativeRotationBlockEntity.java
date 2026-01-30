/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.blockentities.rotation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blockentities.TickableBlockEntity;
import net.dries007.tfc.common.blocks.rotation.CreativeRotationBlock;
import net.dries007.tfc.util.network.Action;
import net.dries007.tfc.util.network.RotationNode;
import net.dries007.tfc.util.network.RotationOwner;

public class CreativeRotationBlockEntity extends TickableBlockEntity implements RotationOwner
{
    public static final float TORQUE = 30f;
    // 30 RPM
    public static final float MAX_SPEED = Mth.TWO_PI / (2 * 20);
    public static final int MAX_STEPS = 8;
    public static final float LERP_SPEED = MAX_SPEED / MAX_STEPS;

    public static void serverTick(Level level, BlockPos pos, BlockState state, CreativeRotationBlockEntity motor)
    {
        motor.checkForLastTickSync();
        float target = motor.step * LERP_SPEED;
        if (motor.targetSpeed != target)
        {
            motor.targetSpeed = target;
            motor.performNetworkAction(Action.UPDATE_IN_NETWORK);
            motor.markForSync();
        }
    }

    private final RotationNode node;
    private int step;
    private float targetSpeed = 0f;

    //TODO add a function to change rotation axis
    public CreativeRotationBlockEntity(BlockPos pos, BlockState state)
    {
        this(TFCBlockEntities.CREATIVE_MOTOR.get(), pos, state);
    }

    protected CreativeRotationBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
        Direction.Axis axis = state.getValue(CreativeRotationBlock.AXIS);
        this.node = new RotationNode.Axle(this, axis, TORQUE)
        {
            @Override
            protected float providedSpeed()
            {
                return CreativeRotationBlockEntity.this.targetSpeed;
            }

            @Override
            protected float providedTorque()
            {
                return TORQUE;
            }
        };
    }

    @Override
    public RotationNode getRotationNode()
    {
        return node;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider)
    {
        super.saveAdditional(tag, provider);
        node.saveAdditional(tag);
        tag.putInt("step", step);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider)
    {
        super.loadAdditional(tag, provider);
        node.loadAdditional(tag);
        step = tag.getInt("step");
    }

    @Override
    protected void onLoadAdditional()
    {
        performNetworkAction(Action.ADD);
    }

    @Override
    protected void onUnloadAdditional()
    {
        performNetworkAction(Action.REMOVE);
    }

    public void incrementSpeed()
    {
        step = Mth.clamp(step + 1, -MAX_STEPS, MAX_STEPS);
    }

    public void decrementSpeed()
    {
        step = Mth.clamp(step - 1, -MAX_STEPS, MAX_STEPS);
    }

}
