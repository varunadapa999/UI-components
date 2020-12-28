/*
 * Copyright 2017 The Android Open Source Project
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

package io.material.catalog.feature;

import io.material.catalog.R;

import android.content.Intent;
<<<<<<< HEAD
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
=======
import androidx.fragment.app.Fragment;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
>>>>>>> pr/1944

/** Represents a single demo. */
public abstract class Demo {

  @StringRes private final int titleResId;

  public Demo() {
    this(R.string.cat_demo_landing_row_demo_header);
  }

  public Demo(@StringRes int titleResId) {
    this.titleResId = titleResId;
  }

  @StringRes
  public final int getTitleResId() {
    return titleResId;
  }

  @Nullable
  public Fragment createFragment() {
    return null;
  }

  @Nullable
  public Intent createActivityIntent() {
    return null;
  }
}
