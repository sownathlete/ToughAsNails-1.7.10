package toughasnails.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import toughasnails.api.TANBlocks;
import toughasnails.block.BlockTANCampfire;
import toughasnails.core.ToughAsNails;
import toughasnails.temperature.BlockTemperatureData;
import toughasnails.temperature.MaterialTemperatureData;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TANConfig {

    public static Gson serializer = new GsonBuilder().setPrettyPrinting().create();
    public static JsonParser parser = new JsonParser();
    public static HashMap<String, ArrayList<BlockTemperatureData>> blockTemperatureData;
    public static MaterialTemperatureData materialTemperatureData;

    public static void init(File configDir) {
        blockTemperatureData = new HashMap<String, ArrayList<BlockTemperatureData>>();
        File blockTemperatureConfigFile = new File(configDir, "block_temperature.json");

        // Create default config if missing
        if (!blockTemperatureConfigFile.exists()) {
            try {
                BlockTemperatureData[] defaults = new BlockTemperatureData[]{
                        new BlockTemperatureData(TANBlocks.campfire, 0, 12.0f),
                        new BlockTemperatureData(Blocks.lit_furnace, 0, 12.0f),
                        new BlockTemperatureData(Blocks.lava, 0, 1.5f),
                        new BlockTemperatureData(Blocks.flowing_lava, 0, 1.5f)
                };
                JsonArray tempArray = new JsonArray();
                for (BlockTemperatureData data : defaults) {
                    tempArray.add(asJsonObject(data));
                }
                writeFile(blockTemperatureConfigFile, tempArray);
            } catch (Exception e) {
                ToughAsNails.logger.error("Error creating default block temperature config file: " + blockTemperatureConfigFile, e);
            }
        }

        // Parse existing block temperature JSON
        try {
            String json = FileUtils.readFileToString(blockTemperatureConfigFile);
            JsonElement root = parser.parse(json);
            if (root == null || !root.isJsonArray()) {
                ToughAsNails.logger.error("Error parsing block temperature config: not a JSON array");
            } else {
                for (JsonElement el : root.getAsJsonArray()) {
                    BlockTemperatureData tempData = asBlockTemperatureData(el);
                    if (tempData == null) continue;
                    String blockName = Block.blockRegistry.getNameForObject(tempData.block);
                    if (!blockTemperatureData.containsKey(blockName))
                        blockTemperatureData.put(blockName, new ArrayList<BlockTemperatureData>());
                    blockTemperatureData.get(blockName).add(tempData);
                }
            }
        } catch (Exception e) {
            ToughAsNails.logger.error("Error reading block temperature config: " + blockTemperatureConfigFile, e);
        }

        // Material temperature config
        materialTemperatureData = new MaterialTemperatureData();
        File materialTemperatureConfigFile = new File(configDir, "material_temperature.json");
        try {
            if (!materialTemperatureConfigFile.exists()) {
                writeFile(materialTemperatureConfigFile, materialTemperatureData);
            }
        } catch (Exception e) {
            ToughAsNails.logger.error("Error creating default material temperature config file: " + materialTemperatureConfigFile, e);
        }

        try {
            String json = FileUtils.readFileToString(materialTemperatureConfigFile);
            Gson gson = new Gson();
            materialTemperatureData = gson.fromJson(json, MaterialTemperatureData.class);
        } catch (Exception e) {
            ToughAsNails.logger.error("Error reading material temperature config: " + materialTemperatureConfigFile, e);
        }
    }

    protected static boolean writeFile(File file, Object obj) {
        try {
            FileUtils.write(file, serializer.toJson(obj));
            return true;
        } catch (Exception e) {
            ToughAsNails.logger.error("Error writing config file " + file.getAbsolutePath(), e);
            return false;
        }
    }

    private static JsonObject asJsonObject(BlockTemperatureData data) {
        JsonObject obj = new JsonObject();
        String blockName = Block.blockRegistry.getNameForObject(data.block);
        obj.addProperty("block", blockName);
        obj.addProperty("meta", data.meta);
        obj.addProperty("temperature", data.blockTemperature);
        return obj;
    }

    private static BlockTemperatureData asBlockTemperatureData(JsonElement element) {
        try {
            JsonObject obj = element.getAsJsonObject();
            if (!obj.has("block") || !obj.has("temperature")) {
                ToughAsNails.logger.error("Invalid block temperature entry: missing required fields");
                return null;
            }

            String blockName = obj.get("block").getAsString();
            Block block = (Block) Block.blockRegistry.getObject(blockName);
            if (block == null) {
                ToughAsNails.logger.error("Unknown block name in temperature config: " + blockName);
                return null;
            }

            int meta = obj.has("meta") ? obj.get("meta").getAsInt() : 0;
            float temp = obj.get("temperature").getAsFloat();
            return new BlockTemperatureData(block, meta, temp);
        } catch (Exception e) {
            ToughAsNails.logger.error("Error parsing block temperature entry", e);
            return null;
        }
    }
}
