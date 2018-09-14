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
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.panda912.muddy.plugin.bytecode.CryptoDump;
import com.panda912.muddy.plugin.bytecode.ModifyClassVisitor;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * Created by panda on 2018/9/7 下午5:48.
 */
public class Muddy {

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
        Files.createParentDirs(outputjar);

        JarOutputStream jos = new JarOutputStream(new FileOutputStream(outputjar));

        JarFile jarFile = new JarFile(jarInput.getFile());
        jarFile.stream().forEach(jarEntry -> {
          Log.e(jarEntry.getName());
          if (!jarEntry.isDirectory()) {
            try {
              InputStream inputStream = jarFile.getInputStream(jarEntry);
              JarEntry newJarEntry = new JarEntry(jarEntry.getName());
              jos.putNextEntry(newJarEntry);
              if (jarEntry.getName().endsWith(".class")) {
                jos.write(generateNewClassByteArray(inputStream));
              } else {
                jos.write(ByteStreams.toByteArray(inputStream));
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        });
        jos.finish();
        jos.close();
        IOUtils.closeQuietly(jos);
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

      String cryptoClassPath = inputDir.getAbsolutePath() + "/com/panda912/muddy/lib/Crypto.class";
      try {
        // generate Crypto.class into input dir
        generateCryptoClassOnce(inputDir, cryptoClassPath);
        // copy input dir to output dir
        FileUtils.copyDirectory(inputDir, outputDir);
      } catch (Exception e) {
        e.printStackTrace();
      }

      FileUtils.getAllFiles(inputDir).stream()
          .filter(file -> file.getName().endsWith(".class"))
          .filter(file -> !cryptoClassPath.equals(file.getAbsolutePath()))
          .filter(file -> {
            String classFile = file.getAbsolutePath().replace(inputDir.getAbsolutePath() + File.separator, "");
            if (mExtension.includes != null) {
              return mExtension.includes.stream()
                  .map(exclude -> exclude.replace(".", "/"))
                  .anyMatch(exclude -> {
                    if (classFile.startsWith(exclude)) {
                      String end = classFile.substring(exclude.length(), classFile.length());
                      return end.startsWith("/") || end.startsWith(".class") || end.startsWith("$");
                    }
                    return false;
                  });
            } else if (mExtension.excludes != null) {
              return mExtension.excludes.stream()
                  .map(exclude -> exclude.replace(".", "/"))
                  .noneMatch(exclude -> {
                    if (classFile.startsWith(exclude)) {
                      String end = classFile.substring(exclude.length(), classFile.length());
                      return end.startsWith("/") || end.startsWith(".class") || end.startsWith("$");
                    }
                    return false;
                  });
            } else {
              return true;
            }
          })
          .forEach(inputFile -> {
            String out = inputFile.getAbsolutePath().replace(inputDir.getAbsolutePath(), outputDir.getAbsolutePath());
            try {
              byte[] bytes = generateNewClassByteArray(new FileInputStream(inputFile));
              FileOutputStream fos = new FileOutputStream(out);
              fos.write(bytes);
              fos.flush();
              fos.close();
            } catch (IOException e) {
              e.printStackTrace();
            }
          });
    });
  }

  /**
   * dynamic generate Crypto.class
   *
   * @param inputDir
   * @param output   output file's absolute path
   * @throws Exception
   */
  private void generateCryptoClassOnce(File inputDir, String output) throws Exception {
    if (hasGeneratedCrypto) {
      return;
    }
    hasGeneratedCrypto = true;

    byte[] bytes = CryptoDump.dump(mExtension.key);
    FileUtils.mkdirs(new File(inputDir.getAbsolutePath() + "/com/panda912/muddy/lib"));
    FileOutputStream fos = new FileOutputStream(output);
    fos.write(bytes);
    fos.close();
  }

  /**
   * modify input class and then output to dist file
   *
   * @param inputStream
   */
  private byte[] generateNewClassByteArray(InputStream inputStream) throws IOException {
    ClassReader cr = new ClassReader(inputStream);
    ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
    ModifyClassVisitor cv = new ModifyClassVisitor(Opcodes.ASM5, cw, mExtension.key);
    cr.accept(cv, Opcodes.ASM5);
    return cw.toByteArray();
  }

}
