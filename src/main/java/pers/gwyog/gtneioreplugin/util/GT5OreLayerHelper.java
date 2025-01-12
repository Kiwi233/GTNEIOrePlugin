package pers.gwyog.gtneioreplugin.util;

import static pers.gwyog.gtneioreplugin.util.OreVeinLayer.*;

import gregtech.api.GregTech_API;
import gregtech.common.GT_Worldgen_GT_Ore_Layer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import net.minecraft.item.ItemStack;

public class GT5OreLayerHelper {

    private static final int DIMENSION_COUNT = 33;
    public static Integer[] weightPerWorld = new Integer[DIMENSION_COUNT];
    public static Integer[] DimIDs = new Integer[DIMENSION_COUNT];
    public static HashMap<String, OreLayerWrapper> mapOreLayerWrapper = new HashMap<>();
    public static HashMap<OreLayerWrapper, String> bufferedDims = new HashMap<>();

    public GT5OreLayerHelper() {
        Arrays.fill(weightPerWorld, 0);
        Arrays.fill(DimIDs, 0);
        for (GT_Worldgen_GT_Ore_Layer tWorldGen : GT_Worldgen_GT_Ore_Layer.sList)
            mapOreLayerWrapper.put(tWorldGen.mWorldGenName, new OreLayerWrapper(tWorldGen));
        for (OreLayerWrapper layer : mapOreLayerWrapper.values()) {
            bufferedDims.put(layer, getDims(layer));
        }
    }

    public static String getDims(OreLayerWrapper oreLayer) {
        return GT5CFGHelper.GT5CFG(
                oreLayer.veinName.replace("ore.mix.custom" + ".", "").replace("ore.mix.", ""));
    }

    public static class OreLayerWrapper {
        public String veinName, worldGenHeightRange;
        public short[] Meta = new short[4];
        public short randomWeight, size, density;
        public List<Integer> Weight = new ArrayList<>();

        public OreLayerWrapper(GT_Worldgen_GT_Ore_Layer worldGen) {
            this.veinName = worldGen.mWorldGenName;
            this.Meta[0] = worldGen.mPrimaryMeta;
            this.Meta[1] = worldGen.mSecondaryMeta;
            this.Meta[2] = worldGen.mBetweenMeta;
            this.Meta[3] = worldGen.mSporadicMeta;
            this.size = worldGen.mSize;
            this.density = worldGen.mDensity;
            this.worldGenHeightRange = worldGen.mMinY + "-" + worldGen.mMaxY;
            this.randomWeight = worldGen.mWeight;
        }

        public List<ItemStack> getVeinLayerOre(int maximumMaterialIndex, int veinLayer) {
            List<ItemStack> stackList = new ArrayList<>();
            for (int i = 0; i < maximumMaterialIndex; i++) {
                stackList.add(getLayerOre(veinLayer, i));
            }
            return stackList;
        }

        public ItemStack getLayerOre(int veinLayer, int materialIndex) {
            return new ItemStack(GregTech_API.sBlockOres1, 1, Meta[veinLayer] + materialIndex * 1000);
        }

        public boolean containsOre(short materialIndex) {
            return Meta[VEIN_PRIMARY] == materialIndex
                    || Meta[VEIN_SECONDARY] == materialIndex
                    || Meta[VEIN_BETWEEN] == materialIndex
                    || Meta[VEIN_SPORADIC] == materialIndex;
        }
    }
}
