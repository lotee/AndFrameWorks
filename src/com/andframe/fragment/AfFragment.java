package com.andframe.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.Toast;

import com.andframe.activity.framework.AfActivity;
import com.andframe.activity.framework.AfPageable;
import com.andframe.activity.framework.AfView;
import com.andframe.annotation.interpreter.Injecter;
import com.andframe.annotation.interpreter.ViewBinder;
import com.andframe.annotation.view.BindLayout;
import com.andframe.application.AfApplication;
import com.andframe.application.AfDaemonThread;
import com.andframe.application.AfExceptionHandler;
import com.andframe.exception.AfToastException;
import com.andframe.feature.AfBundle;
import com.andframe.feature.AfDialogBuilder;
import com.andframe.feature.AfIntent;
import com.andframe.feature.AfSoftInputer;
import com.andframe.thread.AfData2Task;
import com.andframe.thread.AfData3Task;
import com.andframe.thread.AfDataTask;
import com.andframe.thread.AfTask;
import com.andframe.thread.AfThreadWorker;
import com.andframe.util.java.AfReflecter;

import java.util.Date;
import java.util.List;

/**
 * 框架 AfFragment
 *
 * @author 树朾
 *         <p/>
 *         以下是 AfFragment 像子类提供的 功能方法
 *         <p/>
 *         protected void buildThreadWorker()
 *         为本页面开启一个独立后台线程 供 postTask 的 任务(AfTask)运行 注意：开启线程之后 postTask
 *         任何任务都会在该线程中运行。 如果 postTask 前一个任务未完成，后一个任务将等待
 *         <p/>
 *         protected AfTask postTask(AfTask task)
 *         抛送任务到Worker执行
 *         <p/>
 *         AfSoftInputPageListener 接口中的方法
 *         public void onSoftInputShown();
 *         public void onSoftInputHiden();
 *         public void onQueryChanged();
 *         }
 */
public abstract class AfFragment extends Fragment implements AfPageable {

    @SuppressWarnings("unused")
    public static final String EXTRA_DATA = "EXTRA_DATA";//通用数据传递标识
    @SuppressWarnings("unused")
    public static final String EXTRA_INDEX = "EXTRA_INDEX";//通用下标栓地标识
    @SuppressWarnings("unused")
    public static final String EXTRA_RESULT = "EXTRA_RESULT";//通用返回传递标识
    @SuppressWarnings("unused")
    public static final String EXTRA_MAIN = "EXTRA_MAIN";//主要数据传递标识
    @SuppressWarnings("unused")
    public static final String EXTRA_DEPUTY = "EXTRA_DEPUTY";//主要数据传递标识

    @SuppressWarnings("unused")
    public static final int LP_MP = LayoutParams.MATCH_PARENT;
    @SuppressWarnings("unused")
    public static final int LP_WC = LayoutParams.WRAP_CONTENT;
    // 根视图
    protected View mRootView = null;

    protected AfThreadWorker mWorker = null;
    protected AfDialogBuilder mDialogBuilder = null;
    protected boolean mIsRecycled = false;

    @Nullable
    @Override
    public View getView() {
        return mRootView;
    }

    public AfActivity getAfActivity() {
        if (super.getActivity() instanceof AfActivity) {
            return ((AfActivity) super.getActivity());
        }
        return AfApplication.getApp().getCurActivity();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mDialogBuilder = newDialogBuilder();
    }

    /**
     * 获取LOG日志 TAG 是 AfFragment 的方法
     * 用户也可以重写自定义TAG,这个值AfActivity在日志记录时候会使用
     * 子类实现也可以使用
     */
    protected String TAG() {
        return "AfFragment(" + getClass().getName() + ")";
    }

    protected String TAG(String tag) {
        return "AfFragment(" + getClass().getName() + ")." + tag;
    }

    /**
     * 判断是否被回收
     *
     * @return true 已经被回收
     */
    @Override
    public boolean isRecycled() {
        return mIsRecycled;
    }

    /**
     * 为本页面开启一个独立后台线程 供 postTask 的 任务(AfTask)运行 注意：开启线程之后 postTask
     * 任何任务都会在该线程中运行。 如果 postTask 前一个任务未完成，后一个任务将等待
     */
    @SuppressWarnings("unused")
    protected void buildThreadWorker() {
        if (mWorker == null) {
            mWorker = new AfThreadWorker(this.getClass().getSimpleName());
        }
    }

    /**
     * 抛送任务到Worker执行
     * @param task 任务标识
     */
    public <T extends AfTask> T postTask(T task) {
        if (mWorker != null) {
            return mWorker.postTask(task);
        }
        return AfDaemonThread.postTask(task);
    }

    /**
     * 抛送带数据任务到Worker执行
     */
    public <T> AfDataTask postDataTask(T t, AfDataTask.OnTaskHandlerListener<T> task) {
        return postTask(new AfDataTask<>(t, task));
    }

    /**
     * 抛送带数据任务到Worker执行
     */
    public <T, TT> AfData2Task postDataTask(T t, TT tt, AfData2Task.OnData2TaskHandlerListener<T, TT> task) {
        return postTask(new AfData2Task<>(t, tt, task));
    }

    /**
     * 抛送带数据任务到Worker执行
     */
    public <T, TT, TTT> AfData3Task postDataTask(T t, TT tt, TTT ttt, AfData3Task.OnData3TaskHandlerListener<T, TT, TTT> task) {
        return postTask(new AfData3Task<>(t, tt, ttt, task));
    }

    @Override
    public void startActivity(Class<? extends Activity> clazz) {
        startActivity(new Intent(getAfActivity(), clazz));
    }

    public void startActivity(Class<? extends Activity> clazz,Object... args) {
        AfIntent intent = new AfIntent(getAfActivity(), clazz);
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length / 2; i++) {
                if (args[2 * i] instanceof String) {
                    Object arg = args[2 * i + 1];
                    if (arg != null && arg instanceof List) {
                        intent.putList((String) args[2 * i], (List<?>)arg);
                    } else {
                        intent.put((String) args[2 * i], arg);
                    }
                }
            }
        }
        startActivity(intent);
    }

    @Override
    public void startActivityForResult(Class<? extends Activity> clazz,
                                       int request) {
        startActivityForResult(new Intent(getAfActivity(), clazz), request);
    }

    public void startActivityForResult(Class<? extends Activity> clazz,
                                       int request, Object... args) {
        AfIntent intent = new AfIntent(getAfActivity(), clazz);
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length / 2; i++) {
                if (args[2 * i] instanceof String) {
                    Object arg = args[2 * i + 1];
                    if (arg != null && arg instanceof List) {
                        intent.putList((String) args[2 * i], (List<?>)arg);
                    } else {
                        intent.put((String) args[2 * i], arg);
                    }
                }
            }
        }
        startActivityForResult(intent, request);
    }

    /**
     * (non-Javadoc)
     *
     * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int, android.content.Intent)
     * final 重写 onActivityResult 使用 try-catch 调用
     * onActivityResult(AfIntent intent, int requestcode,int resultcode)
     * @see AfFragment#onActivityResult(AfIntent intent, int requestcode, int resultcode)
     * {@link AfFragment#onActivityResult(AfIntent intent, int requestcode, int resultcode)}
     */
    @Override
    public final void onActivityResult(int requestcode, int resultcode, Intent data) {
        try {
            onActivityResult(new AfIntent(data), requestcode, resultcode);
        } catch (Throwable e) {
            if (!(e instanceof AfToastException)) {
                AfExceptionHandler.handle(e, TAG("onActivityResult"));
            }
            makeToastLong("反馈信息读取错误！", e);
        }
    }

    /**
     * 安全 onActivityResult(AfIntent intent, int requestcode,int resultcode)
     * 在onActivityResult(int requestcode, int resultCode, Intent data) 中调用
     * 并使用 try-catch 提高安全性，子类请重写这个方法
     *
     * @see AfFragment#onActivityResult(int, int, android.content.Intent)
     * {@link AfFragment#onActivityResult(int, int, android.content.Intent)}
     */
    protected void onActivityResult(AfIntent intent, int requestcode, int resultcode) {
        super.onActivityResult(requestcode, resultcode, intent);
    }

    /**
     * 自定义 View onCreate(Bundle)
     */
    protected void onCreated(AfView rootView, AfBundle bundle) throws Exception {

    }

    /**
     * 自定义 View onCreateView(LayoutInflater, ViewGroup)
     */
    protected View onCreateView(LayoutInflater inflater, ViewGroup container) {
        BindLayout layout = AfReflecter.getAnnotation(this.getClass(), AfFragment.class, BindLayout.class);
        if (layout != null) {
            return inflater.inflate(layout.value(), container, false);
        }
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            Injecter.doInjectQueryChanged(this);
            this.onQueryChanged();
        } catch (Throwable ex) {
            AfExceptionHandler.handle(ex, "AfFragment.onResume");
        }
    }

    @Override
    public final void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    /**
     * 锁住 上级的 View onCreateView(LayoutInflater, ViewGroup, Bundle)
     */
    @Override
    public final View onCreateView(LayoutInflater inflater,
                                   ViewGroup container, Bundle bundle) {
        mRootView = onCreateView(inflater, container);
        if (mRootView == null) {
            mRootView = super.onCreateView(inflater, container, bundle);
        }
        try {
            ViewBinder.doBind(this);
            Injecter.doInject(this,getAfActivity());
            AfSoftInputer inputer = new AfSoftInputer(getAfActivity());
            inputer.setBindListener(mRootView, this);
            onCreated(new AfView(mRootView), new AfBundle(getArguments()));
        } catch (Throwable e) {
            if (!(e instanceof AfToastException)) {
                AfExceptionHandler.handle(e, TAG("onCreateView"));
            }
            makeToastLong("页面初始化异常！", e);
        }
        return mRootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//		mRootView = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mIsRecycled = true;
        if (mWorker != null) {
            mWorker.quit();
        }
    }

    /**
     * 第一次切换到本页面
     */
    protected void onFirstSwitchOver() {
    }

    /**
     * 每次切换到本页面
     * @param count 切换序号
     */
    protected void onSwitchOver(int count) {
    }

    /**
     * 离开本页面
     */
    protected void onSwitchLeave() {
    }

    /**
     * 查询系统数据变动
     */
    @Override
    public void onQueryChanged() {
    }


    @Override
    public boolean getSoftInputStatus() {
        return new AfSoftInputer(getAfActivity()).getSoftInputStatus();
    }

    @Override
    public boolean getSoftInputStatus(View view) {
        return new AfSoftInputer(getAfActivity()).getSoftInputStatus(view);
    }

    @Override
    public void setSoftInputEnable(EditText editview, boolean enable) {
        new AfSoftInputer(getAfActivity()).setSoftInputEnable(editview, enable);
    }

    @Override
    public Context getContext() {
        Activity activity = getAfActivity();
        if (activity == null) {
            return AfApplication.getAppContext();
        }
        return activity;
    }

    @Override
    public void makeToastLong(String tip) {
        Toast.makeText(getContext(), tip, Toast.LENGTH_LONG).show();
    }

    @Override
    public void makeToastShort(String tip) {
        Toast.makeText(getContext(), tip, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void makeToastLong(int resid) {
        Toast.makeText(getContext(), resid, Toast.LENGTH_LONG).show();
    }

    @Override
    public void makeToastShort(int resid) {
        Toast.makeText(getContext(), resid, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void makeToastLong(String tip, Throwable e) {
        tip = AfExceptionHandler.tip(e, tip);
        Toast.makeText(getContext(), tip, Toast.LENGTH_LONG).show();
    }


    @Override
    public final View findViewById(int id) {
        if (mRootView != null) {
            return mRootView.findViewById(id);
        }
        return null;
    }

    @Override
    public <T extends View> T findViewById(int id, Class<T> clazz) {
        View view = findViewById(id);
        if (clazz.isInstance(view)) {
            return clazz.cast(view);
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends View> T findViewByID(int id) {
        try {
            return (T) findViewById(id);
        } catch (Throwable e) {
            AfExceptionHandler.handle(e, TAG("findViewByID"));
        }
        return null;
    }

    @Override
    public void onSoftInputHiden() {
    }

    @Override
    public void onSoftInputShown() {
    }

    /**
     * 创建对话框构建器
     */
    @NonNull
    protected AfDialogBuilder newDialogBuilder() {
        return new AfDialogBuilder(getContext());
    }
    /**
     * 显示 进度对话框
     *
     * @param message 消息
     */
    public Dialog showProgressDialog(String message) {
        return showProgressDialog(message, false, 20);
    }

    /**
     * 显示 进度对话框
     *
     * @param message 消息
     * @param cancel  是否可取消
     */
    public Dialog showProgressDialog(String message, boolean cancel) {
        return showProgressDialog(message, cancel, 20);
    }

    /**
     * 显示 进度对话框
     *
     * @param message  消息
     * @param cancel   是否可取消
     * @param textsize 字体大小
     */
    public Dialog showProgressDialog(String message, boolean cancel,
                                     int textsize) {
        return mDialogBuilder.showProgressDialog(message, cancel, textsize);
    }

    /**
     * 显示 进度对话框
     *
     * @param message  消息
     * @param listener 取消监听器
     */
    public Dialog showProgressDialog(String message, OnCancelListener listener) {
        return mDialogBuilder.showProgressDialog(message, listener);
    }

    /**
     * 显示 进度对话框
     *
     * @param message  消息
     * @param listener 取消监听器
     * @param textsize 字体大小
     */
    public Dialog showProgressDialog(String message, OnCancelListener listener, int textsize) {
        return mDialogBuilder.showProgressDialog(message, listener, textsize);
    }

    /**
     * 隐藏 进度对话框
     */
    public void hideProgressDialog() {
        mDialogBuilder.hideProgressDialog();
    }

    /**
     * 动态改变对话框的文本
     * @param text 文本
     */
    @SuppressWarnings("unused")
    protected void setProgressDialogText(String text) {
        mDialogBuilder.setProgressDialogText(text);
    }

    /**
     * 显示对话框 并添加默认按钮 "我知道了"
     *
     * @param title   显示标题
     * @param message 显示内容
     */
    public Dialog doShowDialog(String title, String message) {
        return doShowDialog(0, title, message, "", null, "我知道了", null);
    }

    /**
     * 显示对话框 并添加默认按钮 "我知道了"
     *
     * @param title     显示标题
     * @param message   显示内容
     * @param lpositive 点击  "我知道了" 响应事件
     */
    public Dialog doShowDialog(String title, String message, OnClickListener lpositive) {
        return doShowDialog(0, title, message, "", null, "我知道了", lpositive);
    }

    /**
     * 显示对话框
     *
     * @param title     显示标题
     * @param message   显示内容
     * @param positive  确认 按钮显示信息
     * @param lpositive 点击  确认 按钮 响应事件
     */
    public Dialog doShowDialog(String title, String message, String positive, OnClickListener lpositive) {
        return doShowDialog(0, title, message, "", null, positive, lpositive);
    }

    /**
     * 显示对话框
     *
     * @param title     显示标题
     * @param message   显示内容
     * @param positive  确认 按钮显示信息
     * @param lpositive 点击  确认 按钮 响应事件
     * @param negative  按钮显示信息
     * @param lnegative 点击  拒绝 按钮 响应事件
     */
    public Dialog doShowDialog(String title, String message, String negative, OnClickListener lnegative, String positive, OnClickListener lpositive) {
        return doShowDialog(0, title, message, negative, lnegative, positive, lpositive);
    }

    /**
     * 显示对话框
     *
     * @param title     显示标题
     * @param message   显示内容
     * @param positive  确认 按钮显示信息
     * @param lpositive 点击  确认 按钮 响应事件
     * @param neutral   详细 按钮显示信息
     * @param lneutral  点击  详细 按钮 响应事件
     * @param negative  按钮显示信息
     * @param lnegative 点击  拒绝 按钮 响应事件
     */
    @Override
    public Dialog doShowDialog(String title, String message, String negative, OnClickListener lnegative, String neutral, OnClickListener lneutral, String positive, OnClickListener lpositive) {
        return doShowDialog(0, title, message, negative, lnegative, neutral, lneutral, positive, lpositive);
    }

    /**
     * 显示对话框
     *
     * @param iconres   对话框图标
     * @param title     显示标题
     * @param message   显示内容
     * @param positive  确认 按钮显示信息
     * @param lpositive 点击  确认 按钮 响应事件
     * @param negative  按钮显示信息
     * @param lnegative 点击  拒绝 按钮 响应事件
     */
    public Dialog doShowDialog(int iconres, String title, String message, String negative, OnClickListener lnegative, String positive, OnClickListener lpositive) {
        return doShowDialog(iconres, title, message, negative, lnegative, "", null, positive, lpositive);
    }

    /**
     * 显示对话框
     *
     * @param iconres   对话框图标
     * @param title     显示标题
     * @param message   显示内容
     * @param positive  确认 按钮显示信息
     * @param lpositive 点击  确认 按钮 响应事件
     * @param neutral   详细 按钮显示信息
     * @param lneutral  点击  详细 按钮 响应事件
     * @param negative  按钮显示信息
     * @param lnegative 点击  拒绝 按钮 响应事件
     */
    public Dialog doShowDialog(int iconres, String title, String message, String negative, OnClickListener lnegative, String neutral, OnClickListener lneutral, String positive, OnClickListener lpositive) {
        return doShowDialog(-1, iconres, title, message, negative, lnegative, neutral, lneutral, positive, lpositive);
    }

    /**
     * 显示视图对话框
     *
     * @param theme     主题
     * @param iconres   对话框图标
     * @param title     显示标题
     * @param message   显示内容
     * @param positive  确认 按钮显示信息
     * @param lpositive 点击  确认 按钮 响应事件
     * @param neutral   详细 按钮显示信息
     * @param lneutral  点击  详细 按钮 响应事件
     * @param negative  按钮显示信息
     * @param lnegative 点击  拒绝 按钮 响应事件
     */
    @Override
    @SuppressLint("NewApi")
    public Dialog doShowDialog(int theme, int iconres,
                                    String title, String message, String negative, OnClickListener lnegative, String neutral, OnClickListener lneutral, String positive, OnClickListener lpositive) {
        return mDialogBuilder.doShowDialog(theme, iconres, title, message, negative, lnegative, neutral, lneutral, positive, lpositive);
    }

    /**
     * 显示视图对话框
     *
     * @param title     显示标题
     * @param view      显示内容
     * @param positive  确认 按钮显示信息
     * @param lpositive 点击  确认 按钮 响应事件
     */
    @Override
    public Dialog doShowViewDialog(String title, View view, String positive,
                                        OnClickListener lpositive) {
        return doShowViewDialog(title, view, "", null, positive, lpositive);
    }

    /**
     * 显示视图对话框
     *
     * @param title     显示标题
     * @param view      显示内容
     * @param positive  确认 按钮显示信息
     * @param lpositive 点击  确认 按钮 响应事件
     * @param negative  按钮显示信息
     * @param lnegative 点击  拒绝 按钮 响应事件
     */
    @Override
    public Dialog doShowViewDialog(String title, View view, String negative, OnClickListener lnegative, String positive, OnClickListener lpositive) {
        return doShowViewDialog(0, title, view, negative, lnegative, positive, lpositive);
    }

    /**
     * 显示视图对话框
     *
     * @param title     显示标题
     * @param view      显示内容
     * @param positive  确认 按钮显示信息
     * @param lpositive 点击  确认 按钮 响应事件
     * @param neutral   详细 按钮显示信息
     * @param lneutral  点击  详细 按钮 响应事件
     * @param negative  按钮显示信息
     * @param lnegative 点击  拒绝 按钮 响应事件
     */
    @Override
    public Dialog doShowViewDialog(String title, View view, String negative, OnClickListener lnegative, String neutral, OnClickListener lneutral, String positive, OnClickListener lpositive) {
        return doShowViewDialog(0, title, view, negative, lnegative, neutral, lneutral, positive, lpositive);
    }

    /**
     * 显示视图对话框
     *
     * @param iconres   对话框图标
     * @param title     显示标题
     * @param view      显示内容
     * @param positive  确认 按钮显示信息
     * @param lpositive 点击  确认 按钮 响应事件
     * @param negative  按钮显示信息
     * @param lnegative 点击  拒绝 按钮 响应事件
     */
    @Override
    public Dialog doShowViewDialog(int iconres, String title, View view, String negative, OnClickListener lnegative, String positive, OnClickListener lpositive) {
        return doShowViewDialog(0, title, view, negative, lnegative, "", null, positive, lpositive);
    }

    /**
     * 显示视图对话框
     *
     * @param iconres   对话框图标
     * @param title     显示标题
     * @param view      显示内容
     * @param positive  确认 按钮显示信息
     * @param lpositive 点击  确认 按钮 响应事件
     * @param neutral   详细 按钮显示信息
     * @param lneutral  点击  详细 按钮 响应事件
     * @param negative  按钮显示信息
     * @param lnegative 点击  拒绝 按钮 响应事件
     */
    @Override
    public Dialog doShowViewDialog(int iconres, String title, View view, String negative, OnClickListener lnegative, String neutral, OnClickListener lneutral, String positive, OnClickListener lpositive) {
        return doShowViewDialog(-1, iconres, title, view, negative, lnegative, neutral, lneutral, positive, lpositive);
    }

    /**
     * 显示视图对话框
     *
     * @param theme     主题
     * @param iconres   对话框图标
     * @param title     显示标题
     * @param view      显示内容
     * @param positive  确认 按钮显示信息
     * @param lpositive 点击  确认 按钮 响应事件
     * @param neutral   详细 按钮显示信息
     * @param lneutral  点击  详细 按钮 响应事件
     * @param negative  按钮显示信息
     * @param lnegative 点击  拒绝 按钮 响应事件
     */
    @SuppressLint("NewApi")
    @Override
    public Dialog doShowViewDialog(int theme,
                                        int iconres, String title, View view, String negative, OnClickListener lnegative, String neutral, OnClickListener lneutral, String positive, OnClickListener lpositive) {
        return mDialogBuilder.doShowViewDialog(theme, iconres, title, view, negative, lnegative, neutral, lneutral, positive, lpositive);
    }

    /**
     * 显示一个单选对话框 （设置可取消）
     *
     * @param title    对话框标题
     * @param items    选择菜单项
     * @param listener 选择监听器
     * @param cancel   取消选择监听器
     */
    public Dialog doSelectItem(String title, String[] items, OnClickListener listener,
                                    boolean cancel) {
        return mDialogBuilder.doSelectItem(title, items, listener, cancel);
    }

    /**
     * 显示一个单选对话框
     *
     * @param title    对话框标题
     * @param items    选择菜单项
     * @param listener 选择监听器
     * @param oncancel 取消选择监听器
     */
    public Dialog doSelectItem(String title, String[] items, OnClickListener listener,
                                    final OnClickListener oncancel) {
        return mDialogBuilder.doSelectItem(title, items, listener, oncancel);
    }

    /**
     * 显示一个单选对话框 （默认可取消）
     *
     * @param title    对话框标题
     * @param items    选择菜单项
     * @param listener 选择监听器
     */
    public Dialog doSelectItem(String title, String[] items, OnClickListener listener) {
        return doSelectItem(title, items, listener, null);
    }

    /**
     * 弹出一个文本输入框
     *
     * @param title    标题
     * @param listener 监听器
     */
    public void doInputText(String title, InputTextListener listener) {
        doInputText(title, "", InputType.TYPE_CLASS_TEXT, listener);
    }

    /**
     * 弹出一个文本输入框
     *
     * @param title    标题
     * @param type     android.text.InputType
     * @param listener 监听器
     */
    public Dialog doInputText(String title, int type, InputTextListener listener) {
        return doInputText(title, "", type, listener);
    }
    /**
     * 弹出一个文本输入框
     *
     * @param title    标题
     * @param defaul   默认值
     * @param listener 监听器
     */
    public Dialog doInputText(String title, String defaul, InputTextListener listener) {
        return mDialogBuilder.doInputText(title, defaul, InputType.TYPE_CLASS_TEXT, listener);
    }

    /**
     * 弹出一个文本输入框
     *
     * @param title    标题
     * @param defaul   默认值
     * @param type     android.text.InputType
     * @param listener 监听器
     */
    public Dialog doInputText(String title, String defaul, int type, InputTextListener listener) {
        return mDialogBuilder.doInputText(title, defaul, type, listener);
    }

    /**
     * 显示对话框 并添加默认按钮 "我知道了"
     *
     * @param key     不再显示KEY
     * @param title   显示标题
     * @param message 显示内容
     */
    public Dialog doShowDialog(String key, String title, String message) {
        return doShowDialog(key, 0, 0, title, message, "", null, "我知道了", null);
    }

    /**
     * 显示对话框
     *
     * @param key       不再显示KEY
     * @param title     显示标题
     * @param message   显示内容
     * @param positive  确认 按钮显示信息
     * @param lpositive 点击  确认 按钮 响应事件
     */
    public Dialog doShowDialog(String key, String title, String message, String positive, OnClickListener lpositive) {
        return doShowDialog(key, 0, 0, title, message, "", null, positive, lpositive);
    }

    /**
     * 显示对话框
     *
     * @param key       不再显示KEY
     * @param defclick  不再显示之后默认执行index
     * @param title     显示标题
     * @param message   显示内容
     * @param positive  确认 按钮显示信息
     * @param lpositive 点击  确认 按钮 响应事件
     * @param negative  按钮显示信息
     * @param lnegative 点击  拒绝 按钮 响应事件
     */
    public Dialog doShowDialog(String key, int defclick,
                                    String title, String message, String negative, OnClickListener lnegative, String positive, OnClickListener lpositive) {
        return doShowDialog(key, defclick, 0, title, message, negative, lnegative, positive, lpositive);
    }

    /**
     * 显示对话框
     *
     * @param key       不再显示KEY
     * @param defclick  不再显示之后默认执行index
     * @param title     显示标题
     * @param message   显示内容
     * @param positive  确认 按钮显示信息
     * @param lpositive 点击  确认 按钮 响应事件
     * @param neutral   详细 按钮显示信息
     * @param lneutral  点击  详细 按钮 响应事件
     * @param negative  按钮显示信息
     * @param lnegative 点击  拒绝 按钮 响应事件
     */
    public Dialog doShowDialog(String key, int defclick,
                                    String title, String message, String negative, OnClickListener lnegative, String neutral, OnClickListener lneutral, String positive, OnClickListener lpositive) {
        return doShowDialog(key, defclick, 0, title, message, negative, lnegative, neutral, lneutral, positive, lpositive);
    }

    /**
     * 显示对话框
     *
     * @param key       不再显示KEY
     * @param defclick  不再显示之后默认执行index
     * @param iconres   对话框图标
     * @param title     显示标题
     * @param message   显示内容
     * @param positive  确认 按钮显示信息
     * @param lpositive 点击  确认 按钮 响应事件
     * @param negative  按钮显示信息
     * @param lnegative 点击  拒绝 按钮 响应事件
     */
    public Dialog doShowDialog(String key, int defclick,
                                    int iconres, String title, String message, String negative, OnClickListener lnegative, String positive, OnClickListener lpositive) {
        if (defclick == 1) {
            defclick = 2;
        }
        return doShowDialog(key, defclick, iconres, title, message, negative, lnegative, "", null, positive, lpositive);
    }

    /**
     * 显示对话框
     *
     * @param key       不再显示KEY
     * @param defclick  不再显示之后默认执行index
     * @param iconres   对话框图标
     * @param title     显示标题
     * @param message   显示内容
     * @param positive  确认 按钮显示信息
     * @param lpositive 点击  确认 按钮 响应事件
     * @param neutral   详细 按钮显示信息
     * @param lneutral  点击  详细 按钮 响应事件
     * @param negative  按钮显示信息
     * @param lnegative 点击  拒绝 按钮 响应事件
     */
    public Dialog doShowDialog(String key, int defclick,
                                    int iconres, String title, String message, String negative, OnClickListener lnegative, String neutral, OnClickListener lneutral, String positive, OnClickListener lpositive) {
        return doShowDialog(key, defclick, -1, iconres, title, message, negative, lnegative, neutral, lneutral, positive, lpositive);
    }


    /**
     * 显示视图对话框
     *
     * @param key       不再显示KEY
     * @param defclick  不再显示之后默认执行index
     * @param theme     主题
     * @param iconres   对话框图标
     * @param title     显示标题
     * @param message   显示内容
     * @param positive  确认 按钮显示信息
     * @param lpositive 点击  确认 按钮 响应事件
     * @param neutral   详细 按钮显示信息
     * @param lneutral  点击  详细 按钮 响应事件
     * @param negative  按钮显示信息
     * @param lnegative 点击  拒绝 按钮 响应事件
     */
    public Dialog doShowDialog(String key, int defclick,
                                    int theme, int iconres,
                                    String title, String message, String negative, OnClickListener lnegative, String neutral, OnClickListener lneutral, String positive, OnClickListener lpositive) {
        return mDialogBuilder.doShowDialog(key, defclick, theme, iconres, title, message, negative, lnegative, neutral, lneutral, positive, lpositive);
    }

    /**
     * 选择日期时间
     * @param listener 监听器
     */
    public Dialog doSelectDateTime(AfDialogBuilder.OnDateTimeSetListener listener) {
        return doSelectDateTime("", new Date(), listener);
    }

    /**
     * 选择日期时间
     * @param title 标题
     * @param listener 监听器
     */
    public Dialog doSelectDateTime(String title, AfDialogBuilder.OnDateTimeSetListener listener) {
        return doSelectDateTime(title, new Date(), listener);
    }

    /**
     * 选择日期时间
     * @param value 默认时间
     * @param listener 监听器
     */
    public Dialog doSelectDateTime(Date value, AfDialogBuilder.OnDateTimeSetListener listener) {
        return doSelectDateTime("", value, listener);
    }

    /**
     * 选择日期时间
     * @param title 标题
     * @param value 默认时间
     * @param listener 监听器
     */
    public Dialog doSelectDateTime(final String title, final Date value, final AfDialogBuilder.OnDateTimeSetListener listener) {
        return mDialogBuilder.doSelectDateTime(title, value, listener);
    }

    /**
     * 选择时间
     * @param listener 监听器
     */
    public Dialog doSelectTime(AfDialogBuilder.OnTimeSetListener listener) {
        return doSelectTime("", new Date(), listener);
    }

    /**
     * 选择时间
     * @param title 标题
     * @param listener 监听器
     */
    public Dialog doSelectTime(String title, AfDialogBuilder.OnTimeSetListener listener) {
        return doSelectTime(title, new Date(), listener);
    }

    /**
     * 选择时间
     * @param value 默认时间
     * @param listener 监听器
     */
    public Dialog doSelectTime(Date value, AfDialogBuilder.OnTimeSetListener listener) {
        return doSelectTime("", value, listener);
    }

    /**
     * 选择时间
     * @param title 标题
     * @param value 默认时间
     * @param listener 监听器
     */
    public Dialog doSelectTime(String title, Date value, final AfDialogBuilder.OnTimeSetListener listener) {
        return mDialogBuilder.doSelectTime(title, value, listener);
    }

    /**
     * 选择日期
     * @param listener 监听器
     */
    public Dialog doSelectDate(AfDialogBuilder.OnDateSetListener listener) {
        return doSelectDate("", new Date(), listener);
    }

    /**
     * 选择日期
     * @param title 标题
     * @param listener 监听器
     */
    public Dialog doSelectDate(String title, AfDialogBuilder.OnDateSetListener listener) {
        return doSelectDate(title, new Date(), listener);
    }

    /**
     * 选择日期
     * @param value 默认时间
     * @param listener 监听器
     */
    public Dialog doSelectDate(Date value, AfDialogBuilder.OnDateSetListener listener) {
        return doSelectDate("", value, listener);
    }

    /**
     * 选择日期
     * @param title 标题
     * @param value 默认时间
     * @param listener 监听器
     */
    public Dialog doSelectDate(String title, Date value, AfDialogBuilder.OnDateSetListener listener) {
        return mDialogBuilder.doSelectDate(title,value, listener);
    }

    /**
     * 改变进度对话框的文本
     * @param dialog 对话框
     * @param text 文本
     */
    @SuppressWarnings("unused")
    protected void setProgressDialogText(ProgressDialog dialog, String text) {
        mDialogBuilder.setProgressDialogText(dialog, text);
    }

    /**
     * 按下返回按键
     *
     * @return 返回 true 表示已经处理 否则 Activity 会处理
     */
    public boolean onBackPressed() {
        return false;
    }

    /**
     * 按键按下事件
     *
     * @return 返回 true 表示已经处理 否则 Activity 会处理
     */
    @SuppressWarnings("UnusedParameters")
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    /**
     * 按键弹起事件
     *
     * @return 返回 true 表示已经处理 否则 Activity 会处理
     */
    @SuppressWarnings("UnusedParameters")
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }

    /**
     * 按键重复事件
     *
     * @return 返回 true 表示已经处理 否则 Activity 会处理
     */
    @SuppressWarnings("UnusedParameters")
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        return false;
    }

    /**
     * 按键onKeyShortcut事件
     *
     * @return 返回 true 表示已经处理 否则 Activity 会处理
     */
    @SuppressWarnings("UnusedParameters")
    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        return false;
    }

    /**
     * 按键onKeyLongPress事件
     *
     * @return 返回 true 表示已经处理 否则 Activity 会处理
     */
    @SuppressWarnings("UnusedParameters")
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return false;
    }

}
