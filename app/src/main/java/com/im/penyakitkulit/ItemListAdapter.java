package com.im.penyakitkulit;




import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

public class ItemListAdapter extends ArrayAdapter {
    String[] nama;
    String[] gejala;
    String[] solusi;
    String[] desk;
    String[] images;
    Context context;

    public ItemListAdapter(Activity context, String[] nama, String[] desk, String[] solusi, String[] gejala, String[] images) {
        super(context, R.layout.list_item, nama);
        this.nama = nama;
        this.desk = desk;
        this.solusi = solusi;
        this.gejala = gejala;
        this.images = images;
        this.context = context;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewRow = layoutInflater.inflate(R.layout.list_item, null,
                true);
        TextView mtextView = (TextView) viewRow.findViewById(R.id.text_view);
        ImageView mimageView = (ImageView) viewRow.findViewById(R.id.image_view);
        mtextView.setText(nama[i]);
        if(images[i].equals("") || images[i]==null){
            Picasso.with(context).load(R.drawable.ic_launcher).placeholder(R.drawable.ic_launcher).into(mimageView);
        }else {
            Picasso.with(context).load(images[i]).placeholder(R.drawable.ic_launcher).into(mimageView);
        }
        final int ii = i;
        viewRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent inten = new Intent(context, DetailPenyakitActivity.class);
                inten.putExtra("pnama", nama[ii]).
                        putExtra("pdesk", desk[ii]).
                        putExtra("psolusi", solusi[ii]).
                        putExtra("pgejala", gejala[ii]).
                        putExtra("pimg", images[ii]);
                context.startActivity(inten);
            }
        });
        return viewRow;
    }
}
