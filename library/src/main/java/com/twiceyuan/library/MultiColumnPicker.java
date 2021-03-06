package com.twiceyuan.library;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.twiceyuan.library.adapter.ColumnAdapter;
import com.twiceyuan.library.adapter.SimpleLeftAdapter;
import com.twiceyuan.library.adapter.SimpleRightAdapter;
import com.twiceyuan.library.listener.OnLeftSelected;
import com.twiceyuan.library.listener.OnRightSelected;
import com.twiceyuan.library.map.LeftStringMapper;
import com.twiceyuan.library.map.MapId;
import com.twiceyuan.library.map.MapString;
import com.twiceyuan.library.map.RightStringMapper;

import java.util.List;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class MultiColumnPicker<Parent, Child> implements LeftStringMapper<Parent>, RightStringMapper<Child> {

    private final String TAG = "MultiColumnPicker";

    private Partner mLeftCreatePartner = Partner.build();

    private Context mContext;

    private OnLeftSelected<Parent, Child>  mOnLeftSelected; // 父节点被选择时的回调
    private OnRightSelected<Parent, Child> mOnRightSelected;
    private MapString<Parent>              mMapLeftString;
    private MapString<Child>               mMapRightString;
    private MapId<Parent>                  mMapLeftId;
    private MapId<Child>                   mMapRightId;
    private OnLeftAdapterProvide<Parent>   mOnLeftAdapterProvide;
    private OnRightAdapterProvide<Child>   mOnRightAdapterProvide;
    private List<Parent>                   mParentData;

    private LinearLayout mContainer;
    private ListView     mParentListView;
    private ListView     mChildListView;
    private View         mRoot;
    private AlertDialog  mDialog;

    private ColumnAdapter<Parent> mLeftAdapter;
    private ColumnAdapter<Child>  mRightAdapter;

    private int mDefaultLeftPosition  = 0;
    private int mDefaultRightPosition = 0;

    private DialogHelper.Size mSize;

    public MultiColumnPicker(Context context) {
        mContext = context;
        mRoot = View.inflate(context, R.layout.multicolomn_dialog_picker, null);
        mContainer = (LinearLayout) mRoot.findViewById(R.id.ll_container);
        mParentListView = (ListView) mRoot.findViewById(R.id.lv_left);
        mChildListView = (ListView) mRoot.findViewById(R.id.lv_right);
    }

    /**
     * 配置左侧内容
     */
    public MultiColumnPicker setLeftContent(final List<Parent> parents) {
        mParentData = parents;
        mParentListView.setOnItemClickListener((parent, view, position, id) -> {
            mParentListView.setItemChecked(position, true);
            if (mOnLeftSelected != null) {
                final List<Child> children = mOnLeftSelected.onLeftSelected(position, parents.get(position));

                // 判断是否提供自定义 Adapter
                if (mOnRightAdapterProvide != null) {
                    mRightAdapter = mOnRightAdapterProvide.provideRightAdapter(this, children);
                } else {
                    mRightAdapter = new SimpleRightAdapter<>(children, this);
                }
                mChildListView.setAdapter(mRightAdapter);
                mLeftCreatePartner.before();
                mChildListView.setOnItemClickListener((parent1, view1, position2, id1) -> {
                    if (mOnRightSelected != null) {
                        mOnRightSelected.onRightSelected(parents.get(position), children.get(position2));
                        mDialog.dismiss();
                    }
                });
            }
        });
        return this;
    }

    /**
     * 配置左侧监听器（联动右侧适配器）
     */
    public MultiColumnPicker setOnLeftSelected(OnLeftSelected<Parent, Child> onLeftSelected) {
        mOnLeftSelected = onLeftSelected;
        return this;
    }

    /**
     * 配置右侧监听器（结束回调结果）
     */
    public MultiColumnPicker setOnRightSelected(OnRightSelected<Parent, Child> onRightSelected) {
        mOnRightSelected = onRightSelected;
        return this;
    }

    public MultiColumnPicker setMapLeftString(MapString<Parent> mapLeftString) {
        mMapLeftString = mapLeftString;
        return this;
    }

    public MultiColumnPicker setMapRightString(MapString<Child> mapRightString) {
        mMapRightString = mapRightString;
        return this;
    }

    public void setMapLeftId(MapId<Parent> mapLeftId) {
        mMapLeftId = mapLeftId;
    }

    public void setMapRightId(MapId<Child> mapRightId) {
        mMapRightId = mapRightId;
    }

    /**
     * 设置默认值
     *
     * @param position 左列默认位置
     */
    public MultiColumnPicker setLeftDefaultPosition(int position) {
        mDefaultLeftPosition = position;
        return this;
    }

    /**
     * 设置默认值
     *
     * @param defaultId 默认值 ID
     */
    public MultiColumnPicker setLeftDefaultId(Object defaultId) {
        if (mMapLeftId == null) {
            throw new NoSuchMethodError("没有配置 MapLeftId");
        }
        for (int i = 0; i < mParentData.size(); i++) {
            if (mMapLeftId.getId(mParentData.get(i)).equals(defaultId)) {
                mDefaultLeftPosition = i;
                return this;
            }
        }
        Log.e(TAG, "没有找到 ID 为" + defaultId + "的选项");
        return this;
    }

    /**
     * 设置默认值
     *
     * @param defaultString 默认值的显示文字
     */
    public MultiColumnPicker setLeftDefaultString(String defaultString) {
        if (mMapLeftString == null) {
            throw new NoSuchMethodError("没有配置 MapLeftString");
        }
        for (int i = 0; i < mParentData.size(); i++) {
            if (mMapLeftString.getString(mParentData.get(i)).equals(defaultString)) {
                mDefaultLeftPosition = i;
                return this;
            }
        }
        Log.e(TAG, "没有找到 String 为" + defaultString + "的选项");
        return this;
    }

    /**
     * 设置默认值
     *
     * @param position 左列默认位置
     */
    public MultiColumnPicker setRightDefaultPosition(int position) {
        mDefaultRightPosition = position;
        return this;
    }

    /**
     * 设置默认值
     *
     * @param defaultId 默认值 ID
     */
    public MultiColumnPicker setRightDefaultId(Object leftId, Object defaultId) {
        if (mMapRightId == null) {
            throw new NoSuchMethodError("没有配置 MapLeftId");
        }
        setLeftDefaultId(leftId);
        Parent item = mParentData.get(mDefaultLeftPosition);
        List<Child> children = mOnLeftSelected.onLeftSelected(mDefaultLeftPosition, item);
        for (int i = 0; i < children.size(); i++) {
            if (mMapRightId.getId(children.get(i)).equals(defaultId)) {
                mDefaultRightPosition = i;
                return this;
            }
        }
        Log.e(TAG, "没有找到 ID 为" + defaultId + "的选项");
        return this;
    }

    @Override
    public String mapLeftString(Parent parent) {
        if (mMapLeftString != null) {
            return mMapLeftString.getString(parent);
        }
        return "";
    }

    @Override
    public String mapRightString(Child child) {
        if (mMapRightString != null) {
            return mMapRightString.getString(child);
        }
        return "";
    }

    /**
     * 配置权重比（默认 1:2）
     *
     * @param left  左列权重
     * @param right 右列权重
     */
    public MultiColumnPicker setWeight(int left, int right) {
        mContainer.setWeightSum(left + right);
        mParentListView.setLayoutParams(
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, left));
        mChildListView.setLayoutParams(
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, right));
        return this;
    }

    /**
     * 设置对话框尺寸（默认都为 match_parent）
     *
     * @param width  宽度 (dp)
     * @param height 高度 (dp)
     */
    public MultiColumnPicker setSize(float width, float height) {
        // dp -> px
        int widthPx = DensityUtil.dip2px(mContext, width);
        int heightPx = DensityUtil.dip2px(mContext, height);

        // compare screen size and set screen size if the value too large
        int screenWidth = DensityUtil.screenWidth(mContext);
        int screenHeight = DensityUtil.screenHeight(mContext);
        widthPx = widthPx > screenWidth ? screenWidth : widthPx;
        heightPx = heightPx > screenHeight ? screenHeight : heightPx;
        mSize = new DialogHelper.Size(widthPx, heightPx);
        return this;
    }

    public interface OnLeftAdapterProvide<Left> {
        ColumnAdapter<Left> provideLeftAdapter(LeftStringMapper<Left> mapper, List<Left> lefts);
    }

    public interface OnRightAdapterProvide<Right> {
        ColumnAdapter<Right> provideRightAdapter(RightStringMapper<Right> mapper, List<Right> rights);
    }

    /**
     * 配置左侧适配器回调
     *
     * @param onLeftAdapterProvide 左侧适配器回调
     */
    public void setLeftAdapter(OnLeftAdapterProvide<Parent> onLeftAdapterProvide) {
        mOnLeftAdapterProvide = onLeftAdapterProvide;
    }

    /**
     * 配置右侧适配器回调
     *
     * @param onRightAdapterProvide 右侧适配器回调
     */
    public void setRightAdapter(OnRightAdapterProvide<Child> onRightAdapterProvide) {
        mOnRightAdapterProvide = onRightAdapterProvide;
    }

    public ListView getLeftView() {
        return mParentListView;
    }

    public ListView getRightView() {
        return mChildListView;
    }

    /**
     * 显示
     */
    public void show() {
        // 判断是否提供自定义 Adapter
        if (mOnLeftAdapterProvide != null) {
            mLeftAdapter = mOnLeftAdapterProvide.provideLeftAdapter(this, mParentData);
        } else {
            mLeftAdapter = new SimpleLeftAdapter<>(mParentData, this);
        }
        mParentListView.setAdapter(mLeftAdapter);
        // 滚到默认值
        mParentListView.performItemClick(mParentListView.getChildAt(mDefaultLeftPosition), mDefaultLeftPosition, 0);
        mParentListView.setSelection(mDefaultLeftPosition);

        mLeftCreatePartner.after(() -> {
            mChildListView.setSelection(mDefaultRightPosition);
            mChildListView.setItemChecked(mDefaultRightPosition, true);
            mChildListView.smoothScrollToPosition(mDefaultRightPosition);
        });

        mDialog = new AlertDialog.Builder(mContext)
                .setView(mRoot)
                .show();
        if (mSize != null) {
            mSize.resize(mDialog);
        }
    }
}
