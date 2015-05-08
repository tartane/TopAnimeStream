package com.topanimestream.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import com.topanimestream.App;

public class Episode implements Parcelable, Comparator<Episode> {
    private int AnimeId;
    private int EpisodeId;
    private String EpisodeNumber;
    private String EpisodeName;
    private String AiredDate;
    private ArrayList<Mirror> Mirrors;
    private ArrayList<Vk> Vks;
    private ArrayList<EpisodeInformations> EpisodeInformations;
    private String Screenshot;
    private int Order;
    private String ScreenshotHD;
    private ArrayList<Link> Links;
    private ArrayList<Subtitle> Subtitles;
    public Episode() {
        super();
    }

    public Episode(Parcel in) {
        Parcelable[] parcelableVkArray = in.readParcelableArray(Vk.class.getClassLoader());
        Vk[] resultVkArray = null;
        if (parcelableVkArray != null) {
            resultVkArray = Arrays.copyOf(parcelableVkArray, parcelableVkArray.length, Vk[].class);
            Vks = new ArrayList<Vk>(Arrays.asList(resultVkArray));
        }
        Parcelable[] parcelableMirrorArray = in.readParcelableArray(Mirror.class.getClassLoader());
        Mirror[] resultMirrorArray = null;
        if (parcelableMirrorArray != null) {
            resultMirrorArray = Arrays.copyOf(parcelableMirrorArray, parcelableMirrorArray.length, Mirror[].class);
            Mirrors = new ArrayList<Mirror>(Arrays.asList(resultMirrorArray));
        }

        Parcelable[] parcelableLinkArray = in.readParcelableArray(Link.class.getClassLoader());
        Link[] resultLinkArray = null;
        if (parcelableLinkArray != null) {
            resultLinkArray = Arrays.copyOf(parcelableLinkArray, parcelableLinkArray.length, Link[].class);
            Links = new ArrayList<Link>(Arrays.asList(resultLinkArray));
        }

        Parcelable[] parcelableEpisodeInfoArray = in.readParcelableArray(Link.class.getClassLoader());
        EpisodeInformations[] resultEpisodeInfoArray = null;
        if (parcelableEpisodeInfoArray != null) {
            resultEpisodeInfoArray = Arrays.copyOf(parcelableEpisodeInfoArray, parcelableEpisodeInfoArray.length, EpisodeInformations[].class);
            EpisodeInformations = new ArrayList<EpisodeInformations>(Arrays.asList(resultEpisodeInfoArray));
        }

        Parcelable[] parcelableSubtitleArray = in.readParcelableArray(Subtitle.class.getClassLoader());
        Subtitle[] resultSubtitleArray = null;
        if (parcelableSubtitleArray != null) {
            resultSubtitleArray = Arrays.copyOf(parcelableSubtitleArray, parcelableSubtitleArray.length, Subtitle[].class);
            Subtitles = new ArrayList<Subtitle>(Arrays.asList(resultSubtitleArray));
        }

        AnimeId = in.readInt();
        EpisodeId = in.readInt();
        EpisodeNumber = in.readString();
        EpisodeName = in.readString();
        AiredDate = in.readString();
        Screenshot = in.readString();
        Order = in.readInt();
        ScreenshotHD = in.readString();
    }

    public Episode(JSONObject jsonEpisode, Context context) {
        JSONArray episodeInfoArray = new JSONArray();
        JSONArray episodeMirrors = new JSONArray();
        JSONArray vkArray = new JSONArray();
        try {
            this.setEpisodeNumber(!jsonEpisode.isNull("EpisodeNumber") ? jsonEpisode.getString("EpisodeNumber") : "0");
            this.setEpisodeId(!jsonEpisode.isNull("EpisodeId") ? jsonEpisode.getInt("EpisodeId") : 0);
            this.setAnimeId(!jsonEpisode.isNull("AnimeId") ? jsonEpisode.getInt("AnimeId") : 0);
            this.setAiredDate(!jsonEpisode.isNull("AiredDate") ? jsonEpisode.getString("AiredDate") : null);
            this.setScreenshot(!jsonEpisode.isNull("Screenshot") ? jsonEpisode.getString("Screenshot") : null);
            this.setOrder(!jsonEpisode.isNull("Order") ? jsonEpisode.getInt("Order") : 0);
            episodeMirrors = !jsonEpisode.isNull("Mirrors") ? jsonEpisode.getJSONArray("Mirrors") : null;
            vkArray = !jsonEpisode.isNull("vks") ? jsonEpisode.getJSONArray("vks") : null;
            this.Mirrors = new ArrayList<Mirror>();
            if (episodeMirrors != null) {
                for (int i = 0; i < episodeMirrors.length(); i++) {
                    this.Mirrors.add(new Mirror(episodeMirrors.getJSONObject(i)));
                }
            }
            this.Vks = new ArrayList<Vk>();
            if (vkArray != null) {
                for (int i = 0; i < vkArray.length(); i++) {
                    this.Vks.add(new Vk(vkArray.getJSONObject(i)));
                }
            }


        } catch (Exception e) {
        }
    }

    public Episode(int animeId, int episodeId, String episodeNumber,
                   String episodeName, String airedDate) {
        super();
        AnimeId = animeId;
        EpisodeId = episodeId;
        EpisodeNumber = episodeNumber;
        EpisodeName = episodeName;
        AiredDate = airedDate;
    }

    public String getScreenshotHD() {
        return ScreenshotHD;
    }

    public ArrayList<Link> getLinks() {
        return Links;
    }

    public void setLinks(ArrayList<Link> links) {
        Links = links;
    }

    public void setScreenshotHD(String screenshotHD) {
        ScreenshotHD = screenshotHD;
    }

    public int getOrder() {
        return Order;
    }

    public void setOrder(int order) {
        Order = order;
    }

    public String getScreenshot() {
        return Screenshot;
    }

    public void setScreenshot(String screenshot) {
        Screenshot = screenshot;
    }

    public EpisodeInformations getEpisodeInformations() {
        if(this.EpisodeInformations == null || this.EpisodeInformations.size() < 1)
            return null;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        for(EpisodeInformations info: this.EpisodeInformations)
        {
            if (String.valueOf(info.getLanguageId()).equals(prefs.getString("prefLanguage", "1"))) {
                return info;
            }
        }

        return null;
    }

    public void setEpisodeInformations(ArrayList<EpisodeInformations> episodeInformations) {
        EpisodeInformations = episodeInformations;
    }

    public ArrayList<Vk> getVks() {
        return Vks;
    }

    public void setVks(ArrayList<Vk> vks) {
        Vks = vks;
    }

    public ArrayList<Mirror> getMirrors() {
        return Mirrors;
    }

    public void setMirrors(ArrayList<Mirror> mirrors) {
        Mirrors = mirrors;
    }

    public int getAnimeId() {
        return AnimeId;
    }

    public void setAnimeId(int animeId) {
        AnimeId = animeId;
    }

    public int getEpisodeId() {
        return EpisodeId;
    }

    public void setEpisodeId(int episodeId) {
        EpisodeId = episodeId;
    }

    public String getEpisodeNumber() {
        return EpisodeNumber;
    }

    public void setEpisodeNumber(String episodeNumber) {
        EpisodeNumber = episodeNumber;
    }

    public String getEpisodeName() {
        return EpisodeName;
    }

    public void setEpisodeName(String episodeName) {
        EpisodeName = episodeName;
    }

    public String getAiredDate() {
        return AiredDate;
    }

    public void setAiredDate(String airedDate) {
        AiredDate = airedDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (Vks == null)
            Vks = new ArrayList<Vk>();
        if (Mirrors == null)
            Mirrors = new ArrayList<Mirror>();
        if(Links == null)
            Links = new ArrayList<Link>();
        if(EpisodeInformations == null)
            EpisodeInformations = new ArrayList<EpisodeInformations>();
        if(Subtitles == null)
            Subtitles = new ArrayList<Subtitle>();

        Parcelable[] parcelableVkArray = new Parcelable[Vks.size()];
        dest.writeParcelableArray(Vks.toArray(parcelableVkArray), flags);
        Parcelable[] parcelableMirrorArray = new Parcelable[Mirrors.size()];
        dest.writeParcelableArray(Mirrors.toArray(parcelableMirrorArray), flags);
        Parcelable[] parcelableLinkArray = new Parcelable[Links.size()];
        dest.writeParcelableArray(Links.toArray(parcelableLinkArray), flags);
        Parcelable[] parcelableEpisodeInfoArray = new Parcelable[EpisodeInformations.size()];
        dest.writeParcelableArray(EpisodeInformations.toArray(parcelableEpisodeInfoArray), flags);
        Parcelable[] parcelableSubtitleArray = new Parcelable[Subtitles.size()];
        dest.writeParcelableArray(Subtitles.toArray(parcelableSubtitleArray), flags);
        dest.writeInt(AnimeId);
        dest.writeInt(EpisodeId);
        dest.writeString(EpisodeNumber);
        dest.writeString(EpisodeName);
        dest.writeString(AiredDate);
        dest.writeString(Screenshot);
        dest.writeInt(Order);
        dest.writeString(ScreenshotHD);


    }

    public static final Creator<Episode> CREATOR = new Creator<Episode>() {
        public Episode createFromParcel(Parcel in) {
            return new Episode(in);
        }

        public Episode[] newArray(int size) {
            return new Episode[size];
        }
    };

    @Override
    public int compare(Episode episode, Episode episode2) {
        int val = 0;

        if (episode.getOrder() < episode2.getOrder()) {
            val = -1;
        } else if (episode.getOrder() > episode2.getOrder()) {
            val = 1;
        } else if (episode.getOrder() == episode2.getOrder()) {
            val = 0;
        }
        return val;
    }
}
