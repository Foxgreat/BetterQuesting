package betterquesting.api2.client.gui.panels.content;

import java.util.List;

import betterquesting.api2.client.gui.misc.*;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import net.minecraft.client.renderer.GlStateManager;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.resources.lines.IGuiLine;

public class PanelLine implements IGuiPanel
{
	/**
	 * Bounds aren't used in the drawing of the line, merely for determining draw order
	 */
	private final IGuiRect bounds;
	private final IGuiLine line;
	private final IGuiRect start;
	private final IGuiRect end;
	private final IGuiColor color;
	private final int width;
	
	public PanelLine(IGuiRect start, IGuiRect end, IGuiLine line, int width, IGuiColor color, int drawOrder)
	{
		this.start = start;
		this.end = end;
		this.line = line;
		this.width = width;
		this.color = color;
		this.bounds = new GuiRectangle(0, 0, 0, 0, drawOrder);
		this.bounds.setParent(start);
	}
	
	@Override
	public IGuiRect getTransform()
	{
		return bounds;
	}
	
	@Override
	public void initPanel()
	{
	}
	
	@Override
	public void drawPanel(int mx, int my, float partialTick)
	{
		GlStateManager.pushMatrix();
		line.drawLine(start, end, width, color, partialTick);
		GlStateManager.popMatrix();
	}
	
	@Override
	public boolean onMouseClick(int mx, int my, int button)
	{
		return false;
	}
	
	@Override
	public boolean onMouseRelease(int mx, int my, int button)
	{
		return false;
	}
	
	@Override
	public boolean onMouseScroll(int mx, int my, int scroll)
	{
		return false;
	}
	
	@Override
	public boolean onKeyTyped(char c, int keycode)
	{
		return false;
	}
	
	@Override
	public List<String> getTooltip(int mx, int my)
	{
		return null;
	}
}
