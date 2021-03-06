package com.fastaccess.ui.modules.feeds;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.MotionEvent;
import android.view.View;

import com.fastaccess.R;
import com.fastaccess.data.dao.SimpleUrlsModel;
import com.fastaccess.data.dao.model.Event;
import com.fastaccess.helper.Logger;
import com.fastaccess.helper.PrefGetter;
import com.fastaccess.provider.rest.loadmore.OnLoadMore;
import com.fastaccess.provider.scheme.SchemeParser;
import com.fastaccess.ui.adapter.FeedsAdapter;
import com.fastaccess.ui.base.BaseFragment;
import com.fastaccess.ui.widgets.StateLayout;
import com.fastaccess.ui.widgets.dialog.ListDialogView;
import com.fastaccess.ui.widgets.recyclerview.DynamicRecyclerView;

import java.util.ArrayList;

import butterknife.BindView;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

/**
 * Created by Kosh on 11 Nov 2016, 12:36 PM
 */

public class FeedsView extends BaseFragment<FeedsMvp.View, FeedsPresenter> implements FeedsMvp.View {

    public static final String TAG = FeedsView.class.getSimpleName();

    @BindView(R.id.recycler) DynamicRecyclerView recycler;
    @BindView(R.id.refresh) SwipeRefreshLayout refresh;
    @BindView(R.id.stateLayout) StateLayout stateLayout;
    private FeedsAdapter adapter;
    private OnLoadMore onLoadMore;

    public static FeedsView newInstance() {
        return new FeedsView();
    }

    @Override protected int fragmentLayout() {
        return R.layout.small_grid_refresh_list;
    }

    @Override protected void onFragmentCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        stateLayout.setEmptyText(R.string.no_feeds);
        stateLayout.setOnReloadListener(this);
        refresh.setOnRefreshListener(this);
        recycler.setEmptyView(stateLayout, refresh);
        adapter = new FeedsAdapter(getPresenter().getEvents());
        adapter.setGuideListener(this);
        adapter.setListener(getPresenter());
        getLoadMore().setCurrent_page(getPresenter().getCurrentPage(), getPresenter().getPreviousTotal());
        recycler.setAdapter(adapter);
        recycler.addOnScrollListener(getLoadMore());
        if (getPresenter().getEvents().isEmpty() && !getPresenter().isApiCalled()) {
            onRefresh();
        }
    }

    @Override public void onRefresh() {
        getPresenter().onCallApi(1);
    }

    @Override public void onNotifyAdapter() {
        hideProgress();
        adapter.notifyDataSetChanged();
    }

    @Override public void showProgress(@StringRes int resId) {

        stateLayout.showProgress();
    }

    @Override public void hideProgress() {
        refresh.setRefreshing(false);
        stateLayout.hideProgress();
    }

    @Override public void showErrorMessage(@NonNull String message) {
        showReload();
        super.showErrorMessage(message);
    }

    @Override public void showMessage(int titleRes, int msgRes) {
        showReload();
        super.showMessage(titleRes, msgRes);
    }

    private void showReload() {
        hideProgress();
        stateLayout.showReload(adapter.getItemCount());
    }

    @Override public void onOpenRepoChooser(@NonNull ArrayList<SimpleUrlsModel> models) {
        Logger.e(models);
        ListDialogView<SimpleUrlsModel> dialogView = new ListDialogView<>();
        dialogView.initArguments(getString(R.string.repo_chooser), models);
        dialogView.show(getChildFragmentManager(), "ListDialogView");
    }

    @NonNull @Override public FeedsPresenter providePresenter() {
        return new FeedsPresenter();
    }

    @SuppressWarnings("unchecked") @NonNull @Override public OnLoadMore getLoadMore() {
        if (onLoadMore == null) {
            onLoadMore = new OnLoadMore(getPresenter());
        }
        return onLoadMore;
    }

    @Override public void onDestroyView() {
        recycler.removeOnScrollListener(getLoadMore());
        super.onDestroyView();
    }

    @Override public void onClick(View view) {
        onRefresh();
    }

    @Override public void onItemSelected(SimpleUrlsModel item) {
        SchemeParser.launchUri(getContext(), Uri.parse(item.getItem()));
    }

    @Override public void onShowGuide(@NonNull View itemView, @NonNull Event model) {
        if (!PrefGetter.isUserIconGuideShowed()) {
            new MaterialTapTargetPrompt.Builder(getActivity())
                    .setTarget(itemView.findViewById(R.id.avatarLayout))
                    .setPrimaryText(R.string.users)
                    .setSecondaryText(R.string.avatar_click_hint)
                    .setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener() {
                        @Override public void onHidePrompt(MotionEvent event, boolean tappedTarget) {

                        }

                        @Override public void onHidePromptComplete() {
                            new MaterialTapTargetPrompt.Builder(getActivity())
                                    .setTarget(itemView)
                                    .setPrimaryText(R.string.fork)
                                    .setSecondaryText(R.string.feeds_fork_hint)
                                    .setCaptureTouchEventOutsidePrompt(true)
                                    .show();
                        }
                    })
                    .setCaptureTouchEventOutsidePrompt(true)
                    .show();
        }
    }
}
