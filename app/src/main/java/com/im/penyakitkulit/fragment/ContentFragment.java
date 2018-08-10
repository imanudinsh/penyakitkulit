package com.im.penyakitkulit.fragment;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import yalantis.com.sidemenu.interfaces.ScreenShotable;

import com.im.penyakitkulit.HTTPURLConnection;
import com.im.penyakitkulit.ItemListAdapter;
import com.im.penyakitkulit.ItemListAdapter;
import com.im.penyakitkulit.MainActivity;
import com.im.penyakitkulit.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class ContentFragment extends Fragment implements ScreenShotable {
    public static final String CLOSE = "Close";
    public static final String MAIN = "Main";
    public static final String DIAGNOSA = "Diagnosa";
    public static final String PENYAKIT = "Penyakit";
    public static final String ABOUT = "About";

    private View containerView;
    protected int res;
    private Bitmap bitmap;
    ListView itemListView;
    LinearLayout ll;
    ProgressBar progres;
    ItemListAdapter androidListAdapter;


    private String PENYAKIT_URL = "http://sispakkulit.mybluemix.net/api/penyakit/sample";

    public static ContentFragment newInstance(int resId) {
        ContentFragment contentFragment = new ContentFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Integer.class.getName(), resId);
        contentFragment.setArguments(bundle);
        return contentFragment;
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
        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        itemListView = (ListView) rootView.findViewById(R.id.llp);
        ll = (LinearLayout) rootView.findViewById(R.id.ll);
        progres = (ProgressBar) rootView.findViewById(R.id.progress);

        Button btndiagnosa = (Button) rootView.findViewById(R.id.btnmulai);
        btndiagnosa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).gotoDiagnosa();
            }
        });

        /*Button btnpenyakit = (Button) rootView.findViewById(R.id.btnlain);
        btnpenyakit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).gotoPenyakit();
            }
        });*/

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
                ContentFragment.this.bitmap = bitmap;
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

            Log.d("MainFragment", "json: "+s);
            String pgejala = "";
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

                    androidListAdapter = new ItemListAdapter(getActivity(), nama, desk, solusi, gejala, img);
                    itemListView.setAdapter(androidListAdapter);
                    progres.setVisibility(View.GONE);
                    ll.setVisibility(View.VISIBLE);
                }


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
                .setActionBarTitle("Penyakit Kulit");
    }
}

