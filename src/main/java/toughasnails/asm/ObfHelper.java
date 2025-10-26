package toughasnails.asm;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

/**
 * Backported ObfHelper for Forge 1.7.10.
 *
 * Handles descriptor creation and type unmapping between obfuscated
 * and deobfuscated names using FMLDeobfuscatingRemapper.
 */
public class ObfHelper {

    /**
     * Creates a JVM method descriptor string (e.g. (Ljava/lang/String;)V)
     * while optionally unmapping obfuscated class names.
     *
     * @param obfuscated Whether class names are currently obfuscated.
     * @param returnType The return type descriptor (e.g. "V", "java/lang/String").
     * @param types      The parameter type descriptors.
     * @return The full JVM method descriptor.
     */
    public static String createMethodDescriptor(boolean obfuscated, String returnType, String... types) {
        StringBuilder result = new StringBuilder("(");
        for (String type : types) {
            if (type.length() == 1) {
                // Primitive type
                result.append(type);
            } else {
                // Object reference type
                result.append("L")
                      .append(obfuscated ? safeUnmap(type) : type)
                      .append(";");
            }
        }
        result.append(")");
        if (returnType.length() > 1) {
            returnType = "L" + unmapType(obfuscated, returnType) + ";";
        }
        result.append(returnType);
        return result.toString();
    }

    /**
     * Unmaps a type name if obfuscation is enabled.
     *
     * @param obfuscated Whether to perform unmapping.
     * @param type       The internal name of the class.
     * @return The unmapped name or original.
     */
    public static String unmapType(boolean obfuscated, String type) {
        return obfuscated ? safeUnmap(type) : type;
    }

    /**
     * Calls FMLDeobfuscatingRemapper safely. If unavailable (rare 1.7.10 runtime),
     * returns the original string.
     */
    private static String safeUnmap(String name) {
        try {
            return FMLDeobfuscatingRemapper.INSTANCE != null
                ? FMLDeobfuscatingRemapper.INSTANCE.unmap(name)
                : name;
        } catch (Throwable t) {
            return name;
        }
    }
}
