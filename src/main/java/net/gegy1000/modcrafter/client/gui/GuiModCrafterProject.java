package net.gegy1000.modcrafter.client.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.gegy1000.modcrafter.ModCrafter;
import net.gegy1000.modcrafter.ModCrafterAPI;
import net.gegy1000.modcrafter.json.JsonMod;
import net.gegy1000.modcrafter.mod.Mod;
import net.gegy1000.modcrafter.mod.ModSaveManager;
import net.gegy1000.modcrafter.mod.sprite.Sprite;
import net.gegy1000.modcrafter.script.Script;
import net.gegy1000.modcrafter.script.ScriptDef;
import net.gegy1000.modcrafter.script.ScriptDefHat;
import net.gegy1000.modcrafter.script.ScriptDefPrintConsole;
import net.gegy1000.modcrafter.script.parameter.DataType;
import net.gegy1000.modcrafter.script.parameter.IParameter;
import net.gegy1000.modcrafter.script.parameter.InputParameter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GuiModCrafterProject extends GuiScreen
{
    private GuiModCrafter modCrafterGui;

    private static final ResourceLocation background = new ResourceLocation("modcrafter:textures/gui/background.png");
    private static final ResourceLocation scriptTextures = new ResourceLocation("modcrafter:textures/gui/script/scripts.png");
    private static final ResourceLocation widgets = new ResourceLocation("modcrafter:textures/gui/widgets.png");

    //GuiTextField
    
    private Mod loadedMod;

    private final int scriptHeight = 11;

    private Script holdingScript;

    private int heldOffsetX, heldOffsetY;

    private Script snapping;

    private Sprite selectedSprite;

    public GuiModCrafterProject(GuiModCrafter modCrafterGui, Mod loadedMod)
    {
        this.modCrafterGui = modCrafterGui;
        this.loadedMod = loadedMod;
    }

    public void initGui()
    {
        int i = this.height / 4 + 48;

        selectedSprite = loadedMod.getSprite(0);

        heldOffsetX = 0;
        heldOffsetY = 0;

        this.buttonList.add(new GuiModCrafterButton(0, this.width - 80, this.height - 10 - 20, 72, 20, I18n.format("gui.done", new Object[0])));
    }

    public void actionPerformed(GuiButton button)
    {
        if (button.id == 0)
        {
            try
            {
                ModSaveManager.saveMod(loadedMod);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            // TODO ask whether to save
            this.mc.displayGuiScreen(modCrafterGui);
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_)
    {
        this.drawDefaultBackground();

        String spriteType = selectedSprite.getSpriteDef().getDisplayName();

        String title = selectedSprite == null ? "ModCrafter" : selectedSprite.getName() + " - " + loadedMod.getName();

        this.drawScaledString(mc, title, 88, 2, 0xFFFFFF, 0.75F);
        this.drawScaledString(mc, spriteType, width - getScaledStringWidth(spriteType, 0.75F) - 1, 2, 0xFFFFFF, 0.75F);

        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        drawSprites();
        drawScriptSidebar();

        if (selectedSprite != null)
        {
            for (Entry<Integer, Script> script : selectedSprite.getScripts().entrySet())
            {
                drawScript(script.getValue());
            }
        }

        super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
    }

    /**
     * Draws the background (i is always 0 as of 1.2.2)
     */
    public void drawBackground(int p_146278_1_)
    {
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_FOG);
        Tessellator tessellator = Tessellator.instance;
        this.mc.getTextureManager().bindTexture(background);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        float f = 32.0F;
        tessellator.startDrawingQuads();
        // tessellator.setColorOpaque_I(4210752);
        tessellator.addVertexWithUV(0.0D, (double) this.height, 0.0D, 0.0D, (double) ((float) this.height / f + (float) p_146278_1_));
        tessellator.addVertexWithUV((double) this.width, (double) this.height, 0.0D, (double) ((float) this.width / f), (double) ((float) this.height / f + (float) p_146278_1_));
        tessellator.addVertexWithUV((double) this.width, 0.0D, 0.0D, (double) ((float) this.width / f), (double) p_146278_1_);
        tessellator.addVertexWithUV(0.0D, 0.0D, 0.0D, 0.0D, (double) p_146278_1_);
        tessellator.draw();
    }

    public void drawScript(Script script)
    {
        int x = script.getX();

        float alpha = 1.0F;

        if((script.equals(holdingScript) && snapping != null) || x < 85)
        {
            alpha = 0.8F;
        }

        drawScript(script.getScriptDef(), x, script.getY(), script.getName(), script.getDisplayName(), alpha);
    }

    public void drawScript(ScriptDef def, int xPosition, int yPosition, Object[] name, String displayName, float alpha)
    {
        int width = getScriptDrawWidth(displayName);

        int colour = def.getColor();

        float r = (colour & 0xFF0000) >> 16;
        float g = (colour & 0xFF00) >> 8;
        float b = (colour & 0xFF);

        GL11.glColor4f(r, g, b, alpha);

        mc.renderEngine.bindTexture(scriptTextures);

        if (def instanceof ScriptDefHat)
        {
            drawTexturedModalRect(xPosition, yPosition, 12, 0, 7, 12);

            for (int i = 0; i < 5; i++)
            {
                drawTexturedModalRect(xPosition + 7 + i, yPosition, 19, 0, 1, 12);
            }

            drawTexturedModalRect(xPosition + 12, yPosition, 20, 0, 1, 12);

            for (int i = 5; i < width; i++)
            {
                drawTexturedModalRect(xPosition + 8 + (i), yPosition, 21, 0, 1, 12);
            }

            drawTexturedModalRect(xPosition + 8 + width, yPosition, 22, 0, 1, 12);

            yPosition++;
        }
        else
        {
            drawTexturedModalRect(xPosition, yPosition, 0, 0, 7, 12);

            for (int i = 0; i < width; i++)
            {
                drawTexturedModalRect(xPosition + 7 + (i), yPosition, 7, 0, 1, 12);
            }

            drawTexturedModalRect(xPosition + 7 + width, yPosition, 9, 0, 1, 12);
        }

        int x = xPosition + 2;

        for (Object object : name)
        {
            if(object instanceof InputParameter)
            {
                InputParameter inputParameter = (InputParameter) object;

                String string = inputParameter.getData().toString();

                int textWidth = getScaledStringWidth(string + " ", 0.5F);

                if(inputParameter.getDataType() == DataType.TEXT)
                {
                    drawRect(x - 1, yPosition + 2, textWidth + 1, 6, 1F, 1F, 1F, 0.9F * alpha);
                }

                drawScaledString(mc, string, x, yPosition + 3, 0xD8D8D8, 0.5F);

                x += textWidth;
            }
            else
            {
                drawScaledString(mc, object.toString(), x, yPosition + 3, 0xFFFFFF, 0.5F);

                x += getScaledStringWidth(object.toString() + " ", 0.5F);
            }
        }
    }

    private int getScriptDrawWidth(String displayName)
    {
        return (int) ((float) fontRendererObj.getStringWidth(displayName) * 0.5F) - 5;
    }

    private int getScriptWidth(String displayName)
    {
        return (int) ((float) fontRendererObj.getStringWidth(displayName) * 0.5F);
    }

    private int getScaledStringWidth(String displayName, float scale)
    {
        return (int) ((float) fontRendererObj.getStringWidth(displayName) * (float) scale);
    }

    private void drawScaledString(Minecraft mc, String text, float x, float y, int color, float scale)
    {
        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, scale);
        GL11.glTranslatef(x / scale, y / scale, 0);
        drawString(mc.fontRenderer, text, 0, 0, color);
        GL11.glPopMatrix();
    }

    protected void mouseClickMove(int mouseX, int mouseY, int lastButtonClicked, long timeSinceMouseClick)
    {
        if (holdingScript != null)
        {
            dragScripts(mouseX, mouseY);
        }
    }

    private void dragScripts(int mouseX, int mouseY)
    {
        int x = mouseX + heldOffsetX;
        int y = mouseY + heldOffsetY;

        snapping = null;

        for (Entry<Integer, Script> entry : selectedSprite.getScripts().entrySet())
        {
            Script script = entry.getValue();

            if (script != holdingScript && holdingScript.getScriptDef().canAttachTo(script) && (script.getChild() == null || script.getChild() == holdingScript))
            {
                int yDiff = Math.abs(y - (script.getY() + scriptHeight));

                if (yDiff <= 4)
                {
                    int sWidth = getScriptWidth(script.getDisplayName());

                    if (x > script.getX() - 4 && x + sWidth < script.getX() + sWidth + 4)
                    {
                        x = script.getX();
                        y = script.getY() + scriptHeight - 1;

                        snapping = script;

                        break;
                    }
                }
            }
        }

        moveChild(holdingScript, x, y);

        holdingScript.setPosition(x, y);
    }

    private void moveChild(Script script, int x, int y)
    {
        if (script.getChild() != null)
        {
            y += scriptHeight - 1;

            script.getChild().setPosition(x, y);

            moveChild(script.getChild(), x, y);
        }
    }

    protected void mouseClicked(int mouseX, int mouseY, int button)
    {
        super.mouseClicked(mouseX, mouseY, button);

        if (holdingScript == null)
        {
            if (selectedSprite != null)
            {
                for (Entry<Integer, Script> entry : selectedSprite.getScripts().entrySet())
                {
                    Script script = entry.getValue();

                    int width = getScriptWidth(script.getDisplayName());

                    int x = script.getX();
                    int y = script.getY();

                    if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + scriptHeight)
                    {
                        holdingScript = script;

                        heldOffsetX = x - mouseX;
                        heldOffsetY = y - mouseY;

                        snapping = null;

                        break;
                    }
                }

                if(holdingScript == null)
                {
                    int y = 12;

                    for (Entry<String, ScriptDef> entry : ModCrafterAPI.getScriptDefs().entrySet())
                    {
                        ScriptDef def = entry.getValue();

                        int width = getScriptWidth(def.getDefualtDisplayName());

                        if (mouseX >= 2 && mouseX <= width && mouseY >= y && mouseY <= y + scriptHeight)
                        {
                            holdingScript = new Script(selectedSprite, def, null);

                            heldOffsetX = 2 - mouseX;
                            heldOffsetY = y - mouseY;

                            holdingScript.setPosition(mouseX + heldOffsetX, mouseY + heldOffsetY);

                            selectedSprite.addScript(holdingScript); //TODO add script inside constructor

                            snapping = null;

                            break;
                        }

                        y += scriptHeight + 2;
                    }
                }
                
                if(holdingScript != null)
                {
                    dragScripts(mouseX, mouseY);
                }
            }
        }

        int x = 0;
        int y = 0;

        int spriteWidth = 21;

        for (Sprite sprite : loadedMod.getSprites())
        {
            int drawY = height - (spriteWidth * 3) + y;

            if (mouseX < x + spriteWidth - 1 && mouseX > x)
            {
                if (mouseY > drawY && mouseY < drawY + spriteWidth)
                {
                    selectedSprite = sprite;

                    break;
                }
            }

            x += spriteWidth;

            if (x > spriteWidth * 3)
            {
                x = 0;
                y += spriteWidth;
            }
        }
    }

    private void drawRect(int x, int y, int sizeX, int sizeY, float r, float g, float b, float a)
    {
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glColor4f(r, g, b, a);

        drawTexturedModalRect(x, y, 0, 0, sizeX, sizeY);

        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    protected void mouseMovedOrUp(int mouseX, int mouseY, int event)
    {
        super.mouseMovedOrUp(mouseX, mouseY, event);

        if (holdingScript != null)
        {
            holdingScript.setParent(snapping);

            if(holdingScript.getX() < 85)
            {
                selectedSprite.removeScript(holdingScript);
            }
        }

        holdingScript = null;
        heldOffsetX = 0;
        heldOffsetY = 0;
    }

    private void drawScriptSidebar()
    {
        drawRect(85, 0, 1, height, 1.0F, 1.0F, 1.0F, 0.2F);
        drawRect(0, height - 66, 85, 1, 1.0F, 1.0F, 1.0F, 0.2F);

        drawRect(0, 9, 85, 1, 1.0F, 1.0F, 1.0F, 0.2F);
        drawRect(86, 9, width - 86, 1, 1.0F, 1.0F, 1.0F, 0.2F);

        if(selectedSprite != null)
        {
            drawScaledString(mc, "Script Selection", 2, 2, 0xFFFFFF, 0.75F);

            int y = 12;

            for (Entry<String, ScriptDef> entry : ModCrafterAPI.getScriptDefs().entrySet())
            {
                ScriptDef def = entry.getValue();

                drawScript(def, 2, y, def.getName(), def.getDefualtDisplayName(), 1.0F);

                y += scriptHeight + 2;
            }
        }
    }

    private void drawSprites()
    {
        int x = 0;
        int y = 0;

        int spriteWidth = 21;

        for (Sprite sprite : loadedMod.getSprites())
        {
            mc.getTextureManager().bindTexture(widgets);

            if (sprite == selectedSprite)
            {
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            }
            else
            {
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
            }

            int drawY = height - (spriteWidth * 3) + y;

            drawTexturedModalRect(x, drawY, 0, 0, spriteWidth - 1, spriteWidth - 1);

            String name = sprite.getName();

            if (name.length() > 15)
            {
                name = name.substring(0, 12) + "...";
            }

            drawScaledString(mc, name, x + 1, drawY + 17, 0xFFFFFF, 0.25F);

            x += spriteWidth;

            if (x > spriteWidth * 3)
            {
                x = 0;
                y += spriteWidth;
            }
        }
    }
}
