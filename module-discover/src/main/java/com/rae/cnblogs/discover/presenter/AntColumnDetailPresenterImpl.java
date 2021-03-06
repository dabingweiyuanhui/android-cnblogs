package com.rae.cnblogs.discover.presenter;

import android.support.annotation.Nullable;

import com.antcode.sdk.AntCodeSDK;
import com.antcode.sdk.IAntColumnApi;
import com.antcode.sdk.model.AntColumnInfo;
import com.antcode.sdk.model.AntEmptyInfo;
import com.rae.cnblogs.discover.AntCodeBasicPresenter;
import com.rae.cnblogs.discover.AntSdkDefaultObserver;
import com.rae.cnblogs.discover.SubscribeColumnMessage;

import org.greenrobot.eventbus.EventBus;

public class AntColumnDetailPresenterImpl extends AntCodeBasicPresenter<IAntColumnDetailContract.View> implements IAntColumnDetailContract.Presenter {

    private final IAntColumnApi mColumnApi;
    @Nullable
    private AntColumnInfo mColumnInfo;

    public AntColumnDetailPresenterImpl(IAntColumnDetailContract.View view) {
        super(view);
        AntCodeSDK antCodeSDK = AntCodeSDK.getInstance();
        mColumnApi = antCodeSDK.getColumnApi();
    }

    @Override
    protected void onStart() {
        final String columnId = getView().getColumnId();
        mColumnApi.getColumnDetail(columnId).with(this).subscribe(new AntSdkDefaultObserver<AntColumnInfo>() {
            @Override
            protected void onError(String message) {
                getView().onLoadDataError(message);
                getView().onColumnSubscribe(false);
            }

            @Override
            protected void accept(AntColumnInfo columnInfo) {
                mColumnInfo = columnInfo;
                getView().onColumnSubscribe(columnInfo.isSubscribe());
                getView().onLoadColumnDetail(columnInfo);
            }
        });


    }

    @Override
    public void subscribe() {
        if (mColumnInfo == null) {
            getView().onSubscribeError("数据尚未加载完毕，请稍后再试");
            return;
        }
        mColumnApi.subscribe(String.valueOf(mColumnInfo.getId()))
                .with(this)
                .subscribe(new AntSdkDefaultObserver<AntEmptyInfo>() {
                    @Override
                    protected void onError(String message) {
                        getView().onSubscribeError(message);
                        getView().onColumnSubscribe(false);
                    }

                    @Override
                    protected void accept(AntEmptyInfo antEmptyInfo) {
                        EventBus.getDefault().post(new SubscribeColumnMessage(getView().getColumnId()));
                        getView().onSubscribeSuccess();
                        getView().onColumnSubscribe(true);
                    }

                    @Override
                    protected void onLoginExpired() {
                        super.onLoginExpired();
                        getView().onLoginExpired();
                    }
                });
    }
}
