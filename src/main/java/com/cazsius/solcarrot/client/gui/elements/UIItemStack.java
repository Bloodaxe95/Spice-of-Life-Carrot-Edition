package com.cazsius.solcarrot.client.gui.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import java.awt.*;
import java.util.List;

import static net.minecraft.client.util.ITooltipFlag.TooltipFlags.ADVANCED;
import static net.minecraft.client.util.ITooltipFlag.TooltipFlags.NORMAL;

public class UIItemStack extends UIElement {
	public static final int size = 16;
	
	public ItemStack itemStack;
	
	public UIItemStack(ItemStack itemStack) {
		super(new Rectangle(size, size));
		
		this.itemStack = itemStack;
	}
	
	@Override
	protected void render(MatrixStack matrices) {
		super.render(matrices);
		
		mc.getItemRenderer().renderGuiItem(
			// no MatrixStack? oof
			itemStack,
			frame.x + (frame.width - size) / 2,
			frame.y + (frame.height - size) / 2
		);
	}
	
	@Override
	protected boolean hasTooltip() {
		return true;
	}
	
	@Override
	protected void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
		List<ITextComponent> tooltip = itemStack.getTooltipLines(mc.player, mc.options.advancedItemTooltips ? ADVANCED : NORMAL);
		renderTooltip(matrices, itemStack, tooltip, mouseX, mouseY);
	}
}
