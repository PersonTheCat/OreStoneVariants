package com.personthecat.orestonevariants.models;

import com.personthecat.orestonevariants.io.ResourceHelper;
import net.minecraftforge.client.event.TextureStitchEvent;

public class RefreshingResourcesHook {

    /**
     * Checks to see if the resource pack has been unloaded incidentally
     * and puts it back, if needed.
     */
    public static void onTextureStitch(TextureStitchEvent.Pre hook) {
        if (!ResourceHelper.resourcePackEnabled()) {
            ResourceHelper.enableResourcePack();
        }
    }
}
