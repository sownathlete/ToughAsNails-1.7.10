package toughasnails.init;

import java.util.HashMap;
import java.util.Map;

import cpw.mods.fml.common.registry.EntityRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import toughasnails.core.ToughAsNails;
import toughasnails.entities.EntityFreeze;
import toughasnails.entities.projectile.EntityIceball;

public class ModEntities {

    private static int nextEntityId = 1;

    public static final Map<Integer, EntityList.EntityEggInfo> ENTITY_EGGS = new HashMap<Integer, EntityList.EntityEggInfo>();
    public static final Map<Integer, String> ENTITY_NAMES = new HashMap<Integer, String>();

    public static void init() {
        registerEntity(EntityIceball.class, "iceball", 80, 3, true);
        registerEntityWithEgg(EntityFreeze.class, "freeze", 80, 3, true, 0xECEFF1, 0x4354A2, 
                BiomeGenBase.icePlains, BiomeGenBase.iceMountains);
    }

    /** Basic entity registration */
    public static int registerEntity(Class<? extends Entity> entityClass, String name, int trackingRange, int updateFreq, boolean sendsVelocityUpdates) {
        int id = nextEntityId++;
        EntityRegistry.registerModEntity(entityClass, name, id, ToughAsNails.instance, trackingRange, updateFreq, sendsVelocityUpdates);
        ENTITY_NAMES.put(id, name);
        return id;
    }

    /** Registers an entity with a spawn egg and biome spawning */
    public static int registerEntityWithEgg(Class<? extends EntityLiving> entityClass, String name, int trackingRange, int updateFreq, boolean sendsVelocityUpdates,
                                            int eggBackground, int eggForeground, BiomeGenBase... biomes) {
        int id = registerEntity(entityClass, name, trackingRange, updateFreq, sendsVelocityUpdates);
        ENTITY_EGGS.put(id, new EntityList.EntityEggInfo(id, eggBackground, eggForeground));
        EntityRegistry.addSpawn(entityClass, 3, 1, 3, EnumCreatureType.monster, biomes);
        return id;
    }

    /** Creates entity by TAN ID (used for eggs, etc.) */
    public static Entity createEntityByID(int id, World world) {
        try {
            String name = ENTITY_NAMES.get(id);
            if (name != null) {
                Class<? extends Entity> clazz = (Class<? extends Entity>) EntityList.stringToClassMapping.get(name);
                if (clazz != null) {
                    return clazz.getConstructor(World.class).newInstance(world);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("[ToughAsNails] Failed to spawn entity with ID: " + id);
        return null;
    }
}
