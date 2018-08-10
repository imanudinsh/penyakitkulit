package com.im.penyakitkulit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DetailPenyakitActivity extends AppCompatActivity {
    TextView tvnama, tvdesk, tvgejala, tvsolusi;
    ImageView ivimg;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_penyakit);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Detail Penyakit");

        tvnama = (TextView) findViewById(R.id.pnama);
        tvdesk = (TextView) findViewById(R.id.pdesk);
        tvgejala = (TextView) findViewById(R.id.pgejala);
        tvsolusi = (TextView) findViewById(R.id.psolusi);
        ivimg = (ImageView) findViewById(R.id.pimg);

        getIntentValue();
    }

    public void getIntentValue(){
        Intent intent = getIntent();
        tvnama.setText(Html.fromHtml(intent.getStringExtra("pnama")));
        tvdesk.setText(Html.fromHtml(intent.getStringExtra("pdesk")));
        tvgejala.setText(Html.fromHtml(intent.getStringExtra("pgejala")));
        tvsolusi.setText(Html.fromHtml(intent.getStringExtra("psolusi")));
        Picasso.with(this).load(intent.getStringExtra("pimg")).into(ivimg);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                finish();
                break;
        }

        return true;
    }
}
