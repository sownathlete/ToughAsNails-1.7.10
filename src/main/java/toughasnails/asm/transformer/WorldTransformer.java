/*
 * Decompiled with CFR 0.148.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  net.minecraft.launchwrapper.IClassTransformer
 *  org.objectweb.asm.ClassReader
 *  org.objectweb.asm.ClassVisitor
 *  org.objectweb.asm.ClassWriter
 *  org.objectweb.asm.tree.AbstractInsnNode
 *  org.objectweb.asm.tree.ClassNode
 *  org.objectweb.asm.tree.InsnList
 *  org.objectweb.asm.tree.InsnNode
 *  org.objectweb.asm.tree.MethodInsnNode
 *  org.objectweb.asm.tree.MethodNode
 *  org.objectweb.asm.tree.VarInsnNode
 */
package toughasnails.asm.transformer;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import toughasnails.asm.ASMHelper;
import toughasnails.asm.ObfHelper;

public class WorldTransformer
implements IClassTransformer {
    private static final String[] VALID_HASHES = new String[]{"547d356661b3b86facf7043fb930bcfb", "a812fff5e65c73ca82f3f2c9ddd2fb03"};
    private static final String[] CAN_SNOW_AT_NAMES = new String[]{"canSnowAt", "canSnowAt", "f"};
    private static final String[] CAN_BLOCK_FREEZE_NAMES = new String[]{"canBlockFreeze", "canBlockFreeze", "e"};
    private static final String[] IS_RAINING_AT_NAMES = new String[]{"isRainingAt", "isRainingAt", "B"};
    private static final String[] GET_BIOME_GEN_FOR_COORDS_NAMES = new String[]{"getBiome", "getBiomeGenForCoords", "b"};

    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals("net.minecraft.world.World")) {
            return this.transformWorld(basicClass, !transformedName.equals(name));
        }
        return basicClass;
    }

    private byte[] transformWorld(byte[] bytes, boolean obfuscatedClass) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept((ClassVisitor)classNode, 0);
        ASMHelper.verifyClassHash("World", bytes, VALID_HASHES);
        ArrayList successfulTransformations = Lists.newArrayList();
        for (MethodNode methodNode : classNode.methods) {
            InsnList insnList;
            if (ASMHelper.methodEquals(methodNode, CAN_SNOW_AT_NAMES, ObfHelper.createMethodDescriptor(obfuscatedClass, "Z", "net/minecraft/util/math/BlockPos", "Z"))) {
                insnList = new InsnList();
                insnList.add((AbstractInsnNode)new VarInsnNode(25, 0));
                insnList.add((AbstractInsnNode)new MethodInsnNode(184, "toughasnails/api/season/SeasonHelper", "getSeasonData", ObfHelper.createMethodDescriptor(obfuscatedClass, "toughasnails/api/season/ISeasonData", "net/minecraft/world/World"), false));
                insnList.add((AbstractInsnNode)new MethodInsnNode(185, "toughasnails/api/season/ISeasonData", "getSubSeason", "()Ltoughasnails/api/season/Season$SubSeason;", true));
                insnList.add((AbstractInsnNode)new MethodInsnNode(182, "toughasnails/api/season/Season$SubSeason", "getSeason", "()Ltoughasnails/api/season/Season;", false));
                insnList.add((AbstractInsnNode)new VarInsnNode(58, 3));
                insnList.add((AbstractInsnNode)new VarInsnNode(25, 0));
                insnList.add((AbstractInsnNode)new VarInsnNode(25, 1));
                insnList.add((AbstractInsnNode)new VarInsnNode(21, 2));
                insnList.add((AbstractInsnNode)new VarInsnNode(25, 3));
                insnList.add((AbstractInsnNode)new MethodInsnNode(184, "toughasnails/season/SeasonASMHelper", "canSnowAtInSeason", ObfHelper.createMethodDescriptor(obfuscatedClass, "Z", "net/minecraft/world/World", "net/minecraft/util/math/BlockPos", "Z", "toughasnails/api/season/Season"), false));
                insnList.add((AbstractInsnNode)new InsnNode(172));
                methodNode.instructions.clear();
                methodNode.instructions.insert(insnList);
                successfulTransformations.add(methodNode.name + " " + methodNode.desc);
                continue;
            }
            if (ASMHelper.methodEquals(methodNode, CAN_BLOCK_FREEZE_NAMES, ObfHelper.createMethodDescriptor(obfuscatedClass, "Z", "net/minecraft/util/math/BlockPos", "Z"))) {
                insnList = new InsnList();
                insnList.add((AbstractInsnNode)new VarInsnNode(25, 0));
                insnList.add((AbstractInsnNode)new MethodInsnNode(184, "toughasnails/api/season/SeasonHelper", "getSeasonData", ObfHelper.createMethodDescriptor(obfuscatedClass, "toughasnails/api/season/ISeasonData", "net/minecraft/world/World"), false));
                insnList.add((AbstractInsnNode)new MethodInsnNode(185, "toughasnails/api/season/ISeasonData", "getSubSeason", "()Ltoughasnails/api/season/Season$SubSeason;", true));
                insnList.add((AbstractInsnNode)new MethodInsnNode(182, "toughasnails/api/season/Season$SubSeason", "getSeason", "()Ltoughasnails/api/season/Season;", false));
                insnList.add((AbstractInsnNode)new VarInsnNode(58, 3));
                insnList.add((AbstractInsnNode)new VarInsnNode(25, 0));
                insnList.add((AbstractInsnNode)new VarInsnNode(25, 1));
                insnList.add((AbstractInsnNode)new VarInsnNode(21, 2));
                insnList.add((AbstractInsnNode)new VarInsnNode(25, 3));
                insnList.add((AbstractInsnNode)new MethodInsnNode(184, "toughasnails/season/SeasonASMHelper", "canBlockFreezeInSeason", ObfHelper.createMethodDescriptor(obfuscatedClass, "Z", "net/minecraft/world/World", "net/minecraft/util/math/BlockPos", "Z", "toughasnails/api/season/Season"), false));
                insnList.add((AbstractInsnNode)new InsnNode(172));
                methodNode.instructions.clear();
                methodNode.instructions.insert(insnList);
                successfulTransformations.add(methodNode.name + " " + methodNode.desc);
                continue;
            }
            if (!ASMHelper.methodEquals(methodNode, IS_RAINING_AT_NAMES, ObfHelper.createMethodDescriptor(obfuscatedClass, "Z", "net/minecraft/util/math/BlockPos"))) continue;
            insnList = new InsnList();
            insnList.add((AbstractInsnNode)new VarInsnNode(25, 0));
            insnList.add((AbstractInsnNode)new MethodInsnNode(184, "toughasnails/api/season/SeasonHelper", "getSeasonData", ObfHelper.createMethodDescriptor(obfuscatedClass, "toughasnails/api/season/ISeasonData", "net/minecraft/world/World"), false));
            insnList.add((AbstractInsnNode)new MethodInsnNode(185, "toughasnails/api/season/ISeasonData", "getSubSeason", "()Ltoughasnails/api/season/Season$SubSeason;", true));
            insnList.add((AbstractInsnNode)new MethodInsnNode(182, "toughasnails/api/season/Season$SubSeason", "getSeason", "()Ltoughasnails/api/season/Season;", false));
            insnList.add((AbstractInsnNode)new VarInsnNode(58, 2));
            insnList.add((AbstractInsnNode)new VarInsnNode(25, 0));
            insnList.add((AbstractInsnNode)new VarInsnNode(25, 1));
            insnList.add((AbstractInsnNode)new VarInsnNode(25, 2));
            insnList.add((AbstractInsnNode)new MethodInsnNode(184, "toughasnails/season/SeasonASMHelper", "isRainingAtInSeason", ObfHelper.createMethodDescriptor(obfuscatedClass, "Z", "net/minecraft/world/World", "net/minecraft/util/math/BlockPos", "toughasnails/api/season/Season"), false));
            insnList.add((AbstractInsnNode)new InsnNode(3));
            insnList.add((AbstractInsnNode)new InsnNode(172));
            MethodInsnNode invokeMethodNode = ASMHelper.getUniqueMethodInsnNode(methodNode, 182, ObfHelper.unmapType(obfuscatedClass, "net/minecraft/world/World"), GET_BIOME_GEN_FOR_COORDS_NAMES, ObfHelper.createMethodDescriptor(obfuscatedClass, "net/minecraft/world/biome/Biome", "net/minecraft/util/math/BlockPos"));
            AbstractInsnNode insertionPoint = methodNode.instructions.get(methodNode.instructions.indexOf((AbstractInsnNode)invokeMethodNode) - 2);
            methodNode.instructions.insertBefore(insertionPoint, insnList);
            ASMHelper.clearNextInstructions(methodNode, insertionPoint);
            successfulTransformations.add(methodNode.name + " " + methodNode.desc);
        }
        if (successfulTransformations.size() != 3) {
            throw new RuntimeException("An error occurred transforming World. Applied transformations: " + ((Object)successfulTransformations).toString());
        }
        ClassWriter writer = new ClassWriter(1);
        classNode.accept((ClassVisitor)writer);
        bytes = writer.toByteArray();
        return bytes;
    }
}

