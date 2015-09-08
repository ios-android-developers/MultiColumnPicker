package com.twiceyuan.library;

import java.util.List;

/**
 * Created by twiceYuan on 9/7/15.
 * <p/>
 * 左列被选择时，同时返回右侧的结果
 */
public interface OnLeftSelected<Left, Right> {
    List<Right> onLeftSelected(int position, Left left);
}