package com.im.penyakitkulit.fragment;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import yalantis.com.sidemenu.interfaces.ScreenShotable;

import com.im.penyakitkulit.HTTPURLConnection;
import com.im.penyakitkulit.ItemListAdapter;
import com.im.penyakitkulit.MainActivity;
import com.im.penyakitkulit.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;




public class PenyakitFragment extends Fragment implements ScreenShotable {
    private static final String USER_USER = "user";
    private static final String USER_WATSON = "watson";
    private static String namauser = "";
    private static String iduser = "";

    private View containerView;
    protected int res;
    private Bitmap bitmap;

    ListView itemListView;
    ProgressBar progres;

    private String TAG = "PenyakitFragment";

    
    private String PENYAKIT_URL = "http://sispakkulit.mybluemix.net/api/penyakit";

    ItemListAdapter androidListAdapter;

    public static PenyakitFragment newInstance(int resId) {
        PenyakitFragment PenyakitFragment = new PenyakitFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Integer.class.getName(), resId);
        PenyakitFragment.setArguments(bundle);
        return PenyakitFragment;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.containerView = view.findViewById(R.id.container);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        res = getArguments().getInt(Integer.class.getName());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;

        rootView = inflater.inflate(R.layout.fragment_penyakit, container, false);


        itemListView = (ListView) rootView.findViewById(R.id.custom_listview_example);
        progres = (ProgressBar) rootView.findViewById(R.id.progress);

        getPenyakit ct = new getPenyakit();
        ct.execute("");





        return rootView;
    }

    @Override
    public void takeScreenShot() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Bitmap bitmap = Bitmap.createBitmap(containerView.getWidth(),
                        containerView.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                containerView.draw(canvas);
                PenyakitFragment.this.bitmap = bitmap;
            }
        };

        thread.start();

    }

    @Override
    public Bitmap getBitmap() {
        return bitmap;
    }



    class getPenyakit extends AsyncTask<String, Void, String> {
        HTTPURLConnection ruc = new HTTPURLConnection();


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Log.d(TAG, "json: "+s);
            String pid = "", pnama = "", pdesk = "", psolusi = "",  pgejala = "",  pimg = "";
            String text = "";
            try {
                JSONObject response = new JSONObject(s);
                JSONArray posts = response.getJSONArray("result");
                String[] nama = new String[posts.length()];
                String[] desk = new String[posts.length()];
                String[] solusi = new String[posts.length()];
                String[] gejala = new String[posts.length()];
                String[] img = new String[posts.length()];

                for (int i = 0; i < posts.length(); i++) {
                    JSONObject post = posts.optJSONObject(i);
                    pid = post.optString("kd_penyakit");
                    nama[i] = post.optString("namapenyakit");
                    desk[i] = post.optString("deskripsi");
                    solusi[i] = post.optString("solusi");

                    img[i] = post.optString("gambar");

                    JSONArray g = post.optJSONArray("gejala");
                    for (int j = 1; j <= g.length(); j++) {
                        JSONObject gej = g.optJSONObject(j - 1);
                        pgejala = pgejala + j + ". " + gej.optString("nama_gejala") + "<br>";
                    }

                    gejala[i] = pgejala;

                }

                itemListView.setVisibility(View.VISIBLE);
                progres.setVisibility(View.GONE);
                androidListAdapter = new ItemListAdapter(getActivity(), nama, desk, solusi, gejala, img);
                itemListView.setAdapter(androidListAdapter);

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        @Override
        protected String doInBackground(String... params) {

            HashMap<String, String> data = new HashMap<String,String>();
            final String result = ruc.ServerData(PENYAKIT_URL,data);

            return  result;
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity())
                .setActionBarTitle("Daftar Penyakit");
    }


}


