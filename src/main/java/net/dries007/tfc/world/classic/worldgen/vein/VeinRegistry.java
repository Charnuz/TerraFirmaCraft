/*
 * Work under Copyright. Licensed under the EUPL.
 * See the project README.md and LICENSE.txt for more information.
 */

package net.dries007.tfc.world.classic.worldgen.vein;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;

import net.dries007.tfc.ConfigTFC;
import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.util.collections.WeightedCollection;
import net.dries007.tfc.world.classic.worldgen.WorldGenOreVeins;

import static net.dries007.tfc.Constants.GSON;
import static net.dries007.tfc.TerraFirmaCraft.MOD_ID;

public enum VeinRegistry
{
    INSTANCE;

    private static final String DEFAULT_ORE_SPAWN_LOCATION = "assets/tfc/config/ore_spawn_data.json";

    private final WeightedCollection<VeinType> weightedVeinTypes = new WeightedCollection<>();
    private final Map<String, VeinType> veinTypeRegistry = new HashMap<>();
    private File tfcConfigDir;

    @Nonnull
    public WeightedCollection<VeinType> getVeins()
    {
        return weightedVeinTypes;
    }

    @Nonnull
    public Set<String> keySet()
    {
        return veinTypeRegistry.keySet();
    }

    @Nullable
    public VeinType getVein(String name)
    {
        return veinTypeRegistry.get(name);
    }

    /**
     * Creates the TFC config directory, and populates it if empty
     *
     * @param dir the mod config directory root
     */
    public void preInit(File dir)
    {
        // Init base config dir
        TerraFirmaCraft.getLog().info("Loading or creating TFC config directory");
        tfcConfigDir = new File(dir, MOD_ID); // todo in 1.15+ move this to a subfolder like damage resistance (i left untouched so old config still work)
        if (!tfcConfigDir.exists() && !tfcConfigDir.mkdir())
        {
            throw new Error("Problem creating TFC extra config directory.");
        }

        // Create or overwrite our default ore gen file
        if (ConfigTFC.WORLD.enableDefaultOreGenFileOverwrite)
        {
            // Create default vein file
            try
            {
                File defaultFile = new File(tfcConfigDir, "ore_spawn_data.json");
                if (defaultFile.exists())
                {
                    // Back up the file, in case of illiteracy
                    FileUtils.copyFile(defaultFile, new File(defaultFile.getPath() + ".old"));
                    // And replace the contents
                    FileUtils.copyInputStreamToFile(Objects.requireNonNull(VeinRegistry.class.getClassLoader().getResourceAsStream(DEFAULT_ORE_SPAWN_LOCATION)), defaultFile);
                }
                else if (defaultFile.createNewFile())
                {
                    FileUtils.copyInputStreamToFile(Objects.requireNonNull(VeinRegistry.class.getClassLoader().getResourceAsStream(DEFAULT_ORE_SPAWN_LOCATION)), defaultFile);
                }
            }
            catch (IOException e)
            {
                throw new Error("Problem creating default ore vein config file.", e);
            }
        }
    }

    /**
     * Recursively scan through config dir, finding all ore gen files
     */
    public void postInit()
    {
        weightedVeinTypes.clear();
        veinTypeRegistry.clear();

        // Changed to only read files in the same folder, not subdirectories
        File[] allFiles;
        try
        {
            allFiles = tfcConfigDir.listFiles();
            Objects.requireNonNull(allFiles);
        }
        catch (NullPointerException e)
        {
            TerraFirmaCraft.getLog().error("Unable to read files in the damage resistance directory! TFC will not set entities resistance data!");
            TerraFirmaCraft.getLog().error("Error: ", e);
            return;
        }

        for (File veinFile : allFiles)
        {
            if (veinFile.isDirectory())
            {
                continue; // Skip
            }
            try
            {
                // Read each file, then json parse each file individually into a map, so each vein can be parsed by GSON independently
                String fileContents = new String(Files.readAllBytes(veinFile.toPath()), Charset.defaultCharset());
                Set<Map.Entry<String, JsonElement>> allVeinsJson = new JsonParser().parse(fileContents).getAsJsonObject().entrySet();
                for (Map.Entry<String, JsonElement> entry : allVeinsJson)
                {
                    try
                    {
                        String properVeinName = entry.getKey();
                        String veinPath = tfcConfigDir.toPath().relativize(veinFile.toPath().getParent()).toString();
                        if (!veinPath.isEmpty())
                        {
                            properVeinName = veinPath + "/" + properVeinName;
                        }
                        VeinType vein = GSON.fromJson(entry.getValue(), VeinType.class);
                        vein.setRegistryName(properVeinName);

                        veinTypeRegistry.put(properVeinName, vein);
                        weightedVeinTypes.add(vein.getWeight(), vein);

                        TerraFirmaCraft.getLog().info("Registered new vein " + vein.toString());
                    }
                    catch (JsonParseException e)
                    {
                        TerraFirmaCraft.getLog().error("An ore vein is specified incorrectly! Skipping.");
                        TerraFirmaCraft.getLog().error("Error: ", e);
                    }
                }
            }
            catch (IOException e)
            {
                // Don't crash the game if one of the files error-ed, just show it in log
                TerraFirmaCraft.getLog().error("There was an error reading an ore generation file at: " + veinFile.toPath());
                TerraFirmaCraft.getLog().error("Error: ", e);
            }
        }

        // Reset max chunk radius and number of rolls for veins
        int maxRadius = 0;
        for (VeinType type : veinTypeRegistry.values())
        {
            if (type.getWidth() > maxRadius)
            {
                maxRadius = type.getWidth();
            }
        }
        WorldGenOreVeins.CHUNK_RADIUS = 1 + (maxRadius >> 4);
        WorldGenOreVeins.NUM_ROLLS = 1 + (int) (0.5 + weightedVeinTypes.getTotalWeight());
        TerraFirmaCraft.getLog().info("Vein Registry Initialized, with {} veins, {} max radius, {} total weight", veinTypeRegistry.size(), maxRadius, weightedVeinTypes.getTotalWeight());
    }
}
