package betterquesting.client.gui2;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumQuestState;
import betterquesting.api.enums.EnumQuestVisibility;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineEntry;
import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.content.PanelLine;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.lines.IGuiLine;
import betterquesting.api2.client.gui.resources.textures.GuiTextureColored;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.resources.textures.ItemTexture;
import betterquesting.api2.client.gui.resources.textures.SimpleTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetLine;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.questing.QuestDatabase;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * My class for lazy quest line setup on a scrolling canvas
 */
public class CanvasQuestLine extends CanvasScrolling
{
    private final int buttonId;
    
    public CanvasQuestLine(IGuiRect rect, int buttonId)
    {
        super(rect);
        this.setupAdvanceScroll(true, true, 24);
        
        this.buttonId = buttonId;
    }
    
    /**
     * Loads in quests and connecting lines
     * @param line The quest line to load
     */
    public void setQuestLine(IQuestLine line)
    {
        // Rest contents
        this.getAllPanels().clear();
        
        if(line == null)
        {
            return;
        }
        
        EntityPlayer player = Minecraft.getMinecraft().player;
        UUID pid = QuestingAPI.getQuestingUUID(player);
        
        String bgString = line.getProperties().getProperty(NativeProps.BG_IMAGE);
        
        if(bgString != null && bgString.length() > 0)
        {
            ResourceLocation bgRes = new ResourceLocation(bgString);
            int bgSize = line.getProperties().getProperty(NativeProps.BG_SIZE);
            this.addPanel(new PanelGeneric(new GuiRectangle(0, 0, bgSize, bgSize), new SimpleTexture(bgRes, new GuiRectangle(0, 0, 256, 256))));
        }
        
        // Used later to center focus the quest line within the window
        boolean flag = false;
        int minX = 0;
        int minY = 0;
        int maxX = 0;
        int maxY = 0;
        
        HashMap<Integer, PanelButtonStorage<IQuest>> questBtns = new HashMap<>();
        
        for(IQuestLineEntry qle : line.getAllValues())
        {
            int id = line.getKey(qle);
            IQuest quest = QuestDatabase.INSTANCE.getValue(id);
            
            if(quest == null || !isQuestShown(quest, pid))
            {
                continue;
            }
            
            EnumQuestState qState = quest.getState(pid);
            IGuiTexture txFrame = null;
            IGuiColor txIconCol = null;
            boolean main = quest.getProperties().getProperty(NativeProps.MAIN);
            
            switch(qState)
            {
                case LOCKED:
                    txFrame = main ? PresetTexture.QUEST_MAIN_0.getTexture() : PresetTexture.QUEST_NORM_0.getTexture();
                    txIconCol = PresetColor.QUEST_ICON_LOCKED.getColor();
                    break;
                case UNLOCKED:
                    txFrame = main ? PresetTexture.QUEST_MAIN_1.getTexture() : PresetTexture.QUEST_NORM_1.getTexture();
                    txIconCol = PresetColor.QUEST_ICON_UNLOCKED.getColor();
                    break;
                case UNCLAIMED:
                    txFrame = main ? PresetTexture.QUEST_MAIN_2.getTexture() : PresetTexture.QUEST_NORM_2.getTexture();
                    txIconCol = PresetColor.QUEST_ICON_PENDING.getColor();
                    break;
                case COMPLETED:
                    txFrame = main ? PresetTexture.QUEST_MAIN_3.getTexture() : PresetTexture.QUEST_NORM_3.getTexture();
                    txIconCol = PresetColor.QUEST_ICON_COMPLETE.getColor();
                    break;
            }
            
            IGuiRect rect = new GuiRectangle(qle.getPosX(), qle.getPosY(), qle.getSize(), qle.getSize());
            PanelButtonStorage<IQuest> paBtn = new PanelButtonStorage<>(rect, buttonId, "", quest);
            paBtn.setTextures(new GuiTextureColored(txFrame, txIconCol), new GuiTextureColored(txFrame, txIconCol), new GuiTextureColored(txFrame, txIconCol));
            paBtn.setIcon(new ItemTexture(quest.getItemIcon()), 4);
            paBtn.setTooltip(quest.getTooltip(player));
            
            this.addPanel(paBtn);
            questBtns.put(id, paBtn);
            
            if(!flag)
            {
                minX = rect.getX();
                minY = rect.getY();
                maxX = minX + rect.getWidth();
                maxY = minY + rect.getHeight();
                flag = true;
            } else
            {
                minX = Math.min(minX, rect.getX());
                minY = Math.min(minY, rect.getY());
                maxX = Math.max(maxX, rect.getX() + rect.getWidth());
                maxY = Math.max(maxY, rect.getY() + rect.getHeight());
            }
        }
        
        for(Entry<Integer, PanelButtonStorage<IQuest>> entry : questBtns.entrySet())
        {
            IQuest quest = entry.getValue().getStoredValue();
            
            List<IQuest> reqList = quest.getPrerequisites();
            
            if(reqList.size() <= 0)
            {
                continue;
            }
            
            boolean main = quest.getProperties().getProperty(NativeProps.MAIN);
            EnumQuestState qState = quest.getState(pid);
            IGuiLine lineRender = null;
            IGuiColor txLineCol = null;
            
            switch(qState)
            {
                case LOCKED:
                    lineRender = PresetLine.QUEST_LOCKED.getLine();
                    txLineCol = PresetColor.QUEST_LINE_LOCKED.getColor();
                    break;
                case UNLOCKED:
                    lineRender = PresetLine.QUEST_UNLOCKED.getLine();
                    txLineCol = PresetColor.QUEST_LINE_UNLOCKED.getColor();
                    break;
                case UNCLAIMED:
                    lineRender = PresetLine.QUEST_PENDING.getLine();
                    txLineCol = PresetColor.QUEST_LINE_PENDING.getColor();
                    break;
                case COMPLETED:
                    lineRender = PresetLine.QUEST_COMPLETE.getLine();
                    txLineCol = PresetColor.QUEST_LINE_COMPLETE.getColor();
                    break;
            }
            
            for(IQuest req : reqList)
            {
                int id = QuestDatabase.INSTANCE.getKey(req);
                
                PanelButtonStorage<IQuest> parBtn = questBtns.get(id);
                
                if(parBtn != null)
                {
                    PanelLine prLine = new PanelLine(parBtn.getTransform(), entry.getValue().getTransform(), lineRender, main? 8 : 4, txLineCol, 1);
                    this.addPanel(prLine);
                }
            }
        }
        
        float frameW = getTransform().getWidth();
        float frameH = getTransform().getHeight();
        
        if(frameW <= 0 || frameH <= 0)
        {
            return;
        }
        
        minX -= margin;
        minY -= margin;
        maxX += margin;
        maxY += margin;
        
        float scale = Math.min(frameW/(maxX - minX), frameH/(maxY - minY));
        scale = MathHelper.clamp(scale, 0.25F, 2F);
        
        this.setZoom(scale);
        int scrollX = Math.round((maxX - minX)/2F - (frameW/scale)/2F);
        int scrollY = Math.round((maxY - minY)/2F - (frameH/scale)/2F);
        
        this.setScrollX(scrollX);
        this.setScrollY(scrollY);
    }
    
    public static boolean isQuestShown(IQuest quest, UUID uuid)
    {
        if(quest == null || uuid == null)
        {
            return false;
        }
        
        Minecraft mc = Minecraft.getMinecraft();
        
        EnumQuestVisibility vis = quest.getProperties().getProperty(NativeProps.VISIBILITY);
        
        if(QuestingAPI.getAPI(ApiReference.SETTINGS).canUserEdit(mc.player) || vis == EnumQuestVisibility.ALWAYS)
        {
            return true;
        } else if(vis == EnumQuestVisibility.HIDDEN)
        {
            return false;
        } else if(vis == EnumQuestVisibility.UNLOCKED)
        {
            return quest.isComplete(uuid) || quest.isUnlocked(uuid);
        } else if(vis == EnumQuestVisibility.NORMAL)
        {
            if(quest.isComplete(uuid) || quest.isUnlocked(uuid))
            {
                return true;
            }
            
            for(IQuest q : quest.getPrerequisites())
            {
                if(!q.isUnlocked(uuid))
                {
                    return false;
                }
            }
            
            return true;
        } else if(vis == EnumQuestVisibility.COMPLETED)
        {
            return quest.isComplete(uuid);
        } else if(vis == EnumQuestVisibility.CHAIN)
        {
            if(quest.getPrerequisites().size() <= 0)
            {
                return true;
            }
            
            for(IQuest q : quest.getPrerequisites())
            {
                if(isQuestShown(q, uuid))
                {
                    return true;
                }
            }
            
            return false;
        }
        
        return true;
    }
}
