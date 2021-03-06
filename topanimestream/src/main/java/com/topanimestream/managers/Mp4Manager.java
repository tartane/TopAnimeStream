package com.topanimestream.managers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Base64;
import android.widget.Toast;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.images.WebImage;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.topanimestream.App;
import com.topanimestream.preferences.Prefs;
import com.topanimestream.utilities.AsyncTaskTools;
import com.topanimestream.utilities.PrefUtils;
import com.topanimestream.utilities.Utils;
import com.topanimestream.views.VideoActivity;
import com.topanimestream.models.Anime;
import com.topanimestream.models.Episode;
import com.topanimestream.models.Mirror;
import com.topanimestream.R;

public class Mp4Manager {
    public static Dialog qualityDialog;
    public static String ignitionKey = null;

    public static void getMp4(Mirror mirror, Activity act, Anime anime, Episode episode) {
        AsyncTaskTools.execute(new GetWebPageTask(mirror, act, anime, episode));
    }

    public static class GetWebPageTask extends AsyncTask<Void, Void, String> {
        Mirror mirror;
        private Dialog busyDialog;
        private Activity act;
        private Anime anime;
        private AlertDialog alertPlay;
        private Episode episode;
        private String providerName;
        private Document doc;

        public GetWebPageTask(Mirror mirror, Activity act, Anime anime, Episode episode) {
            this.mirror = mirror;
            this.act = act;
            this.anime = anime;
            this.episode = episode;
        }

        @Override
        protected void onPreExecute() {
            busyDialog = DialogManager.showBusyDialog(act.getString(R.string.loading_video), act);
        }


        @Override
        protected String doInBackground(Void... params) {

            try {
                providerName = mirror.getProvider().getName().toLowerCase();
                if (providerName.equals("animeultima")) {
                    doc = Jsoup.connect(mirror.getSource()).userAgent("Chrome").ignoreContentType(true).get();
                    String dailyMotionUrl = doc.baseUri().replace("swf/", "");
                    doc = Jsoup.connect(dailyMotionUrl).userAgent("Chrome").get();
                } else if (providerName.equals("ignition s") || providerName.equals("ignition hd")) {
                    if (ignitionKey != null)
                        return ignitionKey;
                    doc = Jsoup.connect(URLDecoder.decode("http://jkanime.net/naruto/1/", "UTF-8")).userAgent("Chrome").get();
                } else {
                    doc = Jsoup.connect(URLDecoder.decode(mirror.getSource(), "UTF-8")).userAgent("Chrome").get();
                }
                if (providerName.equals(act.getString(R.string.play).toLowerCase())) {
                    providerName = "vk";
                }
            } catch (Exception e) {
                return null;
            }

            return "Success";
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (result == null) {
                    Toast.makeText(act, act.getString(R.string.error_loading_video), Toast.LENGTH_LONG).show();
                    AsyncTaskTools.execute(new Utils.ReportMirror(mirror.getMirrorId(), act));
                } else {
                    if (providerName.equals("vk") || providerName.equals("vk_gk") || providerName.equals("vkontakte")) {
                        String content = doc.html();
                        CharSequence[] items = null;
                        if (content.indexOf("url720") != -1)
                            items = new CharSequence[]{"720", "480", "360", "240"};
                        else if (content.indexOf("url480") != -1)
                            items = new CharSequence[]{"480", "360", "240"};
                        else if (content.indexOf("url360") != -1)
                            items = new CharSequence[]{"360", "240"};
                        else if (content.indexOf("url240") != -1)
                            items = new CharSequence[]{"240"};

                        if (items != null && items.length > 1) {
                            final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(act);
                            alertBuilder.setTitle(act.getString(R.string.choose_quality));
                            final CharSequence[] finalItems = items;
                            alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    AsyncTaskTools.execute(new GetMp4(mirror, act, anime, episode, providerName, finalItems[item].toString(), doc, null));
                                }
                            });
                            qualityDialog = alertBuilder.create();
                            qualityDialog.show();
                        } else if (items != null) {
                            Toast.makeText(act, act.getString(R.string.only_240p_available), Toast.LENGTH_SHORT).show();
                            AsyncTaskTools.execute(new GetMp4(mirror, act, anime, episode, providerName, "240", doc, null));
                        } else {
                            throw new Exception("Vk not valid video");
                        }


                        DialogManager.dismissBusyDialog(busyDialog);
                    } else {
                        AsyncTaskTools.execute(new GetMp4(mirror, act, anime, episode, providerName, null, doc, busyDialog));
                    }
                }


            } catch (Exception e)//catch all exception, handle orientation change
            {
                e.printStackTrace();
                Toast.makeText(act, act.getString(R.string.error_loading_video), Toast.LENGTH_LONG).show();
                DialogManager.dismissBusyDialog(busyDialog);
                AsyncTaskTools.execute(new Utils.ReportMirror(mirror.getMirrorId(), act));
            }


        }

    }


    public static class GetMp4 extends AsyncTask<Void, Void, String> {

        Mirror mirror;
        private Dialog busyDialog;
        private Activity act;
        private String providerName;
        private String quality;
        private Document doc;

        public GetMp4(Mirror mirror, Activity act, Anime anime, Episode episode, String providerName, String quality, Document doc, Dialog busyDialog) {
            this.mirror = mirror;
            this.act = act;
            this.providerName = providerName;
            this.quality = quality;
            this.doc = doc;
            this.busyDialog = busyDialog;
        }


        @Override
        protected void onPreExecute() {
            if (busyDialog == null) {
                busyDialog = DialogManager.showBusyDialog(act.getString(R.string.loading_video), act);
            }
        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                byte[] data = doc.html().getBytes("UTF-8");
                String base64 = Base64.encodeToString(data, Base64.DEFAULT);


                String URL = act.getString(R.string.odata_path) + "GetMp4Url?provider='" + URLEncoder.encode(providerName) + "'" + (quality != null ? "&quality='" + quality + "'" : "") + "&$format=json";
                //TODO test the okhttpclient
                OkHttpClient client = new OkHttpClient();
                final MediaType plain = MediaType.parse("text/plain");
                RequestBody body = RequestBody.create(plain, base64);
                Request request = new Request.Builder()
                        .url(URL)
                        .post(body)
                        .addHeader("Authentication",App.accessToken)
                        .build();
                Response response = client.newCall(request).execute();

                try {
                    JSONObject jsonObj = new JSONObject(Utils.convertStreamToString(response.body().byteStream()));
                    return jsonObj.getString("value");
                } catch (JSONException e1) {
                    e1.printStackTrace();
                    return null;
                } catch (Exception e1) {
                    e1.printStackTrace();
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(final String result) {
            DialogManager.dismissBusyDialog(busyDialog);

            if (result == null) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                try {
                    i.setData(Uri.parse(URLDecoder.decode(mirror.getSource(), "UTF-8")));
                } catch (UnsupportedEncodingException e) {
                    return;
                }
                act.startActivity(i);
                AsyncTaskTools.execute(new Utils.ReportMirror(mirror.getMirrorId(), act));
                return;
            }

            String playInternal = PrefUtils.get(act, Prefs.PLAY_INTERNAL, "undefined");
            if (playInternal.equals("true"))
                PlayInternalVideo(act, result, mirror.getMirrorId());
            else if (playInternal.equals("false"))
                PlayExternalVideo(act, result);
            else
                DialogManager.ShowChoosePlayerDialog(act, result, mirror.getMirrorId());

        }

    }

    public static MediaInfo buildMediaInfo(String title,
                                           String subTitle, String studio, String url, String imgUrl, String bigImageUrl) {
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, subTitle);
        movieMetadata.putString(MediaMetadata.KEY_TITLE, title);
        movieMetadata.putString(MediaMetadata.KEY_STUDIO, studio);
        movieMetadata.addImage(new WebImage(Uri.parse(imgUrl)));
        movieMetadata.addImage(new WebImage(Uri.parse(bigImageUrl)));

        return new MediaInfo.Builder(url)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType("video/mp4")
                .setMetadata(movieMetadata)
                .build();
    }


    public static void PlayInternalVideo(Context context, String mp4Url, int mirrorId) {
        Intent intent = null;
        intent = new Intent(context, VideoActivity.class);
        intent.putExtra("Mp4Url", mp4Url);
        intent.putExtra("MirrorId", mirrorId);
        context.startActivity(intent);
    }

    public static void PlayExternalVideo(Context context, String mp4Url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(mp4Url), "video/*");
        context.startActivity(intent);
    }
}
