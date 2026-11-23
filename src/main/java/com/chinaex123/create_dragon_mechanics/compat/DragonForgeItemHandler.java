package com.chinaex123.create_dragon_mechanics.compat;

import com.iafenvoy.iceandfire.registry.IafItems;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

/**
 * 龙煅炉物品处理器。
 * <p>
 * 以三槽结构（输入槽、催化剂槽、输出槽）包装龙煅炉的容器，
 * 面向物品管道等外部接口：仅允许向输入槽与催化剂槽插入合适物品，
 * 仅允许从输出槽抽取，并保证方块实体仍有效时才进行读写。
 */
public class DragonForgeItemHandler implements IItemHandlerModifiable {

    /** 输入槽位索引。 */
    private static final int INPUT_SLOT = 0;
    /** 催化剂槽位索引。 */
    private static final int CATALYST_SLOT = 1;
    /** 输出槽位索引。 */
    private static final int OUTPUT_SLOT = 2;
    /** 槽位总数。 */
    private static final int SLOT_COUNT = 3;
    /** 被包装的容器。 */
    private final Container container;

    /**
     * 构造龙煅炉物品处理器。
     *
     * @param container 被包装的容器
     */
    public DragonForgeItemHandler(Container container) {
        this.container = container;
    }

    /**
     * 获取槽位数量。
     *
     * @return 槽位数量
     */
    @Override
    public int getSlots() {
        return SLOT_COUNT;
    }

    /**
     * 将槽位索引限制在有效范围内。
     *
     * @param slot 原始槽位索引
     * @return 有效槽位索引
     */
    private static int clampSlot(int slot) {
        return slot < 0 ? 0 : Math.min(slot, SLOT_COUNT - 1);
    }

    /**
     * 判断容器是否仍然有效。
     * <p>
     * 方块实体需未被移除且处于已加载世界中；非方块实体容器视为有效。
     *
     * @return 有效返回 true
     */
    private boolean stillValid() {
        if (this.container instanceof BlockEntity blockEntity) {
            return !blockEntity.isRemoved() && blockEntity.getLevel() != null;
        }
        return true;
    }

    /**
     * 判断物品是否属于已知的催化剂。
     * <p>
     * 包括玻璃瓶与三种龙血。
     *
     * @param stack 物品堆叠
     * @return 属于已知催化剂返回 true
     */
    private static boolean isKnownCatalyst(ItemStack stack) {
        return stack.is(Items.GLASS_BOTTLE)
                || stack.is(IafItems.FIRE_DRAGON_BLOOD.get())
                || stack.is(IafItems.ICE_DRAGON_BLOOD.get())
                || stack.is(IafItems.LIGHTNING_DRAGON_BLOOD.get());
    }

    /**
     * 判断催化剂槽是否接受该物品。
     * <p>
     * 已知催化剂直接接受；否则回退到容器的放置判定，
     * 容器判定抛出异常时视为不接受。
     *
     * @param stack 物品堆叠
     * @return 接受返回 true
     */
    private boolean catalystAccepts(ItemStack stack) {
        if (isKnownCatalyst(stack)) {
            return true;
        }
        try {
            return this.container.canPlaceItem(CATALYST_SLOT, stack);
        } catch (RuntimeException | AssertionError e) {
            return false;
        }
    }

    /**
     * 判断指定槽位是否接受该物品。
     * <p>
     * 空物品与输出槽一律不接受；其余槽位的接受条件
     * 需与「是否为催化剂」的判定一致。
     *
     * @param slot  槽位索引
     * @param stack 物品堆叠
     * @return 接受返回 true
     */
    private boolean accepts(int slot, ItemStack stack) {
        if (stack.isEmpty() || slot == OUTPUT_SLOT) {
            return false;
        }
        return (slot == CATALYST_SLOT) == this.catalystAccepts(stack);
    }

    /**
     * 获取指定槽位的物品。
     *
     * @param slot 槽位索引
     * @return 物品堆叠，容器无效时为空
     */
    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return this.stillValid() ? this.container.getItem(clampSlot(slot)) : ItemStack.EMPTY;
    }

    /**
     * 向指定槽位插入物品。
     * <p>
     * 容器无效、槽位不接受或与已有物品不一致时原样返回；
     * 按槽位与物品堆叠上限计算可插入数量，模拟时不实际写入，
     * 返回未能插入的剩余部分。
     *
     * @param slot     槽位索引
     * @param stack    待插入物品
     * @param simulate 是否仅模拟
     * @return 未能插入的剩余物品
     */
    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int index = clampSlot(slot);
        if (!this.stillValid() || !this.accepts(index, stack)) {
            return stack;
        }

        ItemStack existing = this.container.getItem(index);
        int limit = Math.min(this.getSlotLimit(index), stack.getMaxStackSize());
        if (!existing.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(stack, existing)) {
                return stack;
            }
            limit -= existing.getCount();
        }
        if (limit <= 0) {
            return stack;
        }

        boolean reachedLimit = stack.getCount() > limit;
        if (!simulate) {
            if (existing.isEmpty()) {
                this.container.setItem(index, reachedLimit ? stack.copyWithCount(limit) : stack.copy());
            } else {
                existing.grow(reachedLimit ? limit : stack.getCount());
                this.container.setChanged();
            }
        }
        return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
    }

    /**
     * 从指定槽位抽取物品。
     * <p>
     * 仅允许从输出槽抽取，且容器必须有效；模拟时不实际写入，
     * 返回抽取到的物品。
     *
     * @param slot     槽位索引
     * @param amount   抽取数量
     * @param simulate 是否仅模拟
     * @return 抽取到的物品
     */
    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        int index = clampSlot(slot);
        if (index != OUTPUT_SLOT || amount <= 0 || !this.stillValid()) {
            return ItemStack.EMPTY;
        }

        ItemStack existing = this.container.getItem(index);
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int toExtract = Math.min(amount, existing.getMaxStackSize());
        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                this.container.setItem(index, ItemStack.EMPTY);
                this.container.setChanged();
            }
            return existing.copy();
        }

        if (!simulate) {
            this.container.setItem(index, existing.copyWithCount(existing.getCount() - toExtract));
            this.container.setChanged();
        }
        return existing.copyWithCount(toExtract);
    }

    /**
     * 获取指定槽位的堆叠上限。
     *
     * @param slot 槽位索引
     * @return 堆叠上限，不超过 64
     */
    @Override
    public int getSlotLimit(int slot) {
        return Math.min(this.container.getMaxStackSize(), 64);
    }

    /**
     * 判断指定槽位是否接受该物品。
     *
     * @param slot  槽位索引
     * @param stack 物品堆叠
     * @return 接受返回 true
     */
    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return this.accepts(clampSlot(slot), stack);
    }

    /**
     * 直接设置指定槽位的物品。
     * <p>
     * 容器无效时忽略；若目标槽位不接受该物品，
     * 则尝试改放入输入槽或催化剂槽，均不接受时忽略。
     *
     * @param slot  槽位索引
     * @param stack 物品堆叠
     */
    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        int index = clampSlot(slot);
        if (!this.stillValid()) {
            return;
        }
        if (!stack.isEmpty() && !this.accepts(index, stack)) {
            if (this.accepts(INPUT_SLOT, stack)) {
                index = INPUT_SLOT;
            } else if (this.accepts(CATALYST_SLOT, stack)) {
                index = CATALYST_SLOT;
            } else {
                return;
            }
        }
        this.container.setItem(index, stack);
        this.container.setChanged();
    }
}
