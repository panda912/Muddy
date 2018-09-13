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

import org.gradle.api.Project;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;

/**
 * Created by panda on 2018/9/12 下午6:31.
 */
public class Log {

  private static Logger logger;

  public static void init(Project project) {
    logger = project.getLogger();
  }

  public static void i(String s) {
    if (logger != null && s != null) {
      logger.log(LogLevel.INFO, s);
    }
  }

  public static void d(String s) {
    if (logger != null && s != null) {
      logger.debug(s);
    }
  }

  public static void w(String s) {
    if (logger != null && s != null) {
      logger.warn(s);
    }
  }

  public static void e(String s) {
    if (logger != null && s != null) {
      logger.error(s);
    }
  }

  public static void lifecycle(String s) {
    if (logger != null && s != null) {
      logger.lifecycle(s);
    }
  }
}
