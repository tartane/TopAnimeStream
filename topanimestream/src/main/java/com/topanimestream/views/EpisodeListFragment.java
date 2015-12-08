package com.topanimestream.views;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

import com.google.gson.Gson;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.topanimestream.App;
import com.topanimestream.models.Anime;
import com.topanimestream.models.OdataRequestInfo;
import com.topanimestream.utilities.AsyncTaskTools;
import com.topanimestream.R;
import com.topanimestream.utilities.ODataUtils;
import com.topanimestream.utilities.Utils;
import com.topanimestream.utilities.WcfDataServiceUtility;
import com.topanimestream.adapters.EpisodeListAdapter;
import com.topanimestream.models.Episode;
import com.topanimestream.views.profile.LoginActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class EpisodeListFragment extends Fragment implements OnItemClickListener {

    private EpisodeListAdapter adapter;
    public Dialog busyDialog;
    private Anime anime;

    @Bind(R.id.txtNoEpisode)
    TextView txtNoEpisode;

    @Bind(R.id.progressBarLoadMore)
    ProgressBar progressBarLoadMore;

    @Bind(R.id.listViewEpisodes)
    ListView listViewEpisodes;

    public EpisodeListFragment() {

    }

    public static EpisodeListFragment newInstance(Anime anime) {
        EpisodeListFragment ttFrag = new EpisodeListFragment();
        Bundle args = new Bundle();
        args.putParcelable("anime", anime);
        ttFrag.setArguments(args);
        return ttFrag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {

        final Episode episode = anime.getEpisodes().get(position);
        Intent intent = new Intent(getActivity(), VideoPlayerActivity.class);
        intent.putExtra("anime", anime);
        intent.putExtra("episodeToPlay", episode);
        getActivity().startActivity(intent);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_episode_list, container, false);
        ButterKnife.bind(this, rootView);

        Bundle bundle = getArguments();
        anime = bundle.getParcelable("anime");

        if (savedInstanceState != null) {
            anime = savedInstanceState.getParcelable("anime");
            /*
            if (anime.getEpisodes() != null && anime.getEpisodes().size() > 0)
                listViewEpisodes.setAdapter(new EpisodeListAdapter(this.getActivity(), anime.getEpisodes()));
            else {
                listViewEpisodes.setVisibility(View.GONE);
                txtNoEpisode.setVisibility(View.VISIBLE);
            }*/
        }

        listViewEpisodes.setFastScrollEnabled(true);
        listViewEpisodes.setOnItemClickListener(this);
        progressBarLoadMore.setVisibility(View.VISIBLE);
        ODataUtils.GetEntityList(getString(R.string.odata_path) + "Episodes?$filter=AnimeId%20eq%20" + anime.getAnimeId() + "&$expand=EpisodeInformations,Links", Episode.class, new ODataUtils.Callback<ArrayList<Episode>>() {
            @Override
            public void onSuccess(ArrayList<Episode> episodes, OdataRequestInfo info) {
                Collections.sort(episodes, new Episode());
                anime.setEpisodes(episodes);
                //adapter = new EpisodeListAdapter(getActivity(), anime);
                /*
                SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(adapter);
                swingBottomInAnimationAdapter.setAbsListView(listViewEpisodes);
                assert swingBottomInAnimationAdapter.getViewAnimator() != null;
                swingBottomInAnimationAdapter.getViewAnimator().setInitialDelayMillis(300);
                listViewEpisodes.setAdapter(swingBottomInAnimationAdapter);
                progressBarLoadMore.setVisibility(View.GONE);*/
            }

            @Override
            public void onFailure(Exception e) {
                progressBarLoadMore.setVisibility(View.GONE);
            }
        });
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("anime", anime);
        super.onSaveInstanceState(outState);
    }


    private class EpisodesTask extends AsyncTask<Void, Void, String> {
        private ArrayList<Episode> episodes = new ArrayList<Episode>();
        public EpisodesTask() {

        }

        private String URL;

        @Override
        protected void onPreExecute() {
            Utils.lockScreen(getActivity());
            progressBarLoadMore.setVisibility(View.VISIBLE);
            URL = new WcfDataServiceUtility(getString(R.string.odata_path)).getEntity("Episodes").filter("AnimeId%20eq%20" + anime.getAnimeId() + "%20and%20Links/any()").expand("EpisodeInformations,Links").formatJson().build();
        }

        @Override
        protected String doInBackground(Void... params) {
            if (!App.IsNetworkConnected()) {
                return getString(R.string.error_internet_connection);
            }
            JSONObject json = Utils.GetJson(URL);
            if (!json.isNull("error")) {
                try {
                    int error = json.getInt("error");
                    if (error == 401) {
                        return "401";
                    }
                } catch (Exception e) {
                    return null;
                }
            }
            if (json == null) {
                return null;
            }
            JSONArray episodesArray = new JSONArray();

            try {
                episodesArray = json.getJSONArray("value");
            } catch (JSONException e) {
                return null;
            }

            Gson gson = new Gson();
            for (int i = 0; i < episodesArray.length(); i++) {
                try {
                    episodes.add(gson.fromJson(episodesArray.getJSONObject(i).toString(), Episode.class));
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }

            }
            return "Success";
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (result == null) {
                    Toast.makeText(getActivity(), getString(R.string.error_loading_episodes), Toast.LENGTH_LONG).show();
                } else if (result.equals("401")) {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.have_been_logged_out), Toast.LENGTH_LONG).show();
                    getActivity().startActivity(new Intent(getActivity(), LoginActivity.class));
                    getActivity().finish();
                } else {
                    Collections.sort(episodes, new Episode());
                    anime.setEpisodes(episodes);
                    /*
                    adapter = new EpisodeListAdapter(getActivity(), anime);
                    SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(adapter);
                    swingBottomInAnimationAdapter.setAbsListView(listViewEpisodes);
                    assert swingBottomInAnimationAdapter.getViewAnimator() != null;
                    swingBottomInAnimationAdapter.getViewAnimator().setInitialDelayMillis(300);
                    listViewEpisodes.setAdapter(swingBottomInAnimationAdapter);*/
                }

                progressBarLoadMore.setVisibility(View.GONE);

                if (listViewEpisodes.getAdapter().getCount() == 0) {
                    txtNoEpisode.setVisibility(View.VISIBLE);
                    listViewEpisodes.setVisibility(View.GONE);
                } else {
                    txtNoEpisode.setVisibility(View.GONE);
                    listViewEpisodes.setVisibility(View.VISIBLE);
                }

            } catch (Exception e)//catch all exception, handle orientation change
            {
                e.printStackTrace();
            }
            try {
                Utils.unlockScreen(getActivity());//User pressed back before the request is finished
            }catch(Exception e)
            {}
        }
    }

}
