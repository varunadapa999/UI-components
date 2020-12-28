/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.bottomnavigation;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.Resources;
<<<<<<< HEAD
import android.graphics.drawable.Drawable;
import androidx.annotation.Dimension;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;
import com.google.android.material.internal.TextScale;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;
import androidx.core.util.Pools;
import androidx.core.view.ViewCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.view.menu.MenuView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MenuItem;
=======
import androidx.core.view.ViewCompat;
import androidx.appcompat.view.menu.MenuBuilder;
import android.view.Gravity;
>>>>>>> pr/1944
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import com.google.android.material.navigation.NavigationBarItemView;
import com.google.android.material.navigation.NavigationBarMenuView;

/** @hide For internal use only. */
@RestrictTo(LIBRARY_GROUP)
public class BottomNavigationMenuView extends NavigationBarMenuView {
  private final int inactiveItemMaxWidth;
  private final int inactiveItemMinWidth;
  private final int activeItemMaxWidth;
  private final int activeItemMinWidth;
  private final int itemHeight;

  private boolean itemHorizontalTranslationEnabled;
  private int[] tempChildWidths;

  public BottomNavigationMenuView(@NonNull Context context) {
    super(context);

    FrameLayout.LayoutParams params =
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    params.gravity = Gravity.CENTER;
    setLayoutParams(params);

    final Resources res = getResources();
    inactiveItemMaxWidth =
        res.getDimensionPixelSize(R.dimen.design_bottom_navigation_item_max_width);
    inactiveItemMinWidth =
        res.getDimensionPixelSize(R.dimen.design_bottom_navigation_item_min_width);
    activeItemMaxWidth =
        res.getDimensionPixelSize(R.dimen.design_bottom_navigation_active_item_max_width);
    activeItemMinWidth =
        res.getDimensionPixelSize(R.dimen.design_bottom_navigation_active_item_min_width);
    itemHeight = res.getDimensionPixelSize(R.dimen.design_bottom_navigation_height);

    tempChildWidths = new int[BottomNavigationView.MAX_ITEM_COUNT];
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    final MenuBuilder menu = getMenu();
    final int width = MeasureSpec.getSize(widthMeasureSpec);
    // Use visible item count to calculate widths
    final int visibleCount = menu.getVisibleItems().size();
    // Use total item counts to measure children
    final int totalCount = getChildCount();

    final int heightSpec = MeasureSpec.makeMeasureSpec(itemHeight, MeasureSpec.EXACTLY);

    if (isShifting(getLabelVisibilityMode(), visibleCount)
        && isItemHorizontalTranslationEnabled()) {
      final View activeChild = getChildAt(getSelectedItemPosition());
      int activeItemWidth = activeItemMinWidth;
      if (activeChild.getVisibility() != View.GONE) {
        // Do an AT_MOST measure pass on the active child to get its desired width, and resize the
        // active child view based on that width
        activeChild.measure(
            MeasureSpec.makeMeasureSpec(activeItemMaxWidth, MeasureSpec.AT_MOST), heightSpec);
        activeItemWidth = Math.max(activeItemWidth, activeChild.getMeasuredWidth());
      }
      final int inactiveCount = visibleCount - (activeChild.getVisibility() != View.GONE ? 1 : 0);
      final int activeMaxAvailable = width - inactiveCount * inactiveItemMinWidth;
      final int activeWidth =
          Math.min(activeMaxAvailable, Math.min(activeItemWidth, activeItemMaxWidth));
      final int inactiveMaxAvailable =
          (width - activeWidth) / (inactiveCount == 0 ? 1 : inactiveCount);
      final int inactiveWidth = Math.min(inactiveMaxAvailable, inactiveItemMaxWidth);
      int extra = width - activeWidth - inactiveWidth * inactiveCount;

      for (int i = 0; i < totalCount; i++) {
        if (getChildAt(i).getVisibility() != View.GONE) {
          tempChildWidths[i] = (i == getSelectedItemPosition()) ? activeWidth : inactiveWidth;
          // Account for integer division which sometimes leaves some extra pixel spaces.
          // e.g. If the nav was 10px wide, and 3 children were measured to be 3px-3px-3px, there
          // would be a 1px gap somewhere, which this fills in.
          if (extra > 0) {
            tempChildWidths[i]++;
            extra--;
          }
        } else {
          tempChildWidths[i] = 0;
        }
      }
    } else {
      final int maxAvailable = width / (visibleCount == 0 ? 1 : visibleCount);
      final int childWidth = Math.min(maxAvailable, activeItemMaxWidth);
      int extra = width - childWidth * visibleCount;
      for (int i = 0; i < totalCount; i++) {
        if (getChildAt(i).getVisibility() != View.GONE) {
          tempChildWidths[i] = childWidth;
          if (extra > 0) {
            tempChildWidths[i]++;
            extra--;
          }
        } else {
          tempChildWidths[i] = 0;
        }
      }
    }

    int totalWidth = 0;
    for (int i = 0; i < totalCount; i++) {
      final View child = getChildAt(i);
      if (child.getVisibility() == GONE) {
        continue;
      }
      child.measure(
          MeasureSpec.makeMeasureSpec(tempChildWidths[i], MeasureSpec.EXACTLY), heightSpec);
      ViewGroup.LayoutParams params = child.getLayoutParams();
      params.width = child.getMeasuredWidth();
      totalWidth += child.getMeasuredWidth();
    }
    setMeasuredDimension(
        View.resolveSizeAndState(
            totalWidth, MeasureSpec.makeMeasureSpec(totalWidth, MeasureSpec.EXACTLY), 0),
        View.resolveSizeAndState(itemHeight, heightSpec, 0));
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    final int count = getChildCount();
    final int width = right - left;
    final int height = bottom - top;
    int used = 0;
    for (int i = 0; i < count; i++) {
      final View child = getChildAt(i);
      if (child.getVisibility() == GONE) {
        continue;
      }
      if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
        child.layout(width - used - child.getMeasuredWidth(), 0, width - used, height);
      } else {
        child.layout(used, 0, child.getMeasuredWidth() + used, height);
      }
      used += child.getMeasuredWidth();
    }
  }

  /**
   * Sets whether the menu items horizontally translate on selection when the combined item widths
   * fill the screen.
   *
   * @param itemHorizontalTranslationEnabled whether the menu items horizontally translate on
   *     selection
   * @see #isItemHorizontalTranslationEnabled()
   */
  public void setItemHorizontalTranslationEnabled(boolean itemHorizontalTranslationEnabled) {
    this.itemHorizontalTranslationEnabled = itemHorizontalTranslationEnabled;
  }

  /**
   * Returns whether the menu items horizontally translate on selection when the combined item
   * widths fill the screen.
   *
   * @return whether the menu items horizontally translate on selection
   * @see #setItemHorizontalTranslationEnabled(boolean)
   */
  public boolean isItemHorizontalTranslationEnabled() {
    return itemHorizontalTranslationEnabled;
  }

<<<<<<< HEAD
  public ColorStateList createDefaultColorStateList(int baseColorThemeAttr) {
    final TypedValue value = new TypedValue();
    if (!getContext().getTheme().resolveAttribute(baseColorThemeAttr, value, true)) {
      return null;
    }
    ColorStateList baseColor = AppCompatResources.getColorStateList(getContext(), value.resourceId);
    if (!getContext()
        .getTheme()
        .resolveAttribute(androidx.appcompat.R.attr.colorPrimary, value, true)) {
      return null;
    }
    int colorPrimary = value.data;
    int defaultColor = baseColor.getDefaultColor();
    return new ColorStateList(
        new int[][] {DISABLED_STATE_SET, CHECKED_STATE_SET, EMPTY_STATE_SET},
        new int[] {
          baseColor.getColorForState(DISABLED_STATE_SET, defaultColor), colorPrimary, defaultColor
        });
  }

  public void setPresenter(BottomNavigationPresenter presenter) {
    this.presenter = presenter;
  }

  public void buildMenuView() {
    removeAllViews();
    if (buttons != null) {
      for (BottomNavigationItemView item : buttons) {
        if (item != null) {
          itemPool.release(item);
        }
      }
    }
    if (menu.size() == 0) {
      selectedItemId = 0;
      selectedItemPosition = 0;
      buttons = null;
      return;
    }
    buttons = new BottomNavigationItemView[menu.size()];
    boolean shifting = isShifting(labelVisibilityMode, menu.getVisibleItems().size());
    for (int i = 0; i < menu.size(); i++) {
      presenter.setUpdateSuspended(true);
      menu.getItem(i).setCheckable(true);
      presenter.setUpdateSuspended(false);
      BottomNavigationItemView child = getNewItem();
      buttons[i] = child;
      child.setIconTintList(itemIconTint);
      child.setIconSize(itemIconSize);
      // Set the text color the default, then look for another text color in order of precedence.
      child.setTextColor(itemTextColorDefault);
      child.setTextAppearanceInactive(itemTextAppearanceInactive);
      child.setTextAppearanceActive(itemTextAppearanceActive);
      child.setTextColor(itemTextColorFromUser);
      if (itemBackground != null) {
        child.setItemBackground(itemBackground);
      } else {
        child.setItemBackground(itemBackgroundRes);
      }
      child.setShifting(shifting);
      child.setLabelVisibilityMode(labelVisibilityMode);
      child.initialize((MenuItemImpl) menu.getItem(i), 0);
      child.setItemPosition(i);
      child.setOnClickListener(onClickListener);
      addView(child);
    }
    selectedItemPosition = Math.min(menu.size() - 1, selectedItemPosition);
    menu.getItem(selectedItemPosition).setChecked(true);
  }

  public void updateMenuView() {
    if (menu == null || buttons == null) {
      return;
    }

    final int menuSize = menu.size();
    if (menuSize != buttons.length) {
      // The size has changed. Rebuild menu view from scratch.
      buildMenuView();
      return;
    }

    int previousSelectedId = selectedItemId;

    for (int i = 0; i < menuSize; i++) {
      MenuItem item = menu.getItem(i);
      if (item.isChecked()) {
        selectedItemId = item.getItemId();
        selectedItemPosition = i;
      }
    }
    if (previousSelectedId != selectedItemId) {
      // Note: this has to be called before BottomNavigationItemView#initialize().
      TransitionManager.beginDelayedTransition(this, set);
    }

    boolean shifting = isShifting(labelVisibilityMode, menu.getVisibleItems().size());
    for (int i = 0; i < menuSize; i++) {
      presenter.setUpdateSuspended(true);
      buttons[i].setLabelVisibilityMode(labelVisibilityMode);
      buttons[i].setShifting(shifting);
      buttons[i].initialize((MenuItemImpl) menu.getItem(i), 0);
      presenter.setUpdateSuspended(false);
    }
  }

  private BottomNavigationItemView getNewItem() {
    BottomNavigationItemView item = itemPool.acquire();
    if (item == null) {
      item = new BottomNavigationItemView(getContext());
    }
    return item;
  }

  public int getSelectedItemId() {
    return selectedItemId;
  }

  private boolean isShifting(@LabelVisibilityMode int labelVisibilityMode, int childCount) {
    return labelVisibilityMode == LabelVisibilityMode.LABEL_VISIBILITY_AUTO
        ? childCount > 3
        : labelVisibilityMode == LabelVisibilityMode.LABEL_VISIBILITY_SELECTED;
  }

  void tryRestoreSelectedItemId(int itemId) {
    final int size = menu.size();
    for (int i = 0; i < size; i++) {
      MenuItem item = menu.getItem(i);
      if (itemId == item.getItemId()) {
        selectedItemId = itemId;
        selectedItemPosition = i;
        item.setChecked(true);
        break;
      }
    }
=======
  @Override
  @NonNull
  protected NavigationBarItemView createNavigationBarItemView(@NonNull Context context) {
    return new BottomNavigationItemView(context);
>>>>>>> pr/1944
  }
}
