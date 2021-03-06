package com.topanimestream.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.squareup.picasso.Picasso;
import com.topanimestream.R;
import com.topanimestream.enums.Languages;
import com.topanimestream.models.Anime;
import com.topanimestream.models.Episode;
import com.topanimestream.utilities.ImageUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

public class EpisodeListAdapter extends HeaderRecyclerViewAdapter {
    private final Context mContext;
    private ArrayList<EpisodeItem> mItems = new ArrayList<>();
    private EpisodeListAdapter.OnItemClickListener mItemClickListener;
    private Anime anime;
    public static final int TYPE_NORMAL = 0, TYPE_LOADING = 1, TYPE_UNAVAILABLE = 2;
    public EpisodeListAdapter(Context context, Anime anime) {
        this.mContext = context;
        this.anime = anime;
        addItems(anime.getEpisodes());
    }

    public interface OnItemClickListener {
        public void onItemClick(View v, Episode episode, int position);
    }

    public void setOnItemClickListener(EpisodeListAdapter.OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    public void addItems(ArrayList<Episode> items) {
        if (items != null) {
            for (Episode item : items) {
                mItems.add(new EpisodeItem(item));
            }
            notifyDataSetChanged();
        }
    }

    public void addLoading() {
        mItems.add(new EpisodeItem(true));
        notifyDataSetChanged();
    }

    public void removeLoading() {
        if(mItems.get(getBasicItemCount() - 1).isLoadingItem)
            mItems.remove(getBasicItemCount() - 1);
    }

    public boolean isLoading() {
        if (getBasicItemCount() <= 0) return false;
        return getItemViewType(getBasicItemCount() - 1) == TYPE_LOADING;
    }

    @Override
    public boolean useHeader() {
        return true;
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_anime_details, parent, false);
        return new EpisodeListAdapter.HeaderViewHolder(v);
    }

    @Override
    public void onBindHeaderView(RecyclerView.ViewHolder viewHolder, int position) {
        final HeaderViewHolder headerViewHolder = (HeaderViewHolder) viewHolder;
        headerViewHolder.txtSysnopsis.setText(anime.getSynopsis());

        if(anime.isAvailable())
            headerViewHolder.txtAnimeUnavailable.setVisibility(View.GONE);
        else
            headerViewHolder.txtAnimeUnavailable.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean useFooter() {
        return false;
    }

    @Override
    public RecyclerView.ViewHolder onCreateFooterViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindFooterView(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public RecyclerView.ViewHolder onCreateBasicItemViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType) {
            case TYPE_NORMAL:
            case TYPE_UNAVAILABLE:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_episode, parent, false);
                return new EpisodeListAdapter.EpisodeViewHolder(v);
            case TYPE_LOADING:
            default:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_loading, parent, false);
                return new EpisodeListAdapter.SimpleHolder(v);

        }
    }

    @Override
    public void onBindBasicItemView(RecyclerView.ViewHolder holder, int position) {
        Episode episode = getItem(position);
        int itemType = getBasicItemType(position);
        switch(itemType)
        {
            case TYPE_NORMAL:
            case TYPE_UNAVAILABLE:
                final EpisodeViewHolder episodeHolder = (EpisodeViewHolder) holder;
                Picasso .with(mContext)
                        .load(mContext.getString(R.string.image_host_path) + ImageUtils.resizeImage(episode.getScreenshotHD(), 250))
                        .into(episodeHolder.imgScreenshot);

                episodeHolder.txtEpisodeName.setText(episode.getEpisodeName(Languages.English));
                episodeHolder.txtEpisodeNumber.setText(mContext.getString(R.string.episode) + " " + episode.getEpisodeNumber());

                if(episode.getAiredDate() != null) {
                    long now = System.currentTimeMillis();
                    long addedDate = episode.getAiredDate().getTime();
                    episodeHolder.txtReleasedDate.setText(DateUtils.getRelativeTimeSpanString(addedDate, now, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE));
                }
                else
                {
                    episodeHolder.txtReleasedDate.setText(mContext.getString(R.string.unknown_aired_date));
                }

                if(itemType == TYPE_UNAVAILABLE)
                    episodeHolder.txtEpisodeUnavailable.setVisibility(View.VISIBLE);
                else
                    episodeHolder.txtEpisodeUnavailable.setVisibility(View.GONE);

                break;
            case TYPE_LOADING:
                break;
        }
    }

    @Override
    public int getBasicItemCount() {
        return mItems.size();
    }

    @Override
    public int getBasicItemType(int position) {
        EpisodeItem episodeItem = mItems.get(position);
        if(episodeItem.isLoadingItem)
            return TYPE_LOADING;
        else if(episodeItem.episode.isAvailable())
            return TYPE_NORMAL;
        else
            return TYPE_UNAVAILABLE;
    }


    public Episode getItem(int position) {
        return mItems.get(position).episode;
    }

    public ArrayList<Episode> getItems() {
        ArrayList<Episode> episodes = new ArrayList<Episode>();

        for (int i = 0; i < mItems.size(); i++)
            episodes.add(mItems.get(i).episode);

        return episodes;
    }

    public class EpisodeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        View itemView;

        @Bind(R.id.txtEpisodeNumber)
        TextView txtEpisodeNumber;

        @Bind(R.id.txtEpisodeName)
        TextView txtEpisodeName;

        @Bind(R.id.imgScreenshot)
        ImageView imgScreenshot;

        @Bind(R.id.txtReleasedDate)
        TextView txtReleasedDate;

        @Bind(R.id.txtEpisodeUnavailable)
        TextView txtEpisodeUnavailable;

        public EpisodeViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.itemView = itemView;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mItemClickListener != null) {
                int position = getAdapterPosition();
                Episode episode = getItem(position - (useHeader() ? 1 : 0));
                if(episode.isAvailable())
                    mItemClickListener.onItemClick(view, episode, position);
                else
                    Toast.makeText(mContext, mContext.getString(R.string.the_episode_unavailable), Toast.LENGTH_LONG).show();
            }
        }

    }

    class UnavailableHolder extends RecyclerView.ViewHolder {

        View itemView;

        public UnavailableHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.itemView = itemView;
        }

    }

    class SimpleHolder extends RecyclerView.ViewHolder {

        View itemView;

        public SimpleHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
        }

    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.expand_text_view)
        ExpandableTextView txtSysnopsis;

        @Bind(R.id.txtAnimeUnavailable)
        TextView txtAnimeUnavailable;

        View itemView;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.itemView = itemView;
        }
    }

    class EpisodeItem {
        Episode episode;
        boolean isLoadingItem = false;

        EpisodeItem(Episode episode) {
            this.episode = episode;
        }

        EpisodeItem(boolean loading) {
            this.isLoadingItem = loading;
        }

    }
}