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
import com.panda912.muddy.plugin.bytecode.GenerateCode;
import com.panda912.muddy.plugin.bytecode.ModifyClassVisitor;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * Created by panda on 2018/9/7 下午5:48.
 */
public class Muddy {

  private MuddyExtension extension;
  private boolean hasGeneratedCrypto;

  public static void transform(TransformInvocation transformInvocation, MuddyExtension extension) {
    new Muddy(transformInvocation, extension);
  }

  private Muddy(TransformInvocation transformInvocation, MuddyExtension extension) {
    this.extension = extension;
    transformInvocation.getInputs().forEach(transformInput -> {
      processJar(transformInvocation, transformInput);
      processDirectory(transformInvocation, transformInput);
    });
  }

  private void processJar(TransformInvocation transformInvocation, TransformInput transformInput) {
    transformInput.getJarInputs().forEach(jarInput -> {
      File outputjar = transformInvocation.getOutputProvider().getContentLocation(jarInput.getName(), jarInput
          .getContentTypes(), jarInput.getScopes(), Format.JAR);
      try {
        // traverse delete old jar file
        FileUtils.deleteIfExists(outputjar);
        Files.createParentDirs(outputjar);
        // if not config includeLibs, just copy all jars to dest dir
        if (extension.includeLibs == null) {
          Files.copy(jarInput.getFile(), outputjar);
          return;
        }

        JarOutputStream jos = new JarOutputStream(new FileOutputStream(outputjar));
        // traverse jar
        JarFile jarFile = new JarFile(jarInput.getFile());
        jarFile.stream()
            .filter(jarEntry -> !jarEntry.isDirectory())
            .forEach(jarEntry -> {
              try {
                String entryName = jarEntry.getName();
                InputStream inputStream = jarFile.getInputStream(jarEntry);
                jos.putNextEntry(new JarEntry(entryName));
                if (entryName.endsWith(".class") && isInclude(extension.includeLibs, entryName)) {
                  jos.write(generateNewClassByteArray(inputStream));
                } else {
                  jos.write(ByteStreams.toByteArray(inputStream));
                }
              } catch (Exception e) {
                e.printStackTrace();
              }
            });
        jos.finish();
        jos.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

  private void processDirectory(TransformInvocation transformInvocation, TransformInput transformInput) {
    transformInput.getDirectoryInputs().forEach(input -> {
      //eg. if the class file's absolute path is {@code /a/b/package/A.class}, this returns {@code /a/b}.
      File inputDir = input.getFile();
      File outputDir = transformInvocation.getOutputProvider().getContentLocation(input.getName(), input
          .getContentTypes(), input.getScopes(), Format.DIRECTORY);
      if (transformInvocation.isIncremental()) {
        input.getChangedFiles().forEach((inputFile, status) -> {
          if (inputFile.isFile() && inputFile.getName().endsWith(".class")) {
            try {
              File destFile = Util.getOutputFile(inputDir, inputFile, outputDir);
              switch (status) {
                case REMOVED:
                  FileUtils.deleteIfExists(destFile);
                  break;
                case CHANGED:
                case ADDED:
                  Log.e(status + ": " + destFile);
                  // added status delete dest file due to Crypto.class
                  FileUtils.deleteIfExists(destFile);
                  Files.createParentDirs(destFile);
                  if (destFile.createNewFile()) {
                    byte[] bytes = generateNewClassByteArray(new FileInputStream(inputFile));
                    FileOutputStream fos = new FileOutputStream(destFile);
                    fos.write(bytes);
                    fos.flush();
                    fos.close();
                  } else {
                    throw new FileNotFoundException(destFile.getAbsolutePath() + " not found, please clean and retry!");
                  }
                  break;
              }
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        });
      } else {
        File cryptoClassFile = new File(Util.toSystemIndependentPath(inputDir),
            Util.toSystemIndependentPath("/com/panda912/muddy/lib/Crypto.class"));
        try {
          // traverse delete old output dir
          FileUtils.deletePath(outputDir);

          FileUtils.mkdirs(outputDir);
          // generate Crypto.class into input dir
          generateCryptoClassOnce(cryptoClassFile);
          // copy input dir to output dir
          FileUtils.copyDirectory(inputDir, outputDir);
        } catch (Exception e) {
          e.printStackTrace();
        }

        FileUtils.getAllFiles(inputDir).stream()
            .filter(file -> {
              String className = Util.getRelativePath(file, inputDir);
              if (file.getName().endsWith(".class") && !cryptoClassFile.equals(file)) {
                if (extension.includes != null) {
                  return isInclude(extension.includes, className);
                } else if (extension.excludes != null) {
                  return isNotExclude(className);
                } else {
                  return true;
                }
              }
              return false;
            })
            .forEach(inputFile -> {
              try {
                byte[] bytes = generateNewClassByteArray(new FileInputStream(inputFile));
                FileOutputStream fos = new FileOutputStream(Util.getOutputFile(inputDir, inputFile, outputDir));
                fos.write(bytes);
                fos.flush();
                fos.close();
              } catch (IOException e) {
                e.printStackTrace();
              }
            });
      }
    });
  }

  /**
   * for include mode
   *
   * @param className package.class / package.outclass$innerclass
   * @return true if the given class name is start with include rule and end with '/' or '$' or '.class', otherwise
   * return false.
   */
  private boolean isInclude(List<String> includes, String className) {
    String classPath = Util.toSystemIndependentPath(className);
    return includes.stream()
        .map(include -> include.replace(".", "/"))
        .anyMatch(include -> {
          if (classPath.startsWith(include)) {
            String end = classPath.substring(include.length(), classPath.length());
            return end.startsWith("/") || end.startsWith(".class") || end.startsWith("$");
          }
          return false;
        });
  }

  /**
   * for exclude mode
   *
   * @param className package.class / package.outclass$innerclass
   * @return false if the given class name is start with exclude rule and end with '/' or '$' or '.class', otherwise
   * return true.
   */
  private boolean isNotExclude(String className) {
    String classPath = Util.toSystemIndependentPath(className);
    return extension.excludes.stream()
        .map(exclude -> exclude.replace(".", "/"))
        .noneMatch(exclude -> {
          if (classPath.startsWith(exclude)) {
            String end = classPath.substring(exclude.length(), classPath.length());
            return end.startsWith("/") || end.startsWith(".class") || end.startsWith("$");
          }
          return false;
        });
  }

  /**
   * dynamic generate Crypto.class
   *
   * @param dest output file's absolute path
   */
  private void generateCryptoClassOnce(File dest) throws IOException {
    if (hasGeneratedCrypto) {
      return;
    }
    hasGeneratedCrypto = true;

    byte[] bytes = GenerateCode.generate(extension.key);
    Files.createParentDirs(dest);
    FileOutputStream fos = new FileOutputStream(dest);
    fos.write(bytes);
    fos.close();
  }

  /**
   * modify input class and then output to dist file
   *
   * @param inputStream class inputstream to be modified
   */
  private byte[] generateNewClassByteArray(InputStream inputStream) throws IOException {
    ClassReader cr = new ClassReader(inputStream);
    ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
    ModifyClassVisitor cv = new ModifyClassVisitor(Opcodes.ASM5, cw, extension.key);
    cr.accept(cv, Opcodes.ASM5);
    return cw.toByteArray();
  }

}
