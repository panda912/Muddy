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

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Map;

/**
 * 若要动态修改常量字符串的值为表达式（eg. "abcdefg" => Crypto.decode("ansvskva9yaf=")）
 * 在调用 clinit() 方法时修改（即在类初始化的时候修改）
 * <p>
 * Created by panda on 2018/8/30 下午1:40.
 */
public class ModifyConstVisitor extends MethodVisitor implements Opcodes {
  private String owner;
  private String name;
  private Map<String, String> map;
  private final int muddyKey;

  ModifyConstVisitor(int api, MethodVisitor mv, String owner, String name, Map<String, String> map, int key) {
    super(api, mv);
    this.owner = owner;
    this.name = name;
    this.muddyKey = key;
    if (map != null) {
      this.map = map;
    }
  }

  @Override
  public void visitCode() {
    if (C.CLINIT.equals(name) && map != null) {
      for (Map.Entry<String, String> entry : map.entrySet()) {
        mv.visitLdcInsn(entry.getValue());
        mv.visitMethodInsn(INVOKESTATIC, C.CRYPTO_CLASS, "decode", "(Ljava/lang/String;)Ljava/lang/String;", false);
        mv.visitFieldInsn(PUTSTATIC, owner, entry.getKey(), C.STRING);
      }
    }
    super.visitCode();
  }

  /**
   * ldc: push a constant #index from a constant pool (String, int, float, Class,
   * java.lang.invoke.MethodType, or java.lang.invoke.MethodHandle) onto the stack
   *
   * @param cst const value
   */
  @Override
  public void visitLdcInsn(Object cst) {
    if (cst instanceof String) {
      mv.visitLdcInsn(Crypto.encode((String) cst, muddyKey));
      mv.visitMethodInsn(INVOKESTATIC, C.CRYPTO_CLASS, "decode", "(Ljava/lang/String;)Ljava/lang/String;", false);
    } else {
      super.visitLdcInsn(cst);
    }
  }
}
