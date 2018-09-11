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

package com.panda912.muddy.plugin;

import com.android.build.api.transform.Format;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.utils.FileUtils;
import com.google.common.io.Files;
import com.panda912.muddy.lib.CryptoDump;
import com.panda912.muddy.lib.ModifyClassVisitor;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by panda on 2018/9/7 下午5:48.
 */
public class Muddy {

  private static final String CRYPTO_CLASS_REL_PATH = "/com/panda912/muddy/lib/Crypto.class";

  private MuddyExtension mExtension;
  private boolean hasGeneratedCrypto;

  public static void transform(TransformInvocation transformInvocation, MuddyExtension muddyExtension) {
    new Muddy(transformInvocation, muddyExtension);
  }

  private Muddy(TransformInvocation transformInvocation, MuddyExtension muddyExtension) {
    mExtension = muddyExtension;
    transformInvocation.getInputs().forEach(transformInput -> {
      handleJar(transformInvocation, transformInput);
      handleDirectory(transformInvocation, transformInput);
    });
  }

  private void handleJar(TransformInvocation transformInvocation, TransformInput transformInput) {
    transformInput.getJarInputs().forEach(jarInput -> {
      File outputjar = transformInvocation.getOutputProvider().getContentLocation(jarInput.getName(), jarInput
          .getContentTypes(), jarInput.getScopes(), Format.JAR);
      try {
        System.err.println("output jar: " + outputjar);
        Files.createParentDirs(outputjar);
        Files.copy(jarInput.getFile(), outputjar);
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

  private void handleDirectory(TransformInvocation transformInvocation, TransformInput transformInput) {
    transformInput.getDirectoryInputs().forEach(input -> {
      File inputDir = input.getFile();
      File outputDir = transformInvocation.getOutputProvider().getContentLocation(input.getName(), input
          .getContentTypes(), input.getScopes(), Format.DIRECTORY);
      FileUtils.mkdirs(outputDir);

      System.err.println("outputs dir: " + outputDir.getAbsolutePath());
      System.err.println("inputs dir: " + inputDir.getAbsolutePath());
      try {
        generateCryptoClassOnce(inputDir);
        // copy input dir to output dir
        FileUtils.copyDirectory(inputDir, outputDir);

        Files.fileTreeTraverser().preOrderTraversal(inputDir).stream()
            .filter(file -> file.isFile() && file.getName().endsWith(".class")
                && !(inputDir.getAbsolutePath() + CRYPTO_CLASS_REL_PATH).equals(file.getAbsolutePath()))
            .filter(file -> {
              String relativePath = file.getAbsolutePath().replace(inputDir.getAbsolutePath(), "");
              return mExtension.excludes == null || mExtension.excludes.stream()
                  .map(exclude -> exclude.replace(".", "/"))
                  .noneMatch(exclude -> {
                    String[] str = relativePath.split(exclude);
                    return str[0] != null && str[0].endsWith("/") && str[1] != null && (str[1].startsWith("/") ||
                        str[1].startsWith(".class"));
                  });
            })
            .filter(file -> {
              String relativePath = file.getAbsolutePath().replace(inputDir.getAbsolutePath(), "");
              return mExtension.includes == null || mExtension.includes.stream()
                  .map(exclude -> exclude.replace(".", "/"))
                  .anyMatch(exclude -> {
                    String[] str = relativePath.split(exclude);
                    return str[0] != null && str[0].endsWith("/") && str[1] != null && (str[1].startsWith("/") ||
                        str[1].startsWith(".class"));
                  });
            })
            .forEach(inputFile -> {
              System.err.println("inputDir.forEach: " + inputFile.getAbsolutePath());
              String out = inputFile.getAbsolutePath().replace(inputDir.getAbsolutePath(), outputDir.getAbsolutePath());
              transform(inputFile, new File(out));
            });
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

  /**
   * dynamic generate Crypto.class
   *
   * @param inputDir
   * @throws Exception
   */
  private void generateCryptoClassOnce(File inputDir) throws Exception {
    if (hasGeneratedCrypto) {
      return;
    }
    hasGeneratedCrypto = true;

    // generate Crypto.class
    byte[] bytes = CryptoDump.dump(mExtension.key);
    FileUtils.mkdirs(new File(inputDir.getAbsolutePath() + "/com/panda912/muddy/lib"));
    FileOutputStream fos = new FileOutputStream(inputDir.getAbsolutePath() + CRYPTO_CLASS_REL_PATH);
    fos.write(bytes);
    fos.close();
  }

  /**
   * modify input class and then output to dist file
   *
   * @param inputFile
   * @param outputFile
   */
  private void transform(File inputFile, File outputFile) {
    try {
      InputStream inputStream = new FileInputStream(inputFile);
      ClassReader cr = new ClassReader(inputStream);
      ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
      ModifyClassVisitor cv = new ModifyClassVisitor(Opcodes.ASM5, cw, mExtension.key);
      cr.accept(cv, Opcodes.ASM5);
      byte[] bytes = cw.toByteArray();
      FileOutputStream fos = new FileOutputStream(outputFile);
      fos.write(bytes);
      fos.flush();
      fos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
