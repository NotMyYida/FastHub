package com.fastaccess.ui.adapter.viewholder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.fastaccess.R;
import com.fastaccess.data.dao.model.Event;
import com.fastaccess.data.dao.types.EventsType;
import com.fastaccess.helper.ParseDateFormat;
import com.fastaccess.ui.widgets.AvatarLayout;
import com.fastaccess.ui.widgets.FontTextView;
import com.fastaccess.ui.widgets.SpannableBuilder;
import com.fastaccess.ui.widgets.recyclerview.BaseRecyclerAdapter;
import com.fastaccess.ui.widgets.recyclerview.BaseViewHolder;

import butterknife.BindString;
import butterknife.BindView;

/**
 * Created by Kosh on 11 Nov 2016, 2:08 PM
 */

public class FeedsViewHolder extends BaseViewHolder<Event> {

    @BindView(R.id.avatarLayout) AvatarLayout avatar;
    @BindView(R.id.title) FontTextView title;
    @BindView(R.id.date) FontTextView date;
    @BindString(R.string.to) String to;

    public FeedsViewHolder(@NonNull View itemView, @Nullable BaseRecyclerAdapter adapter) {
        super(itemView, adapter);
    }

    public static View getView(@NonNull ViewGroup viewGroup) {
        return getView(viewGroup, R.layout.feeds_row_item);
    }

    @Override public void bind(@NonNull Event eventsModel) {
        if (eventsModel.getActor() != null) {
            avatar.setUrl(eventsModel.getActor().getAvatarUrl(), eventsModel.getActor().getLogin());
        } else {
            avatar.setUrl(null, null);
        }
        SpannableBuilder spannableBuilder = SpannableBuilder.builder();
        spannableBuilder.append(eventsModel.getActor() != null ? eventsModel.getActor().getLogin() : "N/A").append(" ");
        if (eventsModel.getType() != null) {
            String action;
            if (eventsModel.getType() == EventsType.WatchEvent) {
                action = itemView.getResources().getString(eventsModel.getType().getType()).toLowerCase();
            } else {
                action = eventsModel.getPayload() != null ? eventsModel.getPayload().getAction() : "";
            }
            spannableBuilder.bold(action != null ? action.toLowerCase() : "")
                    .append(eventsModel.getPayload() != null && eventsModel.getPayload().getAction() != null ? " " : "");
            if (eventsModel.getType() != EventsType.WatchEvent) {
                if (eventsModel.getType() == EventsType.CreateEvent && eventsModel.getPayload().getRefType().equalsIgnoreCase("branch")) {
                    spannableBuilder
                            .bold(itemView.getResources().getString(eventsModel.getType().getType()).toLowerCase())
                            .append(" ")
                            .bold(eventsModel.getPayload().getRefType())
                            .append(" ")
                            .append(to).append(" ");
                } else {
                    spannableBuilder.bold(itemView.getResources().getString(eventsModel.getType().getType()).toLowerCase())
                            .append(" ");
                }
            }
        }
        spannableBuilder.append(eventsModel.getRepo() != null ? eventsModel.getRepo().getName() : "");
        title.setText(spannableBuilder);
        date.setText(ParseDateFormat.getTimeAgo(eventsModel.getCreatedAt()));
    }
}
