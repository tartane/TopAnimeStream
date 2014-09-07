package com.aniblitz;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.aniblitz.models.AnimeSource;
import com.aniblitz.models.Mirror;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.SearchRecentSuggestions;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Utils {

	public static boolean isNumeric(String s) {  
	    return s.matches("[-+]?\\d*\\.?\\d+");  
	}  

	   public static String unGunzip(String str) {
		   String s1 = null;

		    try
		    {
		        byte b[] = str.getBytes();
		        InputStream bais = new ByteArrayInputStream(b);
		        GZIPInputStream gs = new GZIPInputStream(bais);
		        ByteArrayOutputStream baos = new ByteArrayOutputStream();
		        int numBytesRead = 0;
		        byte [] tempBytes = new byte[6000];
		        try
		        {
		            while ((numBytesRead = gs.read(tempBytes, 0, tempBytes.length)) != -1)
		            {
		                baos.write(tempBytes, 0, numBytesRead);
		            }

		            s1 = new String(baos.toByteArray());
		            s1= baos.toString();
		        }
		        catch(ZipException e)
		        {
		            e.printStackTrace();
		        }
		    }
		    catch(Exception e) {
		        e.printStackTrace();
		    }
		    return s1;
       }
	   public static class GetMp4 extends AsyncTask<Void, Void, String> {
			
			Mirror mirror;
			private Dialog busyDialog;
			private Activity act;
			private Resources r;
			public GetMp4(Mirror mirror, Activity act)
			{
				this.mirror = mirror;
				this.act = act;
				r = act.getResources();
			}
			
			
			@Override
		    protected void onPreExecute()
		    {
				busyDialog = Utils.showBusyDialog(r.getString(R.string.loading_video), act);

		    };      
		    @Override
		    protected String doInBackground(Void... params)
		    {   
		    	Document doc;
				try {
					if(mirror.getProvider().getName().toLowerCase().equals("animeultima"))
					{
						doc = Jsoup.connect(mirror.getSource()).userAgent("Chrome").ignoreContentType(true).get();
						String dailyMotionUrl = doc.baseUri().replace("swf/", "");
						doc = Jsoup.connect(dailyMotionUrl).userAgent("Chrome").get();
					}
					else
					{
						doc = Jsoup.connect(mirror.getSource()).userAgent("Chrome").get();
					}
					
					byte[] data = doc.html().getBytes("UTF-8");
					String base64 = Base64.encodeToString(data, Base64.DEFAULT);
					
			    	String URL = "http://lanbox.ca/AnimeServices/AnimeDataService.svc/GetMp4Url?provider='" + this.mirror.getProvider().getName() + "'&$format=json";
			    	HttpClient httpClient = new DefaultHttpClient();
					HttpPost request = new HttpPost(URL);
					/*
					List<NameValuePair> listParam = new ArrayList<NameValuePair>();
					listParam.add(new BasicNameValuePair("html", base64));
		            UrlEncodedFormEntity ent = new UrlEncodedFormEntity(listParam);*/
					request.setEntity(new StringEntity(base64));
					HttpResponse response = httpClient.execute(request);
					HttpEntity entity = response.getEntity();
					InputStream instream = entity.getContent();
					try {
						JSONObject jsonObj = new JSONObject(Utils.convertStreamToString(instream));
						return jsonObj.getString("value");
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					return null;
				} catch (IOException e) {
					return null;
				}

			}     
			    
		    @Override
		    protected void onPostExecute(String result)
		    {
                Utils.dismissBusyDialog(busyDialog);
		    	if(result == null)
		    	{
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(mirror.getSource()));
                    act.startActivity(i);
		    		//Toast.makeText(act, r.getString(R.string.error_loading_video) , Toast.LENGTH_LONG).show();

		    		return;
		    	}


		    	Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(result));
		    	intent.setDataAndType(Uri.parse(result), "video/*");
		    	act.startActivity(Intent.createChooser(intent, "Complete action using"));
		    }

		}
		public static void ShowProviders(final AnimeSource animeSource, final Activity act, AlertDialog alertProviders)
		{
			final ArrayList<String> providers = new ArrayList<String>();
			for(Mirror mirror:animeSource.getMirrors())
			{
				providers.add(mirror.getProvider().getName());
			}
			final CharSequence[] items = providers.toArray(new CharSequence[providers.size()]);
		 	final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(act);
		 	alertBuilder.setTitle("Choose a provider " + (animeSource.isSubbed() ? "(Subbed)" : "(Dubbed)"));
		 	alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
		 	    public void onClick(DialogInterface dialog, int item) {
		 	    	(new Utils.GetMp4(animeSource.getMirrors().get(item), act)).execute();
		 	    }
		 	});
		 	alertProviders = alertBuilder.create();
		 	alertProviders.show();
		}
		
		
	 private static boolean downloadFile(String url, File outputFile) {
		  //return true if successful, false otherwise
		 try {
		      URL u = new URL(url);
		      URLConnection conn = u.openConnection();
		      int contentLength = conn.getContentLength();
		      InputStream inputStream = conn.getInputStream();
		      OutputStream output = new FileOutputStream(outputFile);
		      byte[] buffer = new byte[8 * 1024]; // Or whatever
		      int bytesRead;
		      while ((bytesRead = inputStream.read(buffer)) > 0) {
		          output.write(buffer, 0, bytesRead);
		      }
		      output.flush();
		      output.close();
		      inputStream.close();

		  } catch(Exception e) {
		      return false; // swallow a 404
		  }
		 return true;
		}
	 public static boolean isGZipped(InputStream in) {
		  if (!in.markSupported()) {
		   in = new BufferedInputStream(in);
		  }
		  in.mark(2);
		  int magic = 0;
		  try {
		   magic = in.read() & 0xff | ((in.read() << 8) & 0xff00);
		   in.reset();
		  } catch (IOException e) {
		   e.printStackTrace(System.err);
		   return false;
		  }
		  return magic == GZIPInputStream.GZIP_MAGIC;
		 }

	    public static String getIPAddress(boolean useIPv4) {
	        try {
	            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
	            for (NetworkInterface intf : interfaces) {
	                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
	                for (InetAddress addr : addrs) {
	                    if (!addr.isLoopbackAddress()) {
	                        String sAddr = addr.getHostAddress().toUpperCase();
	                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr); 
	                        if (useIPv4) {
	                            if (isIPv4) 
	                                return sAddr;
	                        } else {
	                            if (!isIPv4) {
	                                int delim = sAddr.indexOf('%'); // drop ip6 port suffix
	                                return delim<0 ? sAddr : sAddr.substring(0, delim);
	                            }
	                        }
	                    }
	                }
	            }
	        } catch (Exception ex) { } // for now eat exceptions
	        return "";
	    }
  
	 public static File getLargestFileInDirectory(String directoryPath)
    {
        File largestFile = null;
        File aRootDir = new File(directoryPath);
        for (File file : aRootDir.listFiles())
        {
        	File folderFile = file;
        	if(file.isDirectory())
        	{
        		folderFile = Utils.getLargestFileInDirectory(folderFile.getAbsolutePath());	
        	}
        	if(largestFile != null)
        	{
        		if(folderFile != null)
        		{
		            if (largestFile.length() < folderFile.length())
		            {
		            	largestFile = folderFile;
		            }
        		}
        	}
        	else
        	{
        		largestFile = folderFile;
        	}
        }
        return largestFile;
    }

	 public static String formatHHMMSS(long secondsCount){  
		    //Calculate the seconds to display:  
		    int seconds = (int) (secondsCount %60);  
		    secondsCount -= seconds;  
		    //Calculate the minutes:  
		    long minutesCount = secondsCount / 60;  
		    long minutes = minutesCount % 60;  
		    minutesCount -= minutes;  
		    //Calculate the hours:  
		    long hoursCount = minutesCount / 60;
		    long hours = minutesCount % 60;
		    hoursCount -= hours;
		    
		    long daysCount = hoursCount / 24;
		    
		    if(daysCount > 99)
		    {
		    	return "∞";
		    }
		    //Build the String  
		    return "" + daysCount +  "d " + hoursCount + "h " + minutes + "m " + seconds + "s ";  
	} 
	public static String RemoveFolderInvalidChars(String line)
	{
		String ReservedChars = "|\\?*<\":>+[]/'";
		for(char c:ReservedChars.toCharArray())
		{
			line = line.replace(c, ' ');
		}
		return line;
	}
    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
              int count=is.read(bytes, 0, buffer_size);
              if(count==-1)
                  break;
              os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }
    public static double roundToHalf(double x) {
        return (Math.ceil(x * 2) / 2);
    }
    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
          sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }
    public static String humanReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
      if (listAdapter == null) {
      // pre-condition
            return;
      }

      int totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
      for (int i = 0; i < listAdapter.getCount(); i++) {
           View listItem = listAdapter.getView(i, null, listView);
           if (listItem instanceof ViewGroup) {
              listItem.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
           }
           listItem.measure(0, 0);
           totalHeight += listItem.getMeasuredHeight();
      }
      
      ViewGroup.LayoutParams params = listView.getLayoutParams();
      params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
                listView.setLayoutParams(params);
  }
    public static void setViewMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }
    public static String JsonStringToDateString(String jsonDate)
    {
    	Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		c.setTimeInMillis(Long.valueOf(jsonDate.replace("/Date(", "").replace(")/","")));
		String month = "";
		if(String.valueOf(c.get(Calendar.MONTH) + 1).length() > 1)
		{
			month = String.valueOf(c.get(Calendar.MONTH) + 1);
		}
		else
		{
			month = "0" + String.valueOf(c.get(Calendar.MONTH) + 1);
		}
		 String day = "";
		 if(String.valueOf(c.get(Calendar.DATE)).length() > 1)
		 {
			 day = String.valueOf(c.get(Calendar.DATE));
		 }
		 else
		 {
			 day = "0" + String.valueOf(c.get(Calendar.DATE));
		 }
		return String.valueOf(c.get(Calendar.YEAR)) + "/" + month + "/" + day;
    }

    public static JSONObject GetJson(String urlString)
    {
    	 BufferedReader reader = null;
    	    try {
    	        URL url = new URL(urlString);
    	        URLConnection conn = url.openConnection();
    	        //conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
    	        reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    	        StringBuffer buffer = new StringBuffer();
    	        int read;
    	        char[] chars = new char[1024];
    	        while ((read = reader.read(chars)) != -1)
    	            buffer.append(chars, 0, read); 
    	        	
    	       // String json = unGunzip(buffer.toString());
    	        String json = buffer.toString();
    	        if(json == null)
    	        	return null;
				return new JSONObject(json);
    	    } 
		    catch(Exception e)
		    {
		    	String hello = e.getMessage();
		    }finally {
    	        if (reader != null)
					try {
						reader.close();
					} catch (IOException e) {
					}
    	    }
    	return null;
    }
    public static String getStringFromFile (String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();        
        return ret;
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }


    public static void watchYoutubeVideo(String id, Activity act){
        try{
             Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
             act.startActivity(intent);                 
             }catch (Exception ex){
                 Intent intent=new Intent(Intent.ACTION_VIEW, 
                 Uri.parse("http://www.youtube.com/watch?v="+id));
                 act.startActivity(intent);
             }
    }

    public static String parseSearch(String key, String search)
    {
    	search = search.replaceAll("[^\\w\\s_]", " ").replace("_", " ");
    	ArrayList<String> terms = new ArrayList<String>();
    	for(String str:search.trim().replace("-", " ").split("[ ]", -1))
    	{
    		if(!String.format("%b", str).replaceAll("false","").equals(""))
    		{
    			terms.add("+" + str.trim() + "*");
    		}
    	}
    	return String.format(new String(new char[terms.toArray().length]).replace("\0", "%s" + " ").replaceFirst("(.*)" + " " + "$","$1"), terms.toArray()).replace("+", "+" + key + ":");
    }
    public static void restartActivity(Activity act) {
        Intent intent = act.getIntent();
        act.finish();
        act.startActivity(intent);
        act.overridePendingTransition(0, 0);
    }

	public static Dialog showBusyDialog(String message, Activity act) {
		Dialog busyDialog = new Dialog(act, R.style.lightbox_dialog);
	    busyDialog.setContentView(R.layout.lightbox_dialog);
	    ((TextView)busyDialog.findViewById(R.id.dialogText)).setText(message);
	    
	    busyDialog.show();
	    return busyDialog;
	}

	public static void dismissBusyDialog(Dialog busyDialog) {
	    if (busyDialog != null)
	        busyDialog.dismiss();

	    busyDialog = null;
	}  
	public static void SaveRecentSearch(Activity act, String query)
	{
		SearchRecentSuggestions suggestions = new SearchRecentSuggestions(act,
                SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
        suggestions.saveRecentQuery(query, null);
	}
	
	public static void lockScreen(Activity act)
	{
		int orientation = act.getRequestedOrientation();
	    int rotation = ((WindowManager) act.getSystemService(
	            Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
	    switch (rotation) {
	    case Surface.ROTATION_0:
	        orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
	        break;
	    case Surface.ROTATION_90:
	        orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
	        break;
	    case Surface.ROTATION_180:
	        orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
	        break;
	    default:
	        orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
	        break;
	    }

	    act.setRequestedOrientation(orientation);
	}
	public static void unlockScreen(Activity act)
	{
		act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}
	
public static class MirrorsTask extends AsyncTask<Void, Void, String> {
		
		private Activity act;
		private int animeId;
		private AlertDialog alertProviders;
		public MirrorsTask(Activity act, int animeId)
		{
			this.act = act;
			this.animeId = animeId;
		}
		private String URL; 
		private ArrayList<AnimeSource> animeSources;
		private Dialog busyDialog;
		private Dialog alertType;
		
		@Override
	    protected void onPreExecute()
	    {
			URL = "http://lanbox.ca/AnimeServices/AnimeDataService.svc/Animes(" + animeId + ")?$format=json&$expand=AnimeSources/Mirrors/Provider";
			busyDialog = Utils.showBusyDialog("Loading mirrors", act);
	    };      
	    @Override
	    protected String doInBackground(Void... params)
	    {   
	    	
	    	JSONObject json = Utils.GetJson(URL);
	    	JSONArray animeSourcesArray = new JSONArray();
	    	try {  	
		    	animeSources = new ArrayList<AnimeSource>();		    	
		    	animeSourcesArray = json.getJSONArray("AnimeSources");
			} catch (Exception e) {
				return null;
			}
	    	for(int i = 0;i<animeSourcesArray.length();i++)
	    	{
				try {
					animeSources.add(new AnimeSource(animeSourcesArray.getJSONObject(i)));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	    	}
		    return "Success";
		}     
		    
	    @Override
	    protected void onPostExecute(String result)
	    {
	    	if(result == null)
	    	{
	    		Toast.makeText(act, act.getResources().getString(R.string.error_loading_mirrors), Toast.LENGTH_LONG).show();
	    		return;
	    	}
	    	if(animeSources.size() > 1)
	    	{
	    		final CharSequence[] items = {"Dubbed", "Subbed"};
		     	final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(act);
		     	alertBuilder.setTitle("Choose a type");
		     	alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
		     	   

					public void onClick(DialogInterface dialog, int item) {
		     	    	if(items[item].equals("Dubbed"))
		     	    	{
		     	    		for(AnimeSource animeSource : animeSources)
		     	    		{
		     	    			if(!animeSource.isSubbed())
		     	    			{
		     	    				Utils.ShowProviders(animeSource,act,alertProviders);
		     	    				break;
		     	    			}
		     	    		}
		     	    	}
		     	    	else if(items[item].equals("Subbed"))
		     	    	{
		    	    		for(AnimeSource animeSource : animeSources)
		     	    		{
		     	    			if(animeSource.isSubbed())
		     	    			{
		     	    				Utils.ShowProviders(animeSource,act,alertProviders);
		     	    				break;
		     	    			}
		     	    		}
		     	    	}
		     	    }
		     	});
		     	alertType = alertBuilder.create();
		     	alertType.show();
		     	Utils.dismissBusyDialog(busyDialog);
	    	}
	    	else
	    	{
	    		Utils.ShowProviders(animeSources.get(0),act,alertProviders);
		        Utils.dismissBusyDialog(busyDialog);
	    	}
	    	
	    }
	
	}

}
