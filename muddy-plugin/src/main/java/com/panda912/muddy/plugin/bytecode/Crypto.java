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

/**
 * Created by panda on 2018/9/7 下午5:19.
 */
public class Crypto {

  private static int KEY;
  public static void setKey(int key) {
    KEY = key;
  }

  public static String encode(String plainText) {
    return xor(plainText);
  }

  public static String decode(String cipherText) {
    return xor(cipherText);
  }

  private static String xor(String str) {
    char[] c = str.toCharArray();
    for (int i = 0; i < c.length; i++) {
      c[i] = (char) (c[i] ^ KEY);
    }
    return new String(c);
  }
}
