package com.personthecat.orestonevariants.proxy;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;

public class CommonProxy {

    public void registerVariantRenderer(Item item, int meta, String filename) {}

    public void registerVariantRenderer(Item item, int meta, ModelResourceLocation mrl) {}

    public void enableResourcePack() {}

    public void generateOverlays() {}
}
