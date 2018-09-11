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

import com.android.build.gradle.BaseExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Created by panda on 2018/9/7 下午5:00.
 */
public class MuddyPlugin implements Plugin<Project> {

  @Override
  public void apply(Project project) {
    BaseExtension android = (BaseExtension) project.getExtensions().getByName("android");
    MuddyExtension muddyExtension = project.getExtensions().create("muddy", MuddyExtension.class);
    MuddyTransform muddyTransform = new MuddyTransform(muddyExtension);
    android.registerTransform(muddyTransform);

    project.afterEvaluate(p -> {
      System.out.println("-------------MuddyTransform--------------");
      System.out.println("key: " + muddyExtension.key);
      if (muddyExtension.includes != null) {
        System.out.println("includes: " + muddyExtension.includes.toString());
      }
      if (muddyExtension.excludes != null) {
        if (muddyExtension.excludes.stream().anyMatch(exclude -> exclude.contains("$"))) {
          throw new IllegalArgumentException("Muddy's 'exlcudes' donot support inner class!");
        }
        System.out.println("excludes: " + muddyExtension.excludes.toString());
      }
      System.out.println("-------------MuddyTransform--------------");
    });
  }
}
