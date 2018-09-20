/*
 * Copyright 2018 panda912
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.panda912.muddy.plugin.bytecode;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * java code:
 * <pre>
 * package com.panda912.muddy.lib;
 * public class Crypto {
 *   public static String encode(String plainText) {
 *     return xor(plainText);
 *   }
 *   public static String decode(String cipherText) {
 *     return xor(cipherText);
 *   }
 *   private static String xor(String str) {
 *     char[] c = str.toCharArray();
 *     for (int i = 0; i < c.length; i++) {
 *       c[i] = (char) (c[i] ^ KEY);
 *     }
 *     return new String(c);
 *   }
 * }
 * </pre>
 * <p>
 * javassist  code:
 * <pre>
 * public static void generateCode(int key) throws IOException, CannotCompileException {
 *   ClassPool classPool = ClassPool.getDefault();
 *   CtClass ctClass = classPool.makeClass("com.panda912.muddy.lib.Crypto");
 *
 *   CtMethod xor = CtNewMethod.make(
 *   "private static String xor(String str) {\n" +
 *   "    char[] c = str.toCharArray();\n" +
 *   "    for (int i = 0; i < c.length; i++) {\n" +
 *   "      c[i] = (char) (c[i] ^ " + key + ");\n" +
 *   "    }\n" +
 *   "    return new String(c);\n" +
 *   "  }", ctClass);
 *   ctClass.addMethod(xor);
 *
 *   CtMethod encode = CtNewMethod.make(
 *   "public static String encode(String plainText) { return xor(plainText); }", ctClass);
 *   ctClass.addMethod(encode);
 *
 *   CtMethod decode = CtNewMethod.make(
 *   "public static String decode(String cipherText) { return xor(cipherText); }", ctClass);
 *   ctClass.addMethod(decode);
 *
 *   ctClass.writeFile("./lib/build/classes/java/main");
 *   ctClass.detach();
 * }
 * </pre>
 * <p>
 * Created by panda on 2018/9/7 下午2:32.
 */
public class GenerateCode implements Opcodes {

  public static byte[] generate(int key) {

    ClassWriter cw = new ClassWriter(0);
    MethodVisitor mv;

    cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, "com/panda912/muddy/lib/Crypto", null, "java/lang/Object", null);

    cw.visitSource("Crypto.java", null);

    {
      mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(20, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
      mv.visitInsn(RETURN);
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLocalVariable("this", "Lcom/panda912/muddy/lib/Crypto;", null, l0, l1, 0);
      mv.visitMaxs(1, 1);
      mv.visitEnd();
    }
    {
      mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "encode", "(Ljava/lang/String;)Ljava/lang/String;", null, null);
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(22, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitMethodInsn(INVOKESTATIC, "com/panda912/muddy/lib/Crypto", "xor", "(Ljava/lang/String;)" +
          "Ljava/lang/String;", false);
      mv.visitInsn(ARETURN);
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLocalVariable("plainText", "Ljava/lang/String;", null, l0, l1, 0);
      mv.visitMaxs(1, 1);
      mv.visitEnd();
    }
    {
      mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "decode", "(Ljava/lang/String;)Ljava/lang/String;", null, null);
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(26, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitMethodInsn(INVOKESTATIC, "com/panda912/muddy/lib/Crypto", "xor", "(Ljava/lang/String;)" +
          "Ljava/lang/String;", false);
      mv.visitInsn(ARETURN);
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLocalVariable("cipherText", "Ljava/lang/String;", null, l0, l1, 0);
      mv.visitMaxs(1, 1);
      mv.visitEnd();
    }
    {
      mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC, "xor", "(Ljava/lang/String;)Ljava/lang/String;", null, null);
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(30, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C", false);
      mv.visitVarInsn(ASTORE, 1);
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(31, l1);
      mv.visitInsn(ICONST_0);
      mv.visitVarInsn(ISTORE, 2);
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitFrame(Opcodes.F_APPEND, 2, new Object[]{"[C", Opcodes.INTEGER}, 0, null);
      mv.visitVarInsn(ILOAD, 2);
      mv.visitVarInsn(ALOAD, 1);
      mv.visitInsn(ARRAYLENGTH);
      Label l3 = new Label();
      mv.visitJumpInsn(IF_ICMPGE, l3);
      Label l4 = new Label();
      mv.visitLabel(l4);
      mv.visitLineNumber(32, l4);
      mv.visitVarInsn(ALOAD, 1);
      mv.visitVarInsn(ILOAD, 2);
      mv.visitVarInsn(ALOAD, 1);
      mv.visitVarInsn(ILOAD, 2);
      mv.visitInsn(CALOAD);
      mv.visitIntInsn(SIPUSH, key);
      mv.visitInsn(IXOR);
      mv.visitInsn(I2C);
      mv.visitInsn(CASTORE);
      Label l5 = new Label();
      mv.visitLabel(l5);
      mv.visitLineNumber(31, l5);
      mv.visitIincInsn(2, 1);
      mv.visitJumpInsn(GOTO, l2);
      mv.visitLabel(l3);
      mv.visitLineNumber(34, l3);
      mv.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
      mv.visitTypeInsn(NEW, "java/lang/String");
      mv.visitInsn(DUP);
      mv.visitVarInsn(ALOAD, 1);
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([C)V", false);
      mv.visitInsn(ARETURN);
      Label l6 = new Label();
      mv.visitLabel(l6);
      mv.visitLocalVariable("i", "I", null, l2, l3, 2);
      mv.visitLocalVariable("str", "Ljava/lang/String;", null, l0, l6, 0);
      mv.visitLocalVariable("c", "[C", null, l1, l6, 1);
      mv.visitMaxs(4, 3);
      mv.visitEnd();
    }
    cw.visitEnd();

    return cw.toByteArray();
  }

}
