package pers.gwyog.gtneioreplugin.plugin.gregtech5;

import static pers.gwyog.gtneioreplugin.util.OreVeinLayer.*;

import codechicken.nei.PositionedStack;
import cpw.mods.fml.common.Loader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import pers.gwyog.gtneioreplugin.plugin.item.ItemDimensionDisplay;
import pers.gwyog.gtneioreplugin.util.DimensionHelper;
import pers.gwyog.gtneioreplugin.util.GT5OreLayerHelper;
import pers.gwyog.gtneioreplugin.util.GT5OreLayerHelper.OreLayerWrapper;

public class PluginGT5VeinStat extends PluginGT5Base {

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals(getOutputId())) {
            for (OreLayerWrapper oreVein : getAllVeins()) {
                addVeinWithLayers(oreVein, 7);
            }
        } else super.loadCraftingRecipes(outputId, results);
    }

    @Override
    public void loadCraftingRecipes(ItemStack stack) {
        if (stack.getUnlocalizedName().startsWith("gt.blockores")) {
            loadMatchingVeins((short) (stack.getItemDamage() % 1000));
        } else super.loadCraftingRecipes(stack);
    }

    private void loadMatchingVeins(short oreId) {
        for (OreLayerWrapper oreVein : getAllVeins()) {
            if (oreVein.containsOre(oreId)) {
                addVeinWithLayers(oreVein, getMaximumMaterialIndex(oreId, false));
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack stack) {
        String dimension = ItemDimensionDisplay.getDimension(stack);
        if (dimension == null) {
            return;
        }

        for (OreLayerWrapper oreVein : getAllVeins()) {
            if (Arrays.asList(getDimNameArrayFromVeinName(oreVein.veinName)).contains(dimension)) {
                addVeinWithLayers(oreVein, getMaximumMaterialIndex((short) (stack.getItemDamage() % 1000), false));
            }
        }
    }

    private void addVeinWithLayers(OreLayerWrapper oreVein, int maximumMaterialIndex) {
        this.arecipes.add(new CachedVeinStatRecipe(
                oreVein.veinName,
                oreVein.getVeinLayerOre(maximumMaterialIndex, VEIN_PRIMARY),
                oreVein.getVeinLayerOre(maximumMaterialIndex, VEIN_SECONDARY),
                oreVein.getVeinLayerOre(maximumMaterialIndex, VEIN_BETWEEN),
                oreVein.getVeinLayerOre(maximumMaterialIndex, VEIN_SPORADIC)));
    }

    private Collection<OreLayerWrapper> getAllVeins() {
        return GT5OreLayerHelper.mapOreLayerWrapper.values();
    }

    @Override
    public void drawExtras(int recipe) {
        OreLayerWrapper oreLayer = getOreLayer(recipe);

        drawVeinName(oreLayer);
        drawVeinLayerNames(oreLayer);
        drawVeinInfo(oreLayer);

        String sDimNames = GT5OreLayerHelper.bufferedDims.get(oreLayer);
        drawDimNames(sDimNames);

        drawSeeAllRecipesLabel();
    }

    private OreLayerWrapper getOreLayer(int recipe) {
        CachedVeinStatRecipe crecipe = (CachedVeinStatRecipe) this.arecipes.get(recipe);
        return GT5OreLayerHelper.mapOreLayerWrapper.get(crecipe.veinName);
    }

    private static void drawVeinName(OreLayerWrapper oreLayer) {
        if (Loader.isModLoaded("visualprospecting")) {
            drawVeinNameLine(I18n.format(oreLayer.veinName) + " ");
        } else {
            String veinName = getGTOreLocalizedName(oreLayer.Meta[VEIN_PRIMARY]);
            if (veinName.contains("Ore")) drawVeinNameLine(veinName.split("Ore")[0]);
            else if (veinName.contains("Sand")) drawVeinNameLine(veinName.split("Sand")[0]);
            else drawVeinNameLine(veinName + " ");
        }
    }

    private static void drawVeinNameLine(String veinName) {
        drawLine("gtnop.gui.nei.veinName", veinName + I18n.format("gtnop.gui" + ".nei.vein"), 2, 20);
    }

    private static void drawVeinLayerNames(OreLayerWrapper oreLayer) {
        drawVeinLayerNameLine(oreLayer, VEIN_PRIMARY, 50);
        drawVeinLayerNameLine(oreLayer, VEIN_SECONDARY, 60);
        drawVeinLayerNameLine(oreLayer, VEIN_BETWEEN, 70);
        drawVeinLayerNameLine(oreLayer, VEIN_SPORADIC, 80);
    }

    private static void drawVeinLayerNameLine(OreLayerWrapper oreLayer, int veinLayer, int height) {
        drawLine(getOreVeinLayerName(veinLayer), getGTOreLocalizedName(oreLayer.Meta[veinLayer]), 2, height);
    }

    private static void drawVeinInfo(OreLayerWrapper oreLayer) {
        drawLine("gtnop.gui.nei.genHeight", oreLayer.worldGenHeightRange, 2, 90);
        drawLine("gtnop.gui.nei.weightedChance", Integer.toString(oreLayer.randomWeight), 100, 90);
    }

    @Override
    public String getOutputId() {
        return "GTOrePluginVein";
    }

    @Override
    public String getRecipeName() {
        return I18n.format("gtnop.gui.veinStat.name");
    }

    /**
     * The dimension names for a given recipe identifier
     *
     * @param recipe identifier
     * @return A CSV string of dimension name abbreviations
     */
    @Override
    protected String getDimensionNames(int recipe) {
        OreLayerWrapper oreLayer = getOreLayer(recipe);
        return GT5OreLayerHelper.bufferedDims.get(oreLayer);
    }

    private String[] getDimNameArrayFromVeinName(String veinName) {
        OreLayerWrapper oreLayer = GT5OreLayerHelper.mapOreLayerWrapper.get(veinName);
        String[] dims = DimensionHelper.parseDimNames(GT5OreLayerHelper.bufferedDims.get(oreLayer));
        Arrays.sort(dims, Comparator.comparingInt(s -> Arrays.asList(DimensionHelper.DimNameDisplayed)
                .indexOf(s)));
        return dims;
    }

    public class CachedVeinStatRecipe extends CachedRecipe {
        public String veinName;
        public PositionedStack positionedStackPrimary;
        public PositionedStack positionedStackSecondary;
        public PositionedStack positionedStackBetween;
        public PositionedStack positionedStackSporadic;
        private final List<PositionedStack> dimensionDisplayItems = new ArrayList<>();

        public CachedVeinStatRecipe(
                String veinName,
                List<ItemStack> stackListPrimary,
                List<ItemStack> stackListSecondary,
                List<ItemStack> stackListBetween,
                List<ItemStack> stackListSporadic) {
            this.veinName = veinName;
            positionedStackPrimary = new PositionedStack(stackListPrimary, 2, 0);
            positionedStackSecondary = new PositionedStack(stackListSecondary, 22, 0);
            positionedStackBetween = new PositionedStack(stackListBetween, 42, 0);
            positionedStackSporadic = new PositionedStack(stackListSporadic, 62, 0);
            setDimensionDisplayItems();
        }

        private void setDimensionDisplayItems() {
            int x = 2;
            int y = 110;
            int count = 0;
            int itemsPerLine = 9;
            int itemSize = 18;
            for (String dim : getDimNameArrayFromVeinName(this.veinName)) {
                ItemStack item = ItemDimensionDisplay.getItem(dim);
                if (item != null) {
                    int xPos = x + itemSize * (count % itemsPerLine);
                    int yPos = y + itemSize * (count / itemsPerLine);
                    dimensionDisplayItems.add(new PositionedStack(item, xPos, yPos, false));
                    count++;
                }
            }
        }

        @Override
        public List<PositionedStack> getIngredients() {
            return dimensionDisplayItems;
        }

        @Override
        public PositionedStack getResult() {
            return null;
        }

        @Override
        public List<PositionedStack> getOtherStacks() {
            List<PositionedStack> outputs = new ArrayList<>();
            positionedStackPrimary.setPermutationToRender((cycleticks / 20) % positionedStackPrimary.items.length);
            positionedStackSecondary.setPermutationToRender((cycleticks / 20) % positionedStackPrimary.items.length);
            positionedStackBetween.setPermutationToRender((cycleticks / 20) % positionedStackPrimary.items.length);
            positionedStackSporadic.setPermutationToRender((cycleticks / 20) % positionedStackPrimary.items.length);
            outputs.add(positionedStackPrimary);
            outputs.add(positionedStackSecondary);
            outputs.add(positionedStackBetween);
            outputs.add(positionedStackSporadic);
            return outputs;
        }
    }
}
