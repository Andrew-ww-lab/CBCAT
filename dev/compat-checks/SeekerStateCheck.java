package com.cbcatfix.rocket;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

/** Plain-JVM persistence checks, also run with Radars and Sable absent from runtime. */
public final class SeekerStateCheck {
    public static void main(String[] args) {
        UUID id = UUID.randomUUID();
        CompoundTag legacy = new CompoundTag();
        legacy.putUUID("Target", id);
        legacy.putDouble("X", 12);
        RocketSeeker old = RocketSeeker.load(legacy);
        check(old != null && old.activeRadar() && old.contactId().equals(id.toString()), "legacy target");

        RocketSeeker synthetic = new RocketSeeker("jammer:ghost", null, false,
            new Vec3(1, 2, 3), true, true);
        RocketSeeker restored = RocketSeeker.load(synthetic.save());
        check(restored != null && restored.synthetic() && restored.contactId().equals("jammer:ghost")
            && !restored.save().hasUUID("Target") && restored.save().getDouble("Y") == 2, "UUID-free contact");

        old.suppress("other", 100);
        check(!old.suppressed(0), "unrelated suppression");
        old.suppress(id.toString(), 100);
        old.suppress(id.toString(), 50);
        old = RocketSeeker.load(old.save());
        check(old.suppressed(99) && !old.suppressed(100), "persisted expiry and reacquisition boundary");
        old.suppress(id.toString(), Long.MAX_VALUE);
        check(RocketSeeker.load(old.save()).suppressed(100000), "permanent API lock break");

        RocketSeeker passive = new RocketSeeker(id.toString(), id, false, Vec3.ZERO, false, false);
        passive.suppress(id.toString(), Long.MAX_VALUE);
        check(!RocketSeeker.load(passive.save()).suppressed(0), "non-radar immunity");
        check(RocketSeeker.load(new CompoundTag()) == null, "missing identity");
        legacy.putDouble("X", Double.NaN);
        check(RocketSeeker.load(legacy) == null, "invalid position");
        System.out.println("Seeker persistence checks passed");
    }

    private static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
    }
}
