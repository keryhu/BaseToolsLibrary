package com.excellence.basetoolslibrary.databinding;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.excellence.basetoolslibrary.databinding.MultiItemTypeBindingRecyclerListAdapter.RecyclerViewHolder;
import com.excellence.basetoolslibrary.databinding.base.ItemViewDelegate;
import com.excellence.basetoolslibrary.databinding.base.ItemViewDelegateManager;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2019/11/14
 *     desc   : 开启dataBinding，多种类型布局RecyclerView {@link ListAdapter}通用适配器
 * </pre> 
 */
public abstract class MultiItemTypeBindingRecyclerListAdapter<T, VH extends RecyclerViewHolder> extends ListAdapter<T, VH> {

    private ItemViewDelegateManager<T> mItemViewDelegateManager = null;
    private OnItemKeyListener mOnItemKeyListener = null;
    private OnItemClickListener mOnItemClickListener = null;
    private OnItemLongClickListener mOnItemLongClickListener = null;
    private OnItemFocusChangeListener mOnItemFocusChangeListener = null;

    public MultiItemTypeBindingRecyclerListAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback) {
        super(diffCallback);
        mItemViewDelegateManager = new ItemViewDelegateManager<>();
    }

    public MultiItemTypeBindingRecyclerListAdapter(@NonNull AsyncDifferConfig<T> config) {
        super(config);
        mItemViewDelegateManager = new ItemViewDelegateManager<>();
    }

    /**
     * 添加视图
     *
     * @param delegate 视图
     * @return
     */
    public MultiItemTypeBindingRecyclerListAdapter<T, VH> addItemViewDelegate(ItemViewDelegate<T> delegate) {
        mItemViewDelegateManager.addDelegate(delegate);
        return this;
    }

    /**
     * 添加视图
     *
     * @param viewType 布局类型
     * @param delegate 视图
     * @return
     */
    public MultiItemTypeBindingRecyclerListAdapter<T, VH> addItemViewDelegate(int viewType, ItemViewDelegate<T> delegate) {
        mItemViewDelegateManager.addDelegate(viewType, delegate);
        return this;
    }

    /**
     * 移除视图
     *
     * @param delegate 视图
     * @return
     */
    public MultiItemTypeBindingRecyclerListAdapter<T, VH> removeItemViewDelegate(ItemViewDelegate<T> delegate) {
        mItemViewDelegateManager.removeDelegate(delegate);
        return this;
    }

    /**
     * 移除视图
     *
     * @param viewType 布局类型
     * @return
     */
    public MultiItemTypeBindingRecyclerListAdapter<T, VH> removeItemViewDelegate(int viewType) {
        mItemViewDelegateManager.removeDelegate(viewType);
        return this;
    }

    /**
     * 判断视图是否可用
     *
     * @return {@code true}:是<br>{@code false}:否
     */
    private boolean userItemViewDelegateManager() {
        return mItemViewDelegateManager.getItemViewDelegateCount() > 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (userItemViewDelegateManager()) {
            return mItemViewDelegateManager.getItemViewType(getItem(position), position);
        }
        return super.getItemViewType(position);
    }

    @Override
    public T getItem(int position) {
        return super.getItem(position);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = mItemViewDelegateManager.getItemViewLayoutId(viewType);
        ViewDataBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), layoutId, parent, false);
        return onCreateViewHolder(binding);
    }

    protected abstract VH onCreateViewHolder(ViewDataBinding binding);

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ItemViewDelegate<T> delegate = mItemViewDelegateManager.getItemViewDelegate(getItemViewType(position));
        ViewDataBinding binding = holder.getBinding();
        binding.setVariable(delegate.getItemVariable(), getItem(position));
        binding.executePendingBindings();
        setViewListener(binding, position);
    }

    protected void setViewListener(final ViewDataBinding binding, final int position) {
        View itemView = binding.getRoot();

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(binding, v, position);
                }
            }
        });

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return mOnItemLongClickListener != null && mOnItemLongClickListener.onItemLongClick(binding, v, position);
            }
        });

        itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (mOnItemFocusChangeListener != null) {
                    mOnItemFocusChangeListener.onItemFocusChange(binding, v, hasFocus, position);
                }
            }
        });

        itemView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return mOnItemKeyListener != null && mOnItemKeyListener.onKey(binding, v, keyCode, event, position);
            }
        });
    }

    public interface OnItemClickListener {
        void onItemClick(ViewDataBinding binding, View v, int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(ViewDataBinding binding, View v, int position);
    }

    public interface OnItemFocusChangeListener {
        void onItemFocusChange(ViewDataBinding binding, View v, boolean hasFocus, int position);
    }

    public interface OnItemKeyListener {
        boolean onKey(ViewDataBinding binding, View v, int keyCode, KeyEvent event, int position);
    }

    public void setOnItemKeyListener(OnItemKeyListener onItemKeyListener) {
        mOnItemKeyListener = onItemKeyListener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
    }

    public void setOnItemFocusChangeListener(OnItemFocusChangeListener listener) {
        mOnItemFocusChangeListener = listener;
    }

    /**
     * 注意添加 static ，否则没有Javadoc红色错误，但是在编译时会报“方法不会覆盖或实现超类型的方法”的异常
     *
     * DataBinding可以直接设置控件属性，无需 {@link com.excellence.basetoolslibrary.helper.ViewHelper}
     */
    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        private ViewDataBinding mBinding = null;

        public RecyclerViewHolder(ViewDataBinding dataBinding) {
            super(dataBinding.getRoot());
            mBinding = dataBinding;
        }

        public ViewDataBinding getBinding() {
            return mBinding;
        }
    }
}
