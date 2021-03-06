package com.topanimestream.views.profile;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.nirhart.parallaxscroll.views.ParallaxListView;
import com.squareup.picasso.Picasso;
import com.topanimestream.App;
import com.topanimestream.R;
import com.topanimestream.managers.DialogManager;
import com.topanimestream.utilities.AsyncTaskTools;
import com.topanimestream.utilities.ImageUtils;
import com.topanimestream.utilities.Utils;
import com.topanimestream.utilities.WcfDataServiceUtility;
import com.topanimestream.managers.AnimationManager;
import com.topanimestream.models.Anime;
import com.topanimestream.models.CurrentUser;
import com.topanimestream.models.Item;
import com.topanimestream.views.TASBaseActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import butterknife.Bind;

public class MyProfileActivity extends TASBaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private ListAdapter adapter;
    private String firstFavoriteBackDropUrl;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.listView)
    ParallaxListView listView;

    ImageView imgBackdrop;
    ImageView imgProfilePic;
    TextView txtUsername;
    TextView txtJoinedDate;
    TextView txtRank;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_myprofile);

        toolbar.setTitle(getString(R.string.my_profile));
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        setSupportActionBar(toolbar);

        final Item[] items = {
                new Item(getString(R.string.edit_profile), R.drawable.ic_edit),
                new Item(getString(R.string.menu_favorites), R.drawable.ic_star),
                new Item(getString(R.string.menu_mylist), R.drawable.ic_history_black),
                new Item(getString(R.string.menu_votes), R.drawable.ic_vote),
                new Item(getString(R.string.reviews), R.drawable.ic_review),
                new Item(getString(R.string.friends), R.drawable.ic_friends),
                new Item(getString(R.string.changes), R.drawable.ic_changes)};

        adapter = new ArrayAdapter<Item>(
                this,
                android.R.layout.select_dialog_item,
                android.R.id.text1,
                items){
            public View getView(int position, View convertView, ViewGroup parent) {
                //User super class to create the View
                View v = super.getView(position, convertView, parent);
                v.setBackgroundColor(Color.WHITE);
                TextView tv = (TextView)v.findViewById(android.R.id.text1);
                //Put the image on the TextView
                tv.setCompoundDrawablesWithIntrinsicBounds(items[position].icon, 0, 0, 0);

                //Add margin between image and text (support various screen densities)
                int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.8f);
                tv.setCompoundDrawablePadding(dp5);

                return v;
            }
        };
        View view = getLayoutInflater().inflate(R.layout.profile_header, null);
        imgBackdrop = (ImageView) view.findViewById(R.id.imgBackdrop);
        imgProfilePic = (ImageView) view.findViewById(R.id.imgProfilePic);
        txtUsername = (TextView) view.findViewById(R.id.txtUsername);
        txtJoinedDate = (TextView) view.findViewById(R.id.txtJoinedDate);
        txtRank = (TextView) view.findViewById(R.id.txtRank);
        listView.addParallaxedHeaderView(view);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(this);

        Picasso .with(this)
                .load(ImageUtils.resizeImage(getString(R.string.image_host_path) + App.currentUser.getProfilePic(), 185))
                .into(imgProfilePic);

        txtUsername.setText(App.currentUser.getUsername());
        txtJoinedDate.setText(App.currentUser.getAddedDate().toString());
        txtRank.setText(getString(R.string.rank) +  App.currentUser.getRoleName());

        AsyncTaskTools.execute(new ProfileTask());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        AnimationManager.ActivityFinish(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AsyncTaskTools.execute(new ProfileTask());
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        TextView txtMenuTitle = (TextView)view.findViewById(android.R.id.text1);
        if(txtMenuTitle.getText().equals(getString(R.string.edit_profile))){
            startActivity(new Intent(MyProfileActivity.this, EditProfileActivity.class));
        }else if(txtMenuTitle.getText().equals(getString(R.string.menu_favorites))){
            startActivity(new Intent(MyProfileActivity.this, MyFavoritesActivity.class));
        }else if(txtMenuTitle.getText().equals(getString(R.string.menu_mylist))){
            startActivity(new Intent(MyProfileActivity.this, MyWatchlistActivity.class));
        }else if(txtMenuTitle.getText().equals(getString(R.string.menu_votes))) {
            startActivity(new Intent(MyProfileActivity.this, MyVotesActivity.class));
        }else if(txtMenuTitle.getText().equals(getString(R.string.reviews))) {
            startActivity(new Intent(MyProfileActivity.this, MyReviewsActivity.class));
        }else if(txtMenuTitle.getText().equals(getString(R.string.friends))) {

        }else if(txtMenuTitle.getText().equals(getString(R.string.changes))) {

        }
    }

    public class ProfileTask extends AsyncTask<Void, Void, String> {
        private Dialog busyDialog;
        private String firstFavoriteUrl;
        @Override
        protected void onPreExecute() {
            busyDialog = DialogManager.showBusyDialog(getString(R.string.loading_your_profile), MyProfileActivity.this);
            firstFavoriteUrl = new WcfDataServiceUtility(getString(R.string.odata_path)).getEntity("Favorites").formatJson().expand("Anime").filter("AccountId%20eq%20" + App.currentUser.getAccountId() + "%20and%20Order%20eq%201").select("Anime/BackdropPath").build();
        }

        @Override
        protected String doInBackground(Void... params) {
            if (!App.IsNetworkConnected()) {
                return getString(R.string.error_internet_connection);
            }

            try {
                JSONObject json = Utils.GetJson(firstFavoriteUrl);
                String errors = Utils.checkDataServiceErrors(json, getString(R.string.error_loading_your_profile));
                if (errors != null)
                    return errors;
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
                Gson gson = new Gson();
                JSONArray jsonAnimes = json.getJSONArray("value");
                if(jsonAnimes.length() > 0) {
                    String jsonAnime = jsonAnimes.getJSONObject(0).getJSONObject("Anime").toString();
                    Anime anime = gson.fromJson(jsonAnime, Anime.class);
                    firstFavoriteBackDropUrl = ImageUtils.resizeImage(getString(R.string.image_host_path) + anime.getBackdropPath(), 500);
                }

                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return getString(R.string.error_loading_your_profile);
            }
        }

        @Override
        protected void onPostExecute(String error) {
            try {
                if (error != null) {
                    if (error.equals("401")) {
                        Toast.makeText(MyProfileActivity.this, getString(R.string.have_been_logged_out), Toast.LENGTH_LONG).show();
                        MyProfileActivity.this.startActivity(new Intent(MyProfileActivity.this, LoginActivity.class));
                        MyProfileActivity.this.finish();
                    } else {
                        Toast.makeText(MyProfileActivity.this, error, Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    App.imageLoader.displayImage(firstFavoriteBackDropUrl, imgBackdrop);

                }
            } catch (Exception e)//catch all exception, handle orientation change
            {
                e.printStackTrace();
            }
            DialogManager.dismissBusyDialog(busyDialog);

        }

    }
}
