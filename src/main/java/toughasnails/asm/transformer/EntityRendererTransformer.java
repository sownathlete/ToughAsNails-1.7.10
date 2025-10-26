/*
 * Decompiled with CFR 0.148.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  net.minecraft.launchwrapper.IClassTransformer
 *  org.objectweb.asm.ClassReader
 *  org.objectweb.asm.ClassVisitor
 *  org.objectweb.asm.ClassWriter
 *  org.objectweb.asm.tree.ClassNode
 *  org.objectweb.asm.tree.MethodInsnNode
 *  org.objectweb.asm.tree.MethodNode
 */
package toughasnails.asm.transformer;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import toughasnails.asm.ASMHelper;
import toughasnails.asm.ObfHelper;

public class EntityRendererTransformer
implements IClassTransformer {
    private static final String[] VALID_HASHES = new String[]{"7039efd63c08f2d8fa9f000a4a194d5c", "48321722b6b3220fc8d2b5dd4a703476"};
    private static final String[] RENDER_RAIN_SNOW_NAMES = new String[]{"renderRainSnow", "renderRainSnow", "c"};
    private static final String[] ADD_RAIN_PARTICLES_NAMES = new String[]{"addRainParticles", "addRainParticles", "p"};
    private static final String[] GET_FLOAT_TEMPERATURE_NAMES = new String[]{"getFloatTemperature", "getFloatTemperature", "a"};

    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals("net.minecraft.client.renderer.EntityRenderer")) {
            return this.transformEntityRenderer(basicClass, !transformedName.equals(name));
        }
        return basicClass;
    }

    private byte[] transformEntityRenderer(byte[] bytes, boolean obfuscatedClass) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept((ClassVisitor)classNode, 0);
        ASMHelper.verifyClassHash("EntityRenderer", bytes, VALID_HASHES);
        ArrayList successfulTransformations = Lists.newArrayList();
        for (MethodNode methodNode : classNode.methods) {
            MethodInsnNode targetMethodInsnNode;
            if (ASMHelper.methodEquals(methodNode, RENDER_RAIN_SNOW_NAMES, "(F)V")) {
                targetMethodInsnNode = ASMHelper.getUniqueMethodInsnNode(methodNode, 182, ObfHelper.unmapType(obfuscatedClass, "net/minecraft/world/biome/Biome"), GET_FLOAT_TEMPERATURE_NAMES, ObfHelper.createMethodDescriptor(obfuscatedClass, "F", "net/minecraft/util/math/BlockPos"));
                targetMethodInsnNode.setOpcode(184);
                targetMethodInsnNode.owner = "toughasnails/season/SeasonASMHelper";
                targetMethodInsnNode.name = "getFloatTemperature";
                targetMethodInsnNode.desc = ObfHelper.createMethodDescriptor(obfuscatedClass, "F", "net/minecraft/world/biome/Biome", "net/minecraft/util/math/BlockPos");
                successfulTransformations.add(methodNode.name + " " + methodNode.desc);
                continue;
            }
            if (!ASMHelper.methodEquals(methodNode, ADD_RAIN_PARTICLES_NAMES, "()V")) continue;
            targetMethodInsnNode = ASMHelper.getUniqueMethodInsnNode(methodNode, 182, ObfHelper.unmapType(obfuscatedClass, "net/minecraft/world/biome/Biome"), GET_FLOAT_TEMPERATURE_NAMES, ObfHelper.createMethodDescriptor(obfuscatedClass, "F", "net/minecraft/util/math/BlockPos"));
            targetMethodInsnNode.setOpcode(184);
            targetMethodInsnNode.owner = "toughasnails/season/SeasonASMHelper";
            targetMethodInsnNode.name = "getFloatTemperature";
            targetMethodInsnNode.desc = ObfHelper.createMethodDescriptor(obfuscatedClass, "F", "net/minecraft/world/biome/Biome", "net/minecraft/util/math/BlockPos");
            successfulTransformations.add(methodNode.name + " " + methodNode.desc);
        }
        if (successfulTransformations.size() != 2) {
            throw new RuntimeException("An error occurred transforming EntityRenderer. Applied transformations: " + ((Object)successfulTransformations).toString());
        }
        ClassWriter writer = new ClassWriter(1);
        classNode.accept((ClassVisitor)writer);
        bytes = writer.toByteArray();
        return bytes;
    }
}

