package com.lzx.lock.module.setting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lzx.lock.R;
import com.lzx.lock.activity.AboutMeActivity;
import com.lzx.lock.activity.lock.GestureCreateActivity;
import com.lzx.lock.base.BaseActivity;
import com.lzx.lock.base.Constants;
import com.lzx.lock.bean.LockAutoTime;
import com.lzx.lock.service.LockService;
import com.lzx.lock.utils.SpUtil;
import com.lzx.lock.utils.SystemBarHelper;
import com.lzx.lock.utils.ToastUtil;
import com.lzx.lock.widget.SelectLockTimeDialog;


/**
 * Created by xian on 2017/2/17.
 */

public class LockSettingActivity extends BaseActivity implements View.OnClickListener
        , DialogInterface.OnDismissListener {
    private TextView mBtnAbout, mLockTime, mBtnChangePwd, mIsShowPath, mLockTip;
    private CheckBox mLockSwitch;
    private RelativeLayout mLockWhen;
    private LockSettingReceiver mLockSettingReceiver;
    public static final String ON_ITEM_CLICK_ACTION = "on_item_click_action";
    private SelectLockTimeDialog dialog;
    private static final int REQUEST_CHANGE_PWD = 3;
    private RelativeLayout mTopLayout;

    @Override
    public int getLayoutId() {
        return R.layout.activity_setting;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        mBtnChangePwd = (TextView) findViewById(R.id.btn_change_pwd);
        mLockTime = (TextView) findViewById(R.id.lock_time);
        mBtnAbout = (TextView) findViewById(R.id.about_me);
        mLockSwitch = (CheckBox) findViewById(R.id.switch_compat);
        mLockWhen = (RelativeLayout) findViewById(R.id.lock_when);
        mIsShowPath = (TextView) findViewById(R.id.is_show_path);
        mLockTip = (TextView) findViewById(R.id.lock_tip);
        mTopLayout = (RelativeLayout) findViewById(R.id.top_layout);
        mTopLayout.setPadding(0, SystemBarHelper.getStatusBarHeight(this), 0, 0);
    }

    @Override
    protected void initData() {
        mLockSettingReceiver = new LockSettingReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ON_ITEM_CLICK_ACTION);
        registerReceiver(mLockSettingReceiver, filter);
        dialog = new SelectLockTimeDialog(this, "");
        dialog.setOnDismissListener(this);
        boolean isLockOpen = SpUtil.getInstance().getBoolean(Constants.LOCK_STATE);
        mLockSwitch.setChecked(isLockOpen);
    }

    @Override
    protected void initAction() {
        mBtnChangePwd.setOnClickListener(this);
        mBtnAbout.setOnClickListener(this);

        mLockWhen.setOnClickListener(this);
        mIsShowPath.setOnClickListener(this);
        mLockSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SpUtil.getInstance().putBoolean(Constants.LOCK_STATE, b);
                Intent intent = new Intent(LockSettingActivity.this, LockService.class);
                if (b) {
                    mLockTip.setText("已开启，加锁应用打开时需要密码");
                    startService(intent);
                } else {
                    mLockTip.setText("已关闭，加锁应用打开时不需要密码");
                    stopService(intent);
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_change_pwd:
                Intent intent = new Intent(LockSettingActivity.this, GestureCreateActivity.class);
                startActivityForResult(intent, REQUEST_CHANGE_PWD);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;
            case R.id.about_me:
                intent = new Intent(LockSettingActivity.this, AboutMeActivity.class);
                startActivity(intent);
                break;
            case R.id.lock_when:
                String title = SpUtil.getInstance().getString(Constants.LOCK_APART_TITLE, "");
                dialog.setTitle(title);
                dialog.show();
                break;
            case R.id.is_show_path:
                boolean ishideline = SpUtil.getInstance().getBoolean(Constants.LOCK_IS_HIDE_LINE, false);
                if (ishideline) {
                    SpUtil.getInstance().putBoolean(Constants.LOCK_IS_HIDE_LINE, false);
                    ToastUtil.showToast("路径已显示");
                } else {
                    SpUtil.getInstance().putBoolean(Constants.LOCK_IS_HIDE_LINE, true);
                    ToastUtil.showToast("路径已隐藏");
                }
                //  sendBroadcast(new Intent(UPDATE_LOCK_VIEW));
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CHANGE_PWD:
                    ToastUtil.showToast("密码重置成功");
                    break;
            }
        }
    }


    @Override
    public void onDismiss(DialogInterface dialogInterface) {

    }

    //
    private class LockSettingReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ON_ITEM_CLICK_ACTION)) {
                LockAutoTime info = intent.getParcelableExtra("info");
                mLockTime.setText(info.getTitle());
                SpUtil.getInstance().putString(Constants.LOCK_APART_TITLE, info.getTitle());
                SpUtil.getInstance().putLong(Constants.LOCK_APART_MILLISENCONS, info.getTime());
                SpUtil.getInstance().putBoolean(Constants.LOCK_AUTO_SCREEN_TIME, true);
                dialog.dismiss();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mLockSettingReceiver);
    }

}
