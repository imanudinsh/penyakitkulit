package com.im.penyakitkulit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {
    private final static String DATABASE_NAME = "dbspk";
    private final static int DATABASE_VERSION = 1;
    private final static String CONFIG_TABLE = "tb_config";
    private final static String CONFIG_TBHASIL = "tb_hasil";
    private final static String KEY_ID = "id_config";
    private final static String KEY_JUDUL = "judul";
    private final static String KEY_ISI = "isi";
    static final String SQL_CREATE_TABLE = "create table if not exists "+
            CONFIG_TABLE+" ("+
            KEY_ID+" integer primary key autoincrement,"+
            KEY_JUDUL+" text not null, "+
            KEY_ISI+" text not null )";

    static final String Favaorit_table_create = "create table if not exists tb_chat ( id_chat integer primary key autoincrement, chat text not null, user text not null, tgl text not null )";

    static final String SQL_CREATE_TBHASIL = "create table if not exists "+
            CONFIG_TBHASIL+" (idhasil integer primary key autoincrement," +
            "pid text not null, " +
            "nama text not null, " +
            "desk text not null, " +
            "solusi text not null, " +
            "gejala text not null, " +
            "gambar text not null )";

    static final String TAG = "DBAdapter";

    final Context context;
    DatabaseHelper DBHelper;
    SQLiteDatabase db;

    public DBAdapter(Context ctx) {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null,
                    DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(SQL_CREATE_TABLE);
                db.execSQL(Favaorit_table_create);
                db.execSQL(SQL_CREATE_TBHASIL);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion) {
            Log.w(TAG, "Memutakhirkan database dari versi " +
                    oldVersion
                    + " ke " + newVersion
                    + ", proses ini akan menghapus semua data");
            db.execSQL("DROP TABLE IF EXISTS tb_config");
            onCreate(db);
        }
    }
    // ---Membuka database---
    public DBAdapter open() throws SQLException {
        db = DBHelper.getWritableDatabase();
        return this;
    }
    // ---Menutup database---
    public void close() {
        DBHelper.close();
    }
    // ---memasukan data---
    public long isiconfig(String judul, String isi) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_JUDUL, judul);
        initialValues.put(KEY_ISI, isi);
        return db.insert(CONFIG_TABLE, null, initialValues);
    }

    public void isichat(String isi, String user) {
        //ContentValues initialValues = new ContentValues();
        //initialValues.put("chat", isi);
        //return db.insert("tb_chat", null, initialValues);
        String query = "INSERT INTO tb_chat VALUES (null,'"+isi+"', '"+user+"', datetime('now','localtime')) ";
        db.execSQL(query);
    }

    public void isihasil(String pid, String pnama, String pdesk, String psolusi, String pgejala, String pimg) {
        //ContentValues initialValues = new ContentValues();
        //initialValues.put("chat", isi);
        //return db.insert("tb_chat", null, initialValues);
        String query = "INSERT INTO "+CONFIG_TBHASIL+" VALUES (null,'"+pid+"', '"+pnama+"', '"+pdesk+"', '"+psolusi+"', '"+pgejala+"', '"+pimg+"') ";
        db.execSQL(query);
    }

    public void clearchat (){
        db.execSQL("delete from tb_chat");
    }


    public boolean hapusconfig(String judul) {
        return db.delete(CONFIG_TABLE, "judul='" + judul+"'", null) > 0;
    }

    public boolean hapusfavorit(String isi) {
        return db.delete("tb_favorit", "idresep='" + isi+"'", null) > 0;
    }





    public Cursor selecttable(String tabel, String field) {
        return db.query(tabel, new String[] { field }, null, null, null, null, null);
    }

    public Cursor selectorder(String tabel, String field, String order) {
        return db.query(tabel, new String[] { field }, null, null, null, null, order,null);
    }

    public Cursor ambilconfig(String judul) throws SQLException {
        Cursor mCursor = db.query(true, CONFIG_TABLE, new
                        String[] { KEY_JUDUL,
                        KEY_ISI }, KEY_JUDUL + "='" + judul+"'",
                null, null, null,
                null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }



    public Cursor selectdata(String tabel, String fieldambil, String field, String value) throws SQLException {
        Cursor mCursor = db.query(true, tabel, new
                        String[] {
                        fieldambil }, field + "='" + value+"'",
                null, null, null,
                null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }


    public boolean ubahconfig(String judul, String nilai) {
        ContentValues args = new ContentValues();
        args.put(KEY_ISI, nilai);
        return db.update(CONFIG_TABLE, args, KEY_JUDUL + "='" +
                judul+"'", null) > 0;
    }

    public boolean ubahhasil(String pid, String pnama, String pdesk, String psolusi, String pgejala, String pimg) {
        ContentValues args = new ContentValues();
        args.put("nama", pnama);
        args.put("desk", pdesk);
        args.put("solusi", psolusi);
        args.put("gejala", pgejala);
        args.put("gambar", pimg);
        return db.update(CONFIG_TBHASIL, args, "pid ='" +
                pid+"'", null) > 0;
    }



}
