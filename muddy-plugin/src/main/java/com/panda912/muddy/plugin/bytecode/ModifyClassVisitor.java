/*
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

/*
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


import com.panda912.muddy.plugin.MuddyExtension;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by panda on 2018/8/29 下午4:13.
 */
public class ModifyClassVisitor extends ClassVisitor implements Opcodes {

  private String owner;

  /**
   * save the const field of [private/protected/public static final String]
   */
  private Map<String, String> constFieldMap;

  private boolean clinitExist = false;

  private final MuddyExtension muddyExtension;

  public ModifyClassVisitor(int api, ClassVisitor cv, MuddyExtension extension) {
    super(api, cv);
    this.muddyExtension = extension;
  }

  @Override
  public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
    owner = name;
    super.visit(version, access, name, signature, superName, interfaces);
  }

  @Override
  public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
    // 若想让静态常量的值为表达式，eg. private static final String TAG = Crypto.decode("GAT")
    // 需让 value 的值为 null，然后在类加载的时候，即在 clinit() 中进行赋值
    // 若原始常量值已经为表达式，则此处 value 为 null，已经在 clinit() 中进行了赋值操作，无需处理。
    // 若类中只有静态常量，则不会生成 clinit()，需在类访问结束的时候手动插入 clinit()，并对静态常量赋表达式。

    if (C.STRING.equals(desc)) {
      if (access == ACC_PUBLIC + ACC_STATIC + ACC_FINAL || access == ACC_PRIVATE + ACC_STATIC + ACC_FINAL ||
          access == ACC_PROTECTED + ACC_STATIC + ACC_FINAL) {
        if (value != null) {
          if (constFieldMap == null) {
            constFieldMap = new HashMap<>();
          }
          Crypto.setKey(muddyExtension.key);
          constFieldMap.put(name, Crypto.encode((String) value));
        }
        return super.visitField(access, name, desc, signature, null);
      }
    }
    return super.visitField(access, name, desc, signature, value);
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    clinitExist = C.CLINIT.equals(name) && !clinitExist;

    MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
    return new ModifyConstVisitor(ASM5, mv, owner, name, constFieldMap, muddyExtension.key);
  }

  @Override
  public void visitEnd() {
    if (!clinitExist && constFieldMap != null) {
      MethodVisitor mv = cv.visitMethod(ACC_STATIC, C.CLINIT, "()V", null, null);
      for (Map.Entry<String, String> entry : constFieldMap.entrySet()) {
        mv.visitLdcInsn(entry.getValue());
        mv.visitMethodInsn(INVOKESTATIC, C.CRYPTO_CLASS, "decode", "(Ljava/lang/String;)Ljava/lang/String;", false);
        mv.visitFieldInsn(PUTSTATIC, owner, entry.getKey(), C.STRING);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 0);
        mv.visitEnd();
      }
    }
    super.visitEnd();
  }
}
