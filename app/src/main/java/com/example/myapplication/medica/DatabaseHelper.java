package com.example.myapplication.medica;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "medicaments.db";
    private static final int DATABASE_VERSION = 1;

    // Nom de la table
    private static final String TABLE_MEDICAMENT = "medicament";

    // Colonnes
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NOM = "nom";
    private static final String COLUMN_DOSE = "dose";
    private static final String COLUMN_HEURE = "heure";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Création de la table
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_MEDICAMENT + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NOM + " TEXT,"
                + COLUMN_DOSE + " TEXT,"
                + COLUMN_HEURE + " TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    // Mise à jour de la base si version change
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICAMENT);
        onCreate(db);
    }

    // Ajouter un médicament
    public void ajouterMedicament(Medicament medicament) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOM, medicament.getNom());
        values.put(COLUMN_DOSE, medicament.getDose());
        values.put(COLUMN_HEURE, medicament.getHeurePrise());

        db.insert(TABLE_MEDICAMENT, null, values);
        db.close();
    }

    // Lire tous les médicaments
    public List<Medicament> getTousLesMedicaments() {
        List<Medicament> medicamentList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MEDICAMENT, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String nom = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOM));
                String dose = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOSE));
                String heure = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HEURE));

                Medicament medicament = new Medicament(id, nom, dose, heure);
                medicamentList.add(medicament);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return medicamentList;
    }

    // Supprimer un médicament par ID
    public void supprimerMedicament(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MEDICAMENT, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }
}
