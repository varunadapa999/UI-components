/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.internal;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.PluralsRes;
import androidx.annotation.RestrictTo;
import androidx.annotation.StringRes;
import androidx.core.view.MarginLayoutParamsCompat;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.security.InvalidParameterException;

/**
 * Horizontally lay out children until the row is filled and then moved to the next line. Call
 * {@link FlowLayout#setSingleLine(boolean)} to disable reflow and lay all children out in one line.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class FlowLayout extends ViewGroup {
  private int lineSpacing;
  private int itemSpacing;
  private boolean singleLine;
  private int rowCount;
  private int maxRowCount;
  private boolean overflowChildEnabled;
  private int remainingItems;
  private int overflowChildTextResource;

  public FlowLayout(@NonNull Context context) {
    this(context, null);
  }

  public FlowLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public FlowLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    singleLine = false;
    loadFromAttributes(context, attrs);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public FlowLayout(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    singleLine = false;
    loadFromAttributes(context, attrs);
  }

  private void loadFromAttributes(@NonNull Context context, @Nullable AttributeSet attrs) {
    final TypedArray array =
        context.getTheme().obtainStyledAttributes(attrs, R.styleable.FlowLayout, 0, 0);
    lineSpacing = array.getDimensionPixelSize(R.styleable.FlowLayout_lineSpacing, 0);
    itemSpacing = array.getDimensionPixelSize(R.styleable.FlowLayout_itemSpacing, 0);
    array.recycle();
  }

  protected int getLineSpacing() {
    return lineSpacing;
  }

  protected void setLineSpacing(int lineSpacing) {
    this.lineSpacing = lineSpacing;
  }

  protected int getItemSpacing() {
    return itemSpacing;
  }

  protected void setItemSpacing(int itemSpacing) {
    this.itemSpacing = itemSpacing;
  }

  /** Returns whether this chip group is single line or reflowed multiline. */
  public boolean isSingleLine() {
    return singleLine;
  }

  /** Sets whether this chip group is single line, or reflowed multiline. */
  public void setSingleLine(boolean singleLine) {
    this.singleLine = singleLine;
  }

  public int getMaxRowCount() {
    return maxRowCount;
  }

  /** Sets the maximum rows rendered for children **/
  public void setMaxRowCount(int maxRowCount) { this.maxRowCount = maxRowCount; }

  public boolean isOverflowChildEnabled() {
    return overflowChildEnabled;
  }

  /** Sets whether to render a overflow child view that displays number of remaining items **/
  public void setOverflowChildEnabled(boolean overflowChildEnabled) {
    this.overflowChildEnabled = overflowChildEnabled;
  }

  /** Sets the plural or string resource to use as the text for overflow child ivew */
  public void setOverflowChildTextResource(int overflowChildTextResource) {
    this.overflowChildTextResource = overflowChildTextResource;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    final int width = MeasureSpec.getSize(widthMeasureSpec);
    final int widthMode = MeasureSpec.getMode(widthMeasureSpec);

    final int height = MeasureSpec.getSize(heightMeasureSpec);
    final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

    final int maxWidth =
        widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.EXACTLY
            ? width
            : Integer.MAX_VALUE;

    int childLeft = getPaddingLeft();
    int childTop = getPaddingTop();
    int childBottom = childTop;
    int childRight = childLeft;
    int maxChildRight = 0;

    final int maxRight = maxWidth - getPaddingRight();

    rowCount = 0;

    int children = this.getChildCount();

    remainingItems = children;

    for (int i = 0; i < children; i++) {
      View child = getChildAt(i);

      if (child.getVisibility() == View.GONE) {
        continue;
      }
      measureChild(child, widthMeasureSpec, heightMeasureSpec);

      LayoutParams lp = child.getLayoutParams();
      int leftMargin = 0;
      int rightMargin = 0;
      if (lp instanceof MarginLayoutParams) {
        MarginLayoutParams marginLp = (MarginLayoutParams) lp;
        leftMargin += marginLp.leftMargin;
        rightMargin += marginLp.rightMargin;
      }

      childRight = childLeft + leftMargin + child.getMeasuredWidth();

      // If the current child's right bound exceeds Flowlayout's max right bound and flowlayout is
      // not confined to a single line, move this child to the next line and reset its left bound to
      // flowlayout's left bound.
      if (childRight > maxRight && !isSingleLine()) {
        rowCount++;

        if (this.maxRowCount > 1 && this.maxRowCount <= rowCount) {
          break;
        }

        childLeft = getPaddingLeft();
        childTop = childBottom + lineSpacing;
      }

      childRight = childLeft + leftMargin + child.getMeasuredWidth();
      childBottom = childTop + child.getMeasuredHeight();

      // Updates Flowlayout's max right bound if current child's right bound exceeds it.
      if (childRight > maxChildRight) {
        maxChildRight = childRight;
      }

      childLeft += (leftMargin + rightMargin + child.getMeasuredWidth()) + itemSpacing;

      // For all preceding children, the child's right margin is taken into account in the next
      // child's left bound (childLeft). However, childLeft is ignored after the last child so the
      // last child's right margin needs to be explicitly added to Flowlayout's max right bound.
      if (i == (getChildCount() - 1)) {
        maxChildRight += rightMargin;
      }

      remainingItems--;
    }

    maxChildRight += getPaddingRight();
    childBottom += getPaddingBottom();

    // if we have remaining child views, adjust height to enable showing the `X more child` view on
    // next line
    if (this.overflowChildEnabled && remainingItems > 0) {
      View child = this.getChildAt(this.getChildCount() - 1);

      if (child.getVisibility() != View.GONE) {
        // need to assign TextView text here, otherwise measurement of view width will be wrong
        if (getResourceNameTypeName(this.overflowChildTextResource).equals("plurals")) {

          ((TextView) child).setText(
                  getContext().getResources().getQuantityString(
                          this.overflowChildTextResource, remainingItems, remainingItems
                  )
          );
        } else if (getResourceNameTypeName(this.overflowChildTextResource).equals("string")) {
          ((TextView) child).setText(getContext().getResources().getString(this.overflowChildTextResource));
        } else {
          throw new InvalidParameterException("overflowChildTextResource can only be a plural or string resource");
        }

        this.measureChild(child, widthMeasureSpec, heightMeasureSpec);

        LayoutParams lp = child.getLayoutParams();
        int leftMargin = 0;

        if (lp instanceof MarginLayoutParams) {
          leftMargin += ((MarginLayoutParams) lp).leftMargin;
        }

        childLeft = this.getPaddingLeft();
        childTop = childBottom + lineSpacing;

        childRight = childLeft + leftMargin + child.getMeasuredWidth();
        childBottom = childTop + child.getMeasuredHeight();

        if (childRight > maxChildRight) {
          maxChildRight = childRight;
        }
      }
    }

    // hide any remaining child views
    if (this.overflowChildEnabled) {
      for (int i = 1; i < remainingItems; i++) {
        // get child view, ignoring the last child item as we
        // assume that if we have remaining items, we are showing the last child view
        // which is the `X more childs` view
        View child = this.getChildAt((this.getChildCount() - i - 1));
        if (child != null) {
          child.setVisibility(View.GONE);
        }
      }
    }

    int finalWidth = getMeasuredDimension(width, widthMode, maxChildRight);
    int finalHeight = getMeasuredDimension(height, heightMode, childBottom);
    setMeasuredDimension(finalWidth, finalHeight);
  }

  private String getResourceNameTypeName(int resourceId) {
    Resources resources = getContext().getResources();
    return resources.getResourceTypeName(resourceId);
  }

  private static int getMeasuredDimension(int size, int mode, int childrenEdge) {
    switch (mode) {
      case MeasureSpec.EXACTLY:
        return size;
      case MeasureSpec.AT_MOST:
        return Math.min(childrenEdge, size);
      default: // UNSPECIFIED:
        return childrenEdge;
    }
  }

  @Override
  protected void onLayout(boolean sizeChanged, int left, int top, int right, int bottom) {
    if (getChildCount() == 0) {
      // Do not re-layout when there are no children.
      rowCount = 0;
      return;
    }
    rowCount = 1;

    boolean isRtl = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
    int paddingStart = isRtl ? getPaddingRight() : getPaddingLeft();
    int paddingEnd = isRtl ? getPaddingLeft() : getPaddingRight();
    int childStart = paddingStart;
    int childTop = getPaddingTop();
    int childBottom = childTop;

    final int maxChildEnd = right - left - paddingEnd;


    // if maxRowCount > 0 then can assume that the last child view is used to display the
    // `X more childs` view
    int children = (this.maxRowCount == 0 || !this.overflowChildEnabled) ? this.getChildCount() : this.getChildCount() - 1;

    for (int i = 0; i < children; i++) {
      View child = getChildAt(i);

      if (child.getVisibility() == View.GONE) {
        child.setTag(R.id.row_index_key, -1);
        continue;
      }

      LayoutParams lp = child.getLayoutParams();
      int startMargin = 0;
      int endMargin = 0;
      if (lp instanceof MarginLayoutParams) {
        MarginLayoutParams marginLp = (MarginLayoutParams) lp;
        startMargin = MarginLayoutParamsCompat.getMarginStart(marginLp);
        endMargin = MarginLayoutParamsCompat.getMarginEnd(marginLp);
      }

      int childEnd = childStart + startMargin + child.getMeasuredWidth();

      if (!singleLine && (childEnd > maxChildEnd)) {
        childStart = paddingStart;
        childTop = childBottom + lineSpacing;
        rowCount++;
      }
      child.setTag(R.id.row_index_key, rowCount - 1);

      childEnd = childStart + startMargin + child.getMeasuredWidth();
      childBottom = childTop + child.getMeasuredHeight();

      if (isRtl) {
        child.layout(
            maxChildEnd - childEnd, childTop, maxChildEnd - childStart - startMargin, childBottom);
      } else {
        child.layout(childStart + startMargin, childTop, childEnd, childBottom);
      }

      childStart += (startMargin + endMargin + child.getMeasuredWidth()) + itemSpacing;
    }

    // if we have remaining child views and overflow is enabled
    // display the `x more childs` view on the next line
    if (this.overflowChildEnabled && remainingItems > 0) {
      View child = getChildAt(this.getChildCount() - 1);

      if (child.getVisibility() == View.GONE) {
        child.setTag(R.id.row_index_key, -1);
        return;
      }

      LayoutParams lp = child.getLayoutParams();
      int startMargin = 0;
      if (lp instanceof MarginLayoutParams) {
        MarginLayoutParams marginLp = (MarginLayoutParams) lp;
        startMargin = MarginLayoutParamsCompat.getMarginStart(marginLp);
      }

      if (!singleLine) {
        childStart = paddingStart;
        childTop = childBottom + lineSpacing;
      }

      child.setTag(R.id.row_index_key, rowCount - 1);

      int childEnd = childStart + startMargin + child.getMeasuredWidth();
      childBottom = childTop + child.getMeasuredHeight();

      if (isRtl) {
        child.layout(
                maxChildEnd - childEnd, childTop, maxChildEnd - childStart - startMargin, childBottom);
      } else {
        child.layout(childStart + startMargin, childTop, childEnd, childBottom);
      }
    }
  }

  protected int getRowCount() {
    return rowCount;
  }

  /** Gets the row index of the child, primarily for accessibility.   */
  public int getRowIndex(@NonNull View child) {
    Object index = child.getTag(R.id.row_index_key);
    if (!(index instanceof Integer)) {
      return -1;
    }
    return (int) index;
  }
}
