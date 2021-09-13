package team.creative.creativecore.common.gui;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.event.GuiEvent;
import team.creative.creativecore.common.gui.event.GuiTooltipEvent;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.creativecore.common.util.text.TextBuilder;

public abstract class GuiControl {
    
    private IGuiParent parent;
    public final String name;
    public boolean enabled = true;
    
    public boolean hasPreferredDimensions;
    public int preferredWidth;
    public int preferredHeight;
    public boolean expandableX = false;
    public boolean expandableY = false;
    
    public boolean visible = true;
    
    private List<Component> customTooltip;
    
    public GuiControl(String name) {
        this.name = name;
        this.hasPreferredDimensions = false;
    }
    
    public GuiControl(String name, int width, int height) {
        this.name = name;
        this.hasPreferredDimensions = true;
        this.preferredWidth = width;
        this.preferredHeight = height;
    }
    
    // BASICS
    
    public boolean isClient() {
        return parent.isClient();
    }
    
    public GuiControl setTooltip(List<Component> tooltip) {
        if (!tooltip.isEmpty())
            this.customTooltip = tooltip;
        return this;
    }
    
    public GuiControl setFixed() {
        this.expandableX = false;
        this.expandableY = false;
        return this;
    }
    
    public GuiControl setFixedX() {
        this.expandableX = false;
        return this;
    }
    
    public GuiControl setFixedY() {
        this.expandableY = false;
        return this;
    }
    
    public GuiControl setExpandable() {
        this.expandableX = true;
        this.expandableY = true;
        return this;
    }
    
    public GuiControl setExpandableX() {
        this.expandableX = true;
        return this;
    }
    
    public GuiControl setExpandableY() {
        this.expandableY = true;
        return this;
    }
    
    public GuiControl setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
    
    public void setParent(IGuiParent parent) {
        this.parent = parent;
    }
    
    public IGuiParent getParent() {
        return parent;
    }
    
    public boolean isExpandableX() {
        return expandableX;
    }
    
    public boolean isExpandableY() {
        return expandableY;
    }
    
    public String getNestedName() {
        if (getParent() instanceof GuiControl)
            return ((GuiControl) getParent()).getNestedName() + "." + name;
        return name;
    }
    
    public boolean hasLayer() {
        if (parent instanceof GuiControl)
            return ((GuiControl) parent).hasLayer();
        return false;
    }
    
    public GuiLayer getLayer() {
        if (parent instanceof GuiControl)
            return ((GuiControl) parent).getLayer();
        throw new RuntimeException("Invalid layer control");
    }
    
    public GuiStyle getStyle() {
        if (parent instanceof GuiControl)
            return ((GuiControl) parent).getStyle();
        throw new RuntimeException("Invalid layer control");
    }
    
    public abstract void init();
    
    public abstract void closed();
    
    public abstract void tick();
    
    public boolean is(String... name) {
        for (int i = 0; i < name.length; i++) {
            if (this.name.equalsIgnoreCase(name[i]))
                return true;
        }
        return false;
    }
    
    // SIZE
    
    public abstract void flowX(int width, int preferred);
    
    public abstract void flowY(int height, int preferred);
    
    public void reflow() {
        parent.reflow();
    }
    
    public int getMinWidth() {
        return -1;
    }
    
    protected abstract int preferredWidth();
    
    public int getPreferredWidth() {
        if (hasPreferredDimensions)
            return preferredWidth;
        return preferredWidth();
    }
    
    public int getMaxWidth() {
        return -1;
    }
    
    public int getMinHeight() {
        return -1;
    }
    
    protected abstract int preferredHeight();
    
    public int getPreferredHeight() {
        if (hasPreferredDimensions)
            return preferredHeight;
        return preferredHeight();
    }
    
    public int getMaxHeight() {
        return -1;
    }
    
    // INTERACTION
    
    public boolean testForDoubleClick(Rect rect, double x, double y) {
        return false;
    }
    
    public boolean isInteractable() {
        return enabled && visible;
    }
    
    public void mouseMoved(Rect rect, double x, double y) {}
    
    public boolean mouseClicked(Rect rect, double x, double y, int button) {
        return false;
    }
    
    public boolean mouseDoubleClicked(Rect rect, double x, double y, int button) {
        return mouseClicked(rect, x, y, button);
    }
    
    public void mouseReleased(Rect rect, double x, double y, int button) {}
    
    public void mouseDragged(Rect rect, double x, double y, int button, double dragX, double dragY, double time) {}
    
    public boolean mouseScrolled(Rect rect, double x, double y, double delta) {
        return false;
    }
    
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }
    
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }
    
    public boolean charTyped(char codePoint, int modifiers) {
        return false;
    }
    
    public void looseFocus() {}
    
    public void raiseEvent(GuiEvent event) {
        if (parent != null)
            parent.raiseEvent(event);
    }
    
    // APPERANCE
    
    public abstract ControlFormatting getControlFormatting();
    
    public int getContentOffset() {
        return getStyle().getContentOffset(getControlFormatting());
    }
    
    public GuiTooltipEvent getTooltipEvent(Rect rect, double x, double y) {
        List<Component> toolTip = getTooltip();
        
        if (customTooltip != null)
            if (toolTip == null)
                toolTip = customTooltip;
            else
                toolTip.addAll(customTooltip);
            
        if (toolTip == null) {
            String langTooltip = translateOrDefault(getNestedName() + ".tooltip", null);
            if (langTooltip != null)
                toolTip = new TextBuilder(langTooltip).build();
        }
        
        if (toolTip != null)
            return new GuiTooltipEvent(this, toolTip);
        return null;
    }
    
    public List<Component> getTooltip() {
        return null;
    }
    
    // RENDERING
    
    @OnlyIn(value = Dist.CLIENT)
    public void render(PoseStack matrix, GuiChildControl control, Rect controlRect, Rect realRect, int mouseX, int mouseY) {
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        
        Rect rectCopy = null;
        if (!enabled)
            rectCopy = controlRect.copy();
        
        GuiStyle style = getStyle();
        ControlFormatting formatting = getControlFormatting();
        
        style.get(formatting.border).render(matrix, 0, 0, controlRect.getWidth(), controlRect.getHeight());
        
        int borderWidth = style.getBorder(formatting.border);
        controlRect.shrink(borderWidth);
        style.get(formatting.face, enabled && realRect.inside(mouseX, mouseY)).render(matrix, borderWidth, borderWidth, controlRect.getWidth(), controlRect.getHeight());
        
        renderContent(matrix, control, formatting, borderWidth, controlRect, realRect, mouseX, mouseY);
        
        if (!enabled) {
            realRect.scissor();
            style.disabled.render(matrix, realRect, rectCopy);
        }
    }
    
    @OnlyIn(value = Dist.CLIENT)
    protected void renderContent(PoseStack matrix, GuiChildControl control, ControlFormatting formatting, int borderWidth, Rect controlRect, Rect realRect, int mouseX, int mouseY) {
        controlRect.shrink(formatting.padding);
        matrix.pushPose();
        matrix.translate(borderWidth + formatting.padding, borderWidth + formatting.padding, 0);
        renderContent(matrix, control, controlRect, controlRect.intersection(realRect), mouseX, mouseY);
        matrix.popPose();
    }
    
    @OnlyIn(value = Dist.CLIENT)
    protected void renderContent(PoseStack matrix, GuiChildControl control, Rect controlRect, Rect realRect, int mouseX, int mouseY) {
        renderContent(matrix, control, controlRect, mouseX, mouseY);
    }
    
    @OnlyIn(value = Dist.CLIENT)
    protected abstract void renderContent(PoseStack matrix, GuiChildControl control, Rect rect, int mouseX, int mouseY);
    
    // MINECRAFT
    
    public Player getPlayer() {
        return parent.getPlayer();
    }
    
    public boolean isClientSide() {
        return getPlayer().level.isClientSide;
    }
    
    // UTILS
    
    @OnlyIn(value = Dist.CLIENT)
    public static String translate(String text, Object... parameters) {
        return I18n.get(text, parameters);
    }
    
    @OnlyIn(value = Dist.CLIENT)
    public static String translateOrDefault(String text, String defaultText) {
        if (I18n.exists(text))
            return translate(text);
        return defaultText;
    }
    
    @OnlyIn(value = Dist.CLIENT)
    public static void playSound(SoundInstance sound) {
        Minecraft.getInstance().getSoundManager().play(sound);
    }
    
    @OnlyIn(value = Dist.CLIENT)
    public static void playSound(SoundEvent event) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(event, 1.0F));
    }
    
    @OnlyIn(value = Dist.CLIENT)
    public static void playSound(SoundEvent event, float volume, float pitch) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(event, pitch, volume));
    }
}
