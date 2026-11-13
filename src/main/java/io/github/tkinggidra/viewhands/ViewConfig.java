package io.github.tkinggidra.viewhands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/** All saved settings. One live instance is read by the mixins every frame; Gson turns it into JSON. */
public final class ViewConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("viewhands.json");
    private static final ViewConfig LIVE = new ViewConfig();

    public boolean enabled = true;

    // Item tab. Size 0 hides the item completely.
    public float scale = 1f;
    public float offsetX = 0f;
    public float offsetY = 0f;
    public float offsetZ = 0f;
    /** Degrees. */
    public float rotX = 0f;
    public float rotY = 0f;
    public float rotZ = 0f;

    // Swing tab.
    /** false = one fixed multiplier, true = multiplier grows with the held tool's Efficiency level. */
    public boolean speedByEfficiency = false;
    /** Multiplier: 2 = twice as fast, 0.5 = half speed, 0 = the hand never swings. */
    public float swingSpeed = 1f;
    /** Efficiency mode only: extra multiplier per Efficiency level (0.2 -> Efficiency V swings 2x). */
    public float efficiencyBonus = 0.2f;
    /** How far the item travels during a swing. 1 = vanilla, 0 = stays in place. */
    public float swingDistance = 1f;
    /** How much the item rotates during a swing. 1 = vanilla, 0 = no rotation. */
    public float swingRotation = 1f;
    /** The dip-and-raise when you switch items or land a hit. */
    public boolean equipAnimation = true;

    public static ViewConfig get() {
        return LIVE;
    }

    /** True when the swing sliders differ from vanilla, i.e. we have to draw the swing ourselves. */
    public boolean hasCustomSwing() {
        return swingDistance != 1f || swingRotation != 1f;
    }

    public void copyFrom(ViewConfig other) {
        enabled = other.enabled;
        scale = clamp(other.scale, 0f, 3f, 1f);
        offsetX = clamp(other.offsetX, -2f, 2f, 0f);
        offsetY = clamp(other.offsetY, -2f, 2f, 0f);
        offsetZ = clamp(other.offsetZ, -2f, 2f, 0f);
        rotX = clamp(other.rotX, -180f, 180f, 0f);
        rotY = clamp(other.rotY, -180f, 180f, 0f);
        rotZ = clamp(other.rotZ, -180f, 180f, 0f);
        speedByEfficiency = other.speedByEfficiency;
        swingSpeed = clamp(other.swingSpeed, 0f, 5f, 1f);
        efficiencyBonus = clamp(other.efficiencyBonus, 0f, 1f, 0.2f);
        swingDistance = clamp(other.swingDistance, 0f, 3f, 1f);
        swingRotation = clamp(other.swingRotation, 0f, 3f, 1f);
        equipAnimation = other.equipAnimation;
    }

    public ViewConfig copy() {
        ViewConfig c = new ViewConfig();
        c.copyFrom(this);
        return c;
    }

    public static void load() {
        if (!Files.exists(FILE)) return;
        try (Reader reader = Files.newBufferedReader(FILE)) {
            ViewConfig loaded = GSON.fromJson(reader, ViewConfig.class);
            if (loaded != null) LIVE.copyFrom(loaded); // copyFrom also clamps hand-edited values
        } catch (Exception e) {
            System.err.println("[viewhands] Could not read " + FILE + ", using defaults: " + e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE)) {
                GSON.toJson(LIVE, writer);
            }
        } catch (Exception e) {
            System.err.println("[viewhands] Could not save " + FILE + ": " + e);
        }
    }

    private static float clamp(float value, float min, float max, float fallback) {
        if (Float.isNaN(value)) return fallback;
        return Math.max(min, Math.min(max, value));
    }
}
