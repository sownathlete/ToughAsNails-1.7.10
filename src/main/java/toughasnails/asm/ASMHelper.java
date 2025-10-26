/*
 * Decompiled with CFR 0.148.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  org.apache.commons.codec.digest.DigestUtils
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 *  org.objectweb.asm.MethodVisitor
 *  org.objectweb.asm.tree.AbstractInsnNode
 *  org.objectweb.asm.tree.InsnList
 *  org.objectweb.asm.tree.MethodInsnNode
 *  org.objectweb.asm.tree.MethodNode
 *  org.objectweb.asm.util.Printer
 *  org.objectweb.asm.util.Textifier
 *  org.objectweb.asm.util.TraceMethodVisitor
 */
package toughasnails.asm;

import com.google.common.collect.Lists;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public class ASMHelper {
    public static final Logger LOGGER = LogManager.getLogger((String)"ToughAsNails Transformer");
    private static Printer printer = new Textifier();
    private static TraceMethodVisitor methodVisitor = new TraceMethodVisitor(printer);

    public static boolean methodEquals(MethodNode methodNode, String[] names, String desc) {
        boolean nameMatches = false;
        for (String name : names) {
            if (!methodNode.name.equals(name)) continue;
            nameMatches = true;
            break;
        }
        return nameMatches && methodNode.desc.equals(desc);
    }

    public static void clearNextInstructions(MethodNode methodNode, AbstractInsnNode insnNode) {
        ListIterator iterator = methodNode.instructions.iterator(methodNode.instructions.indexOf(insnNode));
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
    }

    public static MethodInsnNode getUniqueMethodInsnNode(MethodNode methodNode, int opcode, String owner, String[] names, String desc) {
        List<MethodInsnNode> matchedMethodNodes = ASMHelper.matchMethodInsnNodes(methodNode, opcode, owner, names, desc);
        if (matchedMethodNodes.isEmpty()) {
            throw new RuntimeException("No method instruction node found matching " + owner + " " + names[0] + " " + desc);
        }
        if (matchedMethodNodes.size() > 1) {
            LOGGER.warn("Too many matched instructions were found in " + methodNode.name + " for " + owner + " " + names[0] + " " + desc + ". Crashes or bugs may occur!");
        }
        return matchedMethodNodes.get(matchedMethodNodes.size() - 1);
    }

    public static List<MethodInsnNode> matchMethodInsnNodes(MethodNode methodNode, int opcode, String owner, String[] names, String desc) {
        ArrayList matches = Lists.newArrayList();
        ArrayList validMethodNames = Lists.newArrayList((Object[])names);
        for (AbstractInsnNode insnNode : methodNode.instructions.toArray()) {
            if (!(insnNode instanceof MethodInsnNode) || insnNode.getOpcode() != opcode) continue;
            MethodInsnNode methodInsnNode = (MethodInsnNode)insnNode;
            if (!(methodInsnNode.owner.equals(owner) & validMethodNames.contains(methodInsnNode.name)) || !methodInsnNode.desc.equals(desc)) continue;
            matches.add(methodInsnNode);
        }
        return matches;
    }

    public static void verifyClassHash(String className, byte[] bytes, String ... expectedHashes) {
        String currentHash = DigestUtils.md5Hex((byte[])bytes);
        if (!Lists.newArrayList((Object[])expectedHashes).contains(currentHash)) {
            String error = String.format("Unexpected hash %s detected for class %s. Crashes or bugs may occur!", currentHash, className);
            LOGGER.error(error);
        } else {
            LOGGER.info(String.format("Valid hash %s found for class %s.", currentHash, className));
        }
    }

    public static void printMethod(MethodNode methodNode) {
        for (AbstractInsnNode insnNode : methodNode.instructions.toArray()) {
            insnNode.accept((MethodVisitor)methodVisitor);
            StringWriter stringWriter = new StringWriter();
            printer.print(new PrintWriter(stringWriter));
            printer.getText().clear();
            LOGGER.info(stringWriter.toString().replace("\n", ""));
        }
    }
}

