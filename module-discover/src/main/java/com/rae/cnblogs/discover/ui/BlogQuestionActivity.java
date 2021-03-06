package com.rae.cnblogs.discover.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.RaeTabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.rae.cnblogs.AppRoute;
import com.rae.cnblogs.activity.SwipeBackBasicActivity;
import com.rae.cnblogs.discover.R;
import com.rae.cnblogs.discover.R2;
import com.rae.cnblogs.discover.fragment.BlogQuestionFragment;
import com.rae.cnblogs.discover.presenter.IBlogQuestionContract;
import com.rae.cnblogs.widget.RaeScrollTopTabListener;
import com.rae.session.SessionManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * 博问列表
 */
@Route(path = AppRoute.PATH_DISCOVER_BLOG_QUESTION)
public class BlogQuestionActivity extends SwipeBackBasicActivity {

    @BindView(R2.id.view_pager)
    ViewPager mViewPager;

    @BindView(R2.id.tab)
    RaeTabLayout mRaeTabLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_question);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(new QuestionAdapter(getSupportFragmentManager()));
        mRaeTabLayout.setupWithViewPager(mViewPager);
        mRaeTabLayout.addOnTabSelectedListener(new RaeScrollTopTabListener(mViewPager, getSupportFragmentManager()));
        setTitle(" ");
    }

    class QuestionAdapter extends FragmentPagerAdapter {
        private final List<String> titles = new ArrayList<>();
        private final List<Integer> mTypes = new ArrayList<>();

        QuestionAdapter(FragmentManager fm) {
            super(fm);
            titles.add("待解决");
            mTypes.add(IBlogQuestionContract.TYPE_UNSOLVED);

            titles.add("高分题");
            mTypes.add(IBlogQuestionContract.TYPE_HIGH_SCORE);

            if (SessionManager.getDefault().isLogin()) {
                titles.add("我的");
                mTypes.add(IBlogQuestionContract.TYPE_MY);
            }
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }

        @Override
        public Fragment getItem(int i) {
            return BlogQuestionFragment.newInstance(mTypes.get(i));
        }

        @Override
        public int getCount() {
            return titles.size();
        }
    }
}
