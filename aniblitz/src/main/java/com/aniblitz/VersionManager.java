package com.aniblitz;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class VersionManager {
    //Executed everytime the app is opened
    public static void checkUpdate(Context context)
    {
        AsyncTaskTools.execute(new CheckUpdateTask(context));
    }

    public static class CheckUpdateTask extends AsyncTask<Void, Void, String> {
        private Context context;

        public CheckUpdateTask(Context context)
        {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {


        }


        @Override
        protected String doInBackground(Void... params) {
            if(App.IsNetworkConnected())
            {
               //TODO get json
            }
            else
            {
                return null;
            }

            return "Success";
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (result == null) {
                    //Failed to get the version
                }
                else
                {
                    //TODO check if current version < server version then show dialog
                    DialogManager.ShowUpdateDialog(context);
                }

            } catch (Exception e)//catch all exception, handle orientation change
            {
                e.printStackTrace();
            }


        }

    }
}
