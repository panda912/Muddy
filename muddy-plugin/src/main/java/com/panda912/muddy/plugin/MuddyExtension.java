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

package com.panda912.muddy.plugin;

import java.util.List;

/**
 * Created by panda on 2018/9/7 下午5:06.
 */
public class MuddyExtension {
  /**
   * the muddy key
   */
  public int key;
  /**
   * include project's package or class. must be start with root package.
   * if you config this attribute, it means that the project will not be muddy except 'includes'.
   * can not be used with 'excludes'.
   */
  public List<String> includes;
  /**
   * exclude project's package or class. must be start with root package.
   * if you config this attribute, it means that the project will be muddy except 'excludes'.
   * can not be used with 'excludes'.
   */
  public List<String> excludes;
  /**
   * include third libraries. p.s. not moddy jars by default.
   */
  public List<String> includeLibs;
}
