package com.im.penyakitkulit.fragment;

import android.app.DialogFragment;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import yalantis.com.sidemenu.interfaces.ScreenShotable;

import com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient;
import com.ibm.watson.developer_cloud.conversation.v1.ConversationService;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
import com.ibm.watson.developer_cloud.service.exception.BadRequestException;
import com.ibm.watson.developer_cloud.service.exception.UnauthorizedException;
import com.im.penyakitkulit.AlertDialogFragment;
import com.im.penyakitkulit.ConversationMessage;
import com.im.penyakitkulit.DBAdapter;
import com.im.penyakitkulit.DetailPenyakitActivity;
import com.im.penyakitkulit.HTTPURLConnection;
import com.im.penyakitkulit.MainActivity;
import com.im.penyakitkulit.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class DiagnosaFragment extends Fragment implements ScreenShotable {
    private static final String USER_USER = "user";
    private static final String USER_WATSON = "watson";
    private static String namauser = "";
    private static String iduser = "";

    private View containerView;
    protected int res;
    private Bitmap bitmap;

    LinearLayout messageContainer;
    ScrollView scrollView;

    private String TAG = "DiagnosaFragment";

    /*
    private String MULAIDIAGNOSA_URL = "http://sispakkulit.imanudinsholeh.net/htdocs/api/diagnosa/mulai";
    private String GETGEJALA_URL = "http://sispakkulit.imanudinsholeh.net/htdocs/api/diagnosa/getgejala";
    private String SETGEJALA_URL = "http://sispakkulit.imanudinsholeh.net/htdocs/api/diagnosa/setgejala";
    */
    private String MULAIDIAGNOSA_URL = "http://sispakkulit.mybluemix.net/api/diagnosa/mulai";
    private String GETGEJALA_URL = "http://sispakkulit.mybluemix.net/api/diagnosa/getgejala";
    private String SETGEJALA_URL = "http://sispakkulit.mybluemix.net/api/diagnosa/setgejala";

    private ConversationService conversationService;
    private Map<String, Object> conversationContext;
    DBAdapter db;

    public static DiagnosaFragment newInstance(int resId) {
        DiagnosaFragment DiagnosaFragment = new DiagnosaFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Integer.class.getName(), resId);
        DiagnosaFragment.setArguments(bundle);
        return DiagnosaFragment;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.containerView = view.findViewById(R.id.container);
        setChat();
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
        rootView = inflater.inflate(R.layout.content_main, container, false);

        messageContainer = (LinearLayout) rootView.findViewById(R.id.message_container);
        scrollView = (ScrollView) rootView.findViewById(R.id.message_scrollview);

        db = new DBAdapter(getContext());
        setuser();
        // Initialize the Watson Conversation Service and instantiate the Message Log.
        conversationService = new ConversationService(ConversationService.VERSION_DATE_2016_07_11);
        conversationService.setUsernameAndPassword(getString(R.string.watson_conversation_username),
                getString(R.string.watson_conversation_password));

        // If we have a savedInstanceState, recover the previous Context and Message Log.
        if (savedInstanceState != null) {
            conversationContext = (Map<String,Object>)savedInstanceState.getSerializable("context");


            scrollView.scrollTo(0, scrollView.getBottom());
        } else {
            // Validate that the user's credentials are valid and that we can continue.
            // This also kicks off the first Conversation Task to obtain the intro message from Watson.
            ValidateCredentialsTask vct = new ValidateCredentialsTask();
            vct.execute();
        }

        ImageButton sendButton = (ImageButton) rootView.findViewById(R.id.send_button);
        final TextView entryText = (TextView) rootView.findViewById(R.id.entry_text);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = entryText.getText().toString();

                if (!text.isEmpty()) {
                    // Add the message to the UI.
                    addMessageFromUser(new ConversationMessage(text, USER_USER));

                    db.open();
                    db.isichat(text,USER_USER);
                    db.close();

                    ConversationTask ct = new ConversationTask();
                    ct.execute(text);
                    entryText.setText("");
                }
            }
        });



        // Core SDK must be initialized to interact with Bluemix Mobile services.
        BMSClient.getInstance().initialize(getContext(), BMSClient.REGION_US_SOUTH);

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
                DiagnosaFragment.this.bitmap = bitmap;
            }
        };

        thread.start();

    }

    @Override
    public Bitmap getBitmap() {
        return bitmap;
    }




    private void showDialog(int errorTitle, String errorMessage, boolean canContinue) {
        DialogFragment newFragment = AlertDialogFragment.newInstance(errorTitle, errorMessage, canContinue);
        newFragment.show(getActivity().getFragmentManager(), "dialog");
    }

    /**
     * Adds a message dialog view to the UI.
     * @param message ConversationMessage containing a message and the sender.
     */
    private void addMessageFromUser(ConversationMessage message) {
        if(message.getMessageText()!=null && !message.getMessageText().equals("")) {
            View messageView;

            if (message.getUser().equals(USER_WATSON)) {
                messageView = getActivity().getLayoutInflater().inflate(R.layout.watson_text, messageContainer, false);
                TextView watsonMessageText = (TextView) messageView.findViewById(R.id.watsonTextView);
                watsonMessageText.setText(Html.fromHtml(message.getMessageText()));
            } else {
                messageView = getActivity().getLayoutInflater().inflate(R.layout.user_text, messageContainer, false);
                TextView userMessageText = (TextView) messageView.findViewById(R.id.userTextView);
                userMessageText.setText(message.getMessageText());
            }

            messageContainer.addView(messageView);

            // Scroll to the bottom of the view so the user sees the update.

            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.fullScroll(View.FOCUS_DOWN);
                }
            });
        }
    }

    private void addMessageTemplate(String pid) {
        if(pid!=null && !pid.equals("")) {
            View messageView;

            db.open();
            String pnama="", pimg="", pdesk="", pgejala="", psolusi="";
            Cursor c = db.selectdata("tb_hasil","*","pid",pid);
            if (c.moveToFirst()) {
                do {
                    pnama = c.getString(2);
                    pdesk = c.getString(3);
                    psolusi = c.getString(4);
                    pgejala = c.getString(5);
                    pimg = c.getString(6);
                } while (c.moveToNext());
            }

            db.close();

            messageView = getActivity().getLayoutInflater().inflate(R.layout.answer_text, messageContainer, false);
            TextView watsonMessageText = (TextView) messageView.findViewById(R.id.watsonTextView);
            ImageView watsonMessageImage = (ImageView) messageView.findViewById(R.id.watsonImageView);
            watsonMessageText.setText(Html.fromHtml("Anda mengalami penyakit <b>"+pnama+"</b>"));
            Picasso.with(getContext()).load(pimg).placeholder(R.drawable.ic_launcher).into(watsonMessageImage);



            messageContainer.addView(messageView);
            final String finalPnama = pnama;
            final String finalPdesk = pdesk;
            final String finalPsolusi = psolusi;
            final String finalPgejala = pgejala;
            final String finalPimg = pimg;
            messageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), DetailPenyakitActivity.class);

                    intent.putExtra("pnama", finalPnama).
                            putExtra("pdesk", finalPdesk).
                            putExtra("psolusi", finalPsolusi).
                            putExtra("pgejala", finalPgejala).
                            putExtra("pimg", finalPimg);
                    startActivity(intent);
                }
            });

            // Scroll to the bottom of the view so the user sees the update.

            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.fullScroll(View.FOCUS_DOWN);
                }
            });
        }
    }

    /**
     * Asynchronously contacts the Watson Conversation Service to see if provided Credentials are valid.
     */
    private class ValidateCredentialsTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {

            // Mark whether or not the validation completes.
            boolean success = true;

            try {
                conversationService.getToken().execute();
            } catch (Exception ex) {

                success = false;

                // See if the user's credentials are valid or not, along with other errors.
                if (ex.getClass().equals(UnauthorizedException.class) ||
                        ex.getClass().equals(IllegalArgumentException.class)) {
                    showDialog(R.string.error_title_invalid_credentials,
                            getString(R.string.error_message_invalid_credentials), false);
                } else if (ex.getCause() != null &&
                        ex.getCause().getClass().equals(UnknownHostException.class)) {
                    showDialog(R.string.error_title_bluemix_connection,
                            getString(R.string.error_message_bluemix_connection), true);
                } else {
                    showDialog(R.string.error_title_default, ex.getMessage(), true);
                }
            }

            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            // If validation succeeded, then get the opening message from Watson Conversation
            // by sending an empty input string to the ConversationTask.
            if (success) {
                ConversationTask ct = new ConversationTask();
                ct.execute("");
            }
        }
    }

    /**
     * Asynchronously sends the user's message to Watson Conversation and receives Watson's response.
     */
    private class ConversationTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String entryText = params[0];

            MessageRequest messageRequest;

            // Send Context to Watson in order to continue a conversation.
            if (conversationContext == null) {
                messageRequest = new MessageRequest.Builder()
                        .inputText(entryText).build();
            } else {
                messageRequest = new MessageRequest.Builder()
                        .inputText(entryText)
                        .context(conversationContext).build();
            }

            try {
                // Send the message to the workspace designated in watson_credentials.xml.
                MessageResponse messageResponse = conversationService.message(
                        getString(R.string.watson_conversation_workspace_id), messageRequest).execute();

                conversationContext = messageResponse.getContext();
                return messageResponse.getText().get(0);
            } catch (Exception ex) {
                // A failure here is usually caused by an incorrect workspace in watson_credentials.
                if (ex.getClass().equals(BadRequestException.class)) {
                    showDialog(R.string.error_title_invalid_workspace,
                            getString(R.string.error_message_invalid_workspace), false);
                } else {
                    showDialog(R.string.error_title_default, ex.getMessage(), true);
                }
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // Add the message from Watson to the UI.
            //addMessageFromUser(new ConversationMessage(result, USER_WATSON));

            switch(result){
                case "mulai" :
                    mulaiDiagnosa ru = new mulaiDiagnosa();
                    ru.execute(iduser);
                    Log.d(TAG, "onWatson: "+result);
                    break;

                case "0" :
                case "1" :
                    String did="", gid="";
                    db.open();
                    Cursor c = db.ambilconfig("did");
                    if (c.moveToFirst()) {
                        do {
                            did = c.getString(1);
                        } while (c.moveToNext());


                        c = db.ambilconfig("gid");
                        if (c.moveToFirst()) {
                            do {
                                gid = c.getString(1);
                            } while (c.moveToNext());

                        }


                        setGejala r = new setGejala();
                        r.execute(did, gid, result);
                    }else{
                        String chat =  "Ayo mulai diagnosa penyakit kulit apa yang anda alami dan bagaimana cara mengobatinya...";
                        String user =  USER_WATSON;
                        addMessageFromUser(new ConversationMessage(chat, user));
                        db.isichat(chat,USER_WATSON);
                    }
                    db.close();
                    Log.d(TAG, "onClick: "+result);
                    break;


                case "ulang" :
                    Log.d(TAG, "onClick: "+result);
                    break;

            }


        }
    }

    public void setChat(){
        db.open();
        Cursor c = db.selecttable("tb_chat","chat,user");
        if (c.moveToFirst()) {
            do {
                String chat =  c.getString(0);
                String user =  c.getString(1);

                String awal = chat.substring(0,1);
                if(awal.equals("@")&& user.equals(USER_WATSON)){
                    String akhir = chat.substring(1);
                    addMessageTemplate(akhir);

                }else{
                    addMessageFromUser(new ConversationMessage(chat, user));
                }


            } while (c.moveToNext());

        } else {

            String chat =  "Hi "+namauser+"!\nAyo mulai diagnosa penyakit kulit apa yang anda alami dan bagaimana cara mengobatinya...";
            String user =  USER_WATSON;
            addMessageFromUser(new ConversationMessage(chat, user));
            db.isichat(chat,USER_WATSON);

        }

        db.close();

    }

    public void setuser(){
        db.open();
        Cursor c = db.ambilconfig("uid");
        if (c.moveToFirst()) {
            do {
                iduser = c.getString(1);
            } while (c.moveToNext());

        }

        c = db.ambilconfig("namauser");
        if (c.moveToFirst()) {
            do {
                namauser = c.getString(1);
            } while (c.moveToNext());

        }


        db.close();
    }

    class mulaiDiagnosa extends AsyncTask<String, Void, String> {
        HTTPURLConnection ruc = new HTTPURLConnection();


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Log.d(TAG, "json: "+s);
            String did = "";
            try {
                JSONObject response = new JSONObject(s);
                JSONArray posts = response.optJSONArray("result");
                for (int i = 0; i < posts.length(); i++) {
                    JSONObject post = posts.optJSONObject(i);
                    did = post.optString("id_diagnosa");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(did!="") {

                db.open();
                Cursor c = db.ambilconfig("did");
                if (c.moveToFirst()) {
                    do {
                        db.ubahconfig("did", did);
                    } while (c.moveToNext());
                } else {
                    db.isiconfig("did", did);
                }

                String intro = "okee, jawab pertanyaan - pertanyaan berikut ya";
                addMessageFromUser(new ConversationMessage(intro, USER_WATSON));
                db.isichat(intro, USER_WATSON);
                db.close();
                getGejala ru = new getGejala();
                ru.execute(did);
            }else{

            }
        }

        @Override
        protected String doInBackground(String... params) {

            HashMap<String, String> data = new HashMap<String,String>();
            data.put("uid", params[0]);
            final String result = ruc.ServerData(MULAIDIAGNOSA_URL,data);

            return  result;
        }

    }

    class getGejala extends AsyncTask<String, Void, String> {
        HTTPURLConnection ruc = new HTTPURLConnection();


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Log.d(TAG, "json: "+s);
            String gid = "";
            String gejala = "";
            String text = "";
            if(!s.equals("")) {
                try {
                    JSONObject response = new JSONObject(s);
                    int sts = response.optInt("status");
                    if (sts == 1) {
                        JSONArray posts = response.optJSONArray("result");
                        for (int i = 0; i < posts.length(); i++) {
                            JSONObject post = posts.optJSONObject(i);
                            gid = post.optString("kd_gejala");
                            gejala = post.optString("nama_gejala");
                        }

                        db.open();
                        Cursor c = db.ambilconfig("gid");
                        if (c.moveToFirst()) {
                            do {
                                db.ubahconfig("gid", gid);
                            } while (c.moveToNext());
                        } else {
                            db.isiconfig("gid", gid);
                        }
                        db.isichat(gejala + " ?", USER_WATSON);
                        db.close();
                        addMessageFromUser(new ConversationMessage(gejala + " ?", USER_WATSON));

                    } else if (sts == 2) {
                        db.open();
                        String pid = "", pnama = "", pdesk = "", psolusi = "", pgejala = "", pimg = "";
                        JSONArray posts = response.optJSONArray("result");
                        if (response.optString("result").equals(" ") || response.optString("result").equals("") || posts.length() == 0) {
                            text = "Anda tidak mengalami penyakit kulit";

                            Cursor c = db.ambilconfig("pid");
                            if (c.moveToFirst()) {
                                do {
                                    db.ubahconfig("pid", gid);
                                } while (c.moveToNext());
                            } else {
                                db.isiconfig("pid", gid);
                            }

                            db.isichat("@", USER_WATSON);
                            addMessageFromUser(new ConversationMessage(text, USER_WATSON));

                        } else {

                            for (int i = 0; i < posts.length(); i++) {
                                JSONObject post = posts.optJSONObject(i);
                                pid = post.optString("kd_penyakit");
                                pnama = post.optString("nama_penyakit");
                                pdesk = post.optString("deskripsi");
                                psolusi = post.optString("solusi");
                                pimg = post.optString("gambar");

                                JSONArray g = post.optJSONArray("gejala");
                                for (int j = 1; j <= g.length(); j++) {
                                    JSONObject gej = g.optJSONObject(j - 1);
                                    pgejala = pgejala + j + ". " + gej.optString("nama_gejala") + "<br>";
                                }
                            }


                            Cursor c = db.selectdata("tb_hasil", "*", "pid", pid);
                            if (c.moveToFirst()) {
                                do {
                                    db.ubahhasil(pid, pnama, pdesk, psolusi, pgejala, pimg);
                                } while (c.moveToNext());
                            } else {
                                db.isihasil(pid, pnama, pdesk, psolusi, pgejala, pimg);
                            }

                            db.isichat("@" + pid, USER_WATSON);
                            addMessageTemplate(pid);

                        }
                        db.open();
                        db.hapusconfig("did");
                        db.hapusconfig("gid");
                        db.close();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                db.open();
                text = "Anda tidak mengalami penyakit kulit";


                db.isichat("@", USER_WATSON);
                addMessageFromUser(new ConversationMessage(text, USER_WATSON));
                db.close();
            }


        }

        @Override
        protected String doInBackground(String... params) {

            HashMap<String, String> data = new HashMap<String,String>();
            data.put("did", params[0]);
            final String result = ruc.ServerData(GETGEJALA_URL,data);

            return  result;
        }

    }

    class setGejala extends AsyncTask<String, Void, String> {
        HTTPURLConnection ruc = new HTTPURLConnection();


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Log.d(TAG, "json: "+s);
            String did = "";
            db.open();
            Cursor c = db.ambilconfig("did");
            if (c.moveToFirst()) {
                do {
                    did = c.getString(1);
                } while (c.moveToNext());

            }
            try {
                JSONObject response = new JSONObject(s);
                int sts = response.optInt("status");
                if(sts==1) {
                    getGejala ru = new getGejala();
                    ru.execute(did);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        @Override
        protected String doInBackground(String... params) {

            HashMap<String, String> data = new HashMap<String,String>();
            data.put("did", params[0]);
            data.put("gid", params[1]);
            data.put("j", params[2]);
            final String result = ruc.ServerData(SETGEJALA_URL,data);

            return  result;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity())
                .setActionBarTitle("Diagnosa Penyakit");
    }

}

