package com.cbcatfix.mixin;

import java.util.List;
import java.util.Set;
import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/** Gate optional targets before Mixin resolves their classes, as CBC does for its integrations. */
public final class CbcatFixMixinPlugin implements IMixinConfigPlugin {
    @Override public boolean shouldApplyMixin(String target, String mixin) {
        String dependency = switch (mixin.substring(mixin.lastIndexOf('.') + 1)) {
            case "SableLauncherTransferMixin" -> "sable";
            case "GuidedFuzeItemMixin" -> "create_radar";
            case "CBCATJEIMixin" -> "jei";
            default -> null;
        };
        return dependency == null || LoadingModList.get().getModFileById(dependency) != null;
    }
    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(Set<String> mine, Set<String> others) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String target, ClassNode node, String mixin, IMixinInfo info) {}
    @Override public void postApply(String target, ClassNode node, String mixin, IMixinInfo info) {}
}
