package com.example.myapplication.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class FaceDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "face_database.db";
    private static final int DATABASE_VERSION = 1;

    // Table name
    private static final String TABLE_FACES = "faces";

    // Columns for the table
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_EMBEDDING = "embedding";

    private Context context;
    public String getColumnName() {
        return COLUMN_NAME;
    }


    public FaceDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_FACES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_EMBEDDING + " TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FACES);
        onCreate(db);
    }

    // Add a face to the database
    public boolean addFace(String name, String embedding) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EMBEDDING, embedding);

        long result = db.insert(TABLE_FACES, null, values);
        return result != -1;  // If the insert was successful, return true
    }

    // Get all faces from the database
    public Cursor getAllFaces() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_FACES, null, null, null, null, null, null);
    }
}
