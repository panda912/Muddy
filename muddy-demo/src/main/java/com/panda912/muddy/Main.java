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

package com.panda912.muddy;

/**
 * Created by panda on 2018/9/11 下午4:59.
 */
public class Main extends IMain {

  private String str;

  Main() {
    System.out.println(Thrid.THRID);
  }

  public void m() {
    str = "this is ...";
  }

  public class Second {
    private static final String SECOND = "second";

    public Second() {
      System.out.println(str);
    }

    private class Four {
      private static final String FOUR = "Four";
    }
  }

  public static class Thrid {
    public static final String THRID = "Thrid";
  }
}
