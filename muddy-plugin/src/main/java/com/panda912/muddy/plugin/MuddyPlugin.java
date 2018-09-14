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
      Log.init(project);
      Log.lifecycle("----------------------- Muddy Configuration ------------------------");
      if (muddyExtension.includes != null && muddyExtension.excludes != null) {
        throw new IllegalArgumentException("Muddy's `includes` and `excludes` must not be included at the same time!");
      }

      Log.lifecycle("key: " + muddyExtension.key);

      if (muddyExtension.includes != null) {
        if (muddyExtension.includes.isEmpty()) {
          throw new IllegalArgumentException("Muddy's `includes` must not be empty!");
        }
        if (muddyExtension.includes.stream().anyMatch(String::isEmpty)) {
          throw new IllegalArgumentException("Muddy's `includes` item must not be empty!");
        }
        Log.lifecycle("includes: " + muddyExtension.includes.toString());
      }
      if (muddyExtension.excludes != null) {
        if (muddyExtension.excludes.isEmpty()) {
          throw new IllegalArgumentException("Muddy's `excludes` must not be empty!");
        }
        if (muddyExtension.excludes.stream().anyMatch(String::isEmpty)) {
          throw new IllegalArgumentException("Muddy's `excludes` item must not be empty!");
        }
        Log.lifecycle("excludes: " + muddyExtension.excludes.toString());
      }
      if (muddyExtension.includeLibs != null) {
        if (muddyExtension.includeLibs.isEmpty()) {
          throw new IllegalArgumentException("Muddy's `includeLibs` must not be empty!");
        }
        if (muddyExtension.includeLibs.stream().anyMatch(String::isEmpty)) {
          throw new IllegalArgumentException("Muddy's `includeLibs` item must not be empty!");
        }
        Log.lifecycle("includeLibs: " + muddyExtension.includeLibs.toString());
      }
      Log.lifecycle("----------------------- Muddy Configuration ------------------------");
    });
  }
}
