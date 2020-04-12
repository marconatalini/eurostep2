package com.marconatalini.eurostep;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by Marco on 03/06/2016.
 */
public class db_eurostep {
    // To prevent someone from accidentally instantiating the class,
    // give it an empty constructor.
    public db_eurostep() {}

    private static final String TEXT_TYPE = " TEXT";
    private static final String DATETIME_TYPE = " DATETIME";
    private static final String TIMESTAMP_TYPE = " TIMESTAMP";
    private static final String BLOB_TYPE = " BLOB";
    private static final String INT_TYPE = " INTEGER";
    private static final String REAL_TYPE = " REAL";
    private static final String COMMA_SEP = ",";

    /* Inner class that defines the table contents */
    public static abstract class registro implements BaseColumns {
        public static final String TABLE_NAME = "registro";
        public static final String COLUMN_NAME_REGISTRAZIONE_ID = "_id";
        public static final String COLUMN_NAME_DATI = "dati";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
    }

    private static final String SQL_CREATE_REGISTRO =
            "CREATE TABLE " + registro.TABLE_NAME + " (" +
                    registro.COLUMN_NAME_REGISTRAZIONE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    registro.COLUMN_NAME_DATI + TEXT_TYPE + COMMA_SEP +
                    //registro.COLUMN_NAME_TIMESTAMP + TIMESTAMP_TYPE + " DEFAULT CURRENT_TIMESTAMP NOT NULL" +
                    registro.COLUMN_NAME_TIMESTAMP + TIMESTAMP_TYPE + " DEFAULT (DATETIME(CURRENT_TIMESTAMP, 'LOCALTIME'))" +
                    " )";

    private static final String SQL_DELETE_REGISTRO =
            "DROP TABLE IF EXISTS " + registro.TABLE_NAME;

    public static class EurostepDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 6;
        public static final String DATABASE_NAME = "eurostep.db";

        EurostepDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_REGISTRO);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_REGISTRO);
            onCreate(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }
}

