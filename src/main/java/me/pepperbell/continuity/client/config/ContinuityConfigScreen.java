// Modified 2026-10-22 for the unofficial Forge 1.16.5 backport.
package me.pepperbell.continuity.client.config;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public final class ContinuityConfigScreen extends Screen {
    private final Screen parent;
    private final ContinuityConfig config = ContinuityConfig.INSTANCE;
    public ContinuityConfigScreen(Screen parent) {
        super(new TranslatableText("options.continuity.title"));
        this.parent = parent;
    }
    @Override protected void init() {
        Option.BooleanOption[] options = {config.connectedTextures, config.emissiveTextures, config.customBlockLayers, config.useManualCulling};
        for (int i = 0; i < options.length; i++) {
            Option.BooleanOption option = options[i];
            addButton(new ButtonWidget(width / 2 - 130, height / 2 - 75 + i * 25, 260, 20, label(option), button -> {
                option.set(!option.get());
                button.setMessage(label(option));
            }));
        }
        addButton(new ButtonWidget(width / 2 - 100, height / 2 + 40, 200, 20, new TranslatableText("gui.done"), button -> onClose()));
    }
    private Text label(Option.BooleanOption option) {
        Text name = new TranslatableText("options.continuity." + option.getKey());
        return new LiteralText(name.getString() + ": " + new TranslatableText(option.get() ? "options.on" : "options.off").getString());
    }
    @Override public void onClose() {
        config.save();
        if (client.world != null) client.worldRenderer.reload();
        client.openScreen(parent);
    }
    @Override public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        drawCenteredText(matrices, textRenderer, title, width / 2, 25, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
