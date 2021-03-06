package com.rae.cnblogs.home.launcher;

import android.os.CountDownTimer;
import android.text.TextUtils;

import com.rae.cnblogs.basic.AppMobclickAgent;
import com.rae.cnblogs.basic.BasicPresenter;
import com.rae.cnblogs.basic.rx.AndroidObservable;
import com.rae.cnblogs.sdk.ApiDefaultObserver;
import com.rae.cnblogs.sdk.CnblogsApiFactory;
import com.rae.cnblogs.sdk.api.IRaeServerApi;
import com.rae.cnblogs.sdk.bean.AdvertBean;
import com.rae.cnblogs.sdk.db.DbAdvert;
import com.rae.cnblogs.sdk.db.DbFactory;
import com.rae.cnblogs.sdk.utils.ApiUtils;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DefaultObserver;

/**
 * 启动页
 */
public class LauncherPresenterImpl extends BasicPresenter<LauncherContract.View> implements LauncherContract.Presenter {

    private IRaeServerApi mRaeServerApi;
    private DbAdvert mDbAdvert;
    private AdvertBean mAdvertBean;
    private CountDownTimer mCountDownTimer;


    public LauncherPresenterImpl(LauncherContract.View view) {
        super(view);
        mRaeServerApi = CnblogsApiFactory.getInstance(getContext()).getRaeServerApi();
        mDbAdvert = DbFactory.getInstance().getAdvert();

        // 倒计时
        mCountDownTimer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                getView().onRouteToHome();
            }
        };
    }

    /**
     * 加载本地数据
     */
    private void loadLocalData() {
        AndroidObservable
                .create(Observable.just(mDbAdvert))
                .with(this)
                .map(new Function<DbAdvert, AdvertBean>() {
                    @Override
                    public AdvertBean apply(DbAdvert dbAdvert) {
                        return dbAdvert.getLauncherAd();
                    }
                })
                .subscribe(new DefaultObserver<AdvertBean>() {
                    @Override
                    public void onNext(AdvertBean advertBean) {
                        mAdvertBean = advertBean;
                        AppMobclickAgent.onLaunchAdExposureEvent(getContext(), advertBean.getAdId(), advertBean.getAdName());
                        // 过期时间判断
                        if (System.currentTimeMillis() < ApiUtils.parseDate(advertBean.getMAdEndDate()).getTime()) {
                            getView().onLoadImage(advertBean.getAdName(), advertBean.getImageUrl());
                        } else {
                            getView().onEmptyImage();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        getView().onEmptyImage();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * 加载启动页数据
     */
    private void loadLauncherData() {

        // 加载首页图
        AndroidObservable
                .create(mRaeServerApi.getLauncherAd())
                .with(this)
                .subscribe(new ApiDefaultObserver<AdvertBean>() {
                    @Override
                    protected void onError(String message) {
                        getView().onEmptyImage();
                    }

                    @Override
                    protected void accept(AdvertBean data) {

                        // 如果图片发生改变了，重新开始
                        AdvertBean local = mDbAdvert.getLauncherAd();
                        if (local != null && !TextUtils.equals(local.getImageUrl(), data.getImageUrl())) {
                            mCountDownTimer.cancel();
                            mCountDownTimer.start();
                            getView().onImageChanged();
                        }

                        // 保存到数据，等待下一次加载
                        mDbAdvert.save(data);
                        mAdvertBean = data;

                        AppMobclickAgent.onLaunchAdExposureEvent(getContext(), data.getAdId(), data.getAdName());

                        // 加载图片
                        // 过期时间判断
                        if (!TextUtils.isEmpty(data.getImageUrl()) && System.currentTimeMillis() < ApiUtils.parseDate(data.getMAdEndDate()).getTime()) {
                            getView().onLoadImage(data.getAdName(), data.getImageUrl());
                        } else {
                            getView().onEmptyImage();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        mCountDownTimer.start();
        // 先从本地加载
        loadLocalData();
        // 从网络获取
        loadLauncherData();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCountDownTimer.cancel();
    }

    @Override
    public void onAdClick() {
        if (mAdvertBean == null || TextUtils.isEmpty(mAdvertBean.getAdUrl())) return;
        // 统计
        AppMobclickAgent.onLaunchAdClickEvent(getContext(), mAdvertBean.getAdId(), mAdvertBean.getAdName());
        mCountDownTimer.cancel();
        // 再跳转网页
        getView().onRouteToWeb(mAdvertBean.getAdUrl());
    }


}
