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

import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.internal.pipeline.TransformManager;

import java.io.IOException;
import java.util.Set;

/**
 * Created by panda on 2018/9/7 下午5:06.
 */
public class MuddyTransform extends Transform {

  private MuddyExtension mExtension;

  MuddyTransform(MuddyExtension extension) {
    mExtension = extension;
  }

  @Override
  public String getName() {
    return "muddy";
  }

  @Override
  public Set<QualifiedContent.ContentType> getInputTypes() {
    return TransformManager.CONTENT_CLASS;
  }

  @Override
  public Set<? super QualifiedContent.Scope> getScopes() {
    return TransformManager.SCOPE_FULL_PROJECT;
  }

  @Override
  public boolean isIncremental() {
    return true;
  }

  @Override
  public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException,
      IOException {
    super.transform(transformInvocation);
    Log.e("transform isIncremental=" + transformInvocation.isIncremental());
    Muddy.transform(transformInvocation, mExtension);
  }

}
