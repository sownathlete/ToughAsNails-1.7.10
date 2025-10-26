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
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import toughasnails.asm.ASMHelper;
import toughasnails.asm.ObfHelper;

public class BlockCropsTransformer
implements IClassTransformer {
    private static final String[] VALID_HASHES = new String[]{"3d74307bb515539176e7a84967b10a28", "b835f0bbb24031fee6ad804d8c48d2dc"};
    private static final String[] UPDATE_TICK_NAMES = new String[]{"updateTick", "updateTick", "b"};

    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals("net.minecraft.block.BlockCrops")) {
            return this.transformBlockCrops(basicClass, !transformedName.equals(name));
        }
        return basicClass;
    }

    private byte[] transformBlockCrops(byte[] bytes, boolean obfuscatedClass) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept((ClassVisitor)classNode, 0);
        ASMHelper.verifyClassHash("BlockCrops", bytes, VALID_HASHES);
        classNode.interfaces.add("toughasnails/api/season/IDecayableCrop");
        ArrayList successfulTransformations = Lists.newArrayList();
        for (MethodNode methodNode : classNode.methods) {
            if (!ASMHelper.methodEquals(methodNode, UPDATE_TICK_NAMES, ObfHelper.createMethodDescriptor(obfuscatedClass, "V", "net/minecraft/world/World", "net/minecraft/util/math/BlockPos", "net/minecraft/block/state/IBlockState", "java/util/Random"))) continue;
            InsnList insnList = new InsnList();
            insnList.add((AbstractInsnNode)new VarInsnNode(25, 0));
            insnList.add((AbstractInsnNode)new VarInsnNode(25, 1));
            insnList.add((AbstractInsnNode)new VarInsnNode(25, 2));
            insnList.add((AbstractInsnNode)new MethodInsnNode(184, "toughasnails/season/SeasonASMHelper", "onUpdateTick", ObfHelper.createMethodDescriptor(obfuscatedClass, "V", "net/minecraft/block/BlockCrops", "net/minecraft/world/World", "net/minecraft/util/math/BlockPos"), false));
            methodNode.instructions.insertBefore(methodNode.instructions.get(methodNode.instructions.indexOf(methodNode.instructions.getLast()) - 1), insnList);
            successfulTransformations.add(methodNode.name + " " + methodNode.desc);
        }
        if (successfulTransformations.size() != 1) {
            throw new RuntimeException("An error occurred transforming BlockCrops. Applied transformations: " + ((Object)successfulTransformations).toString());
        }
        ClassWriter writer = new ClassWriter(1);
        classNode.accept((ClassVisitor)writer);
        bytes = writer.toByteArray();
        return bytes;
    }
}

