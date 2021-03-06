package facelookapp.facedetectionlib;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by ohad on 9/4/15.
 */
public class FacesStore extends SQLiteOpenHelper implements BaseColumns {
    public static final String TABLE_NAME = "Faces";

    public static final String FILE_NAME = "FILE_NAME";
    public static final String LEFT_EYE_ANGLE = "left_eye_angle";
    public static final String RIGHT_EYE_ANGLE = "right_eye_angle";
    public static final String CHEEK_ANGLE = "cheek_angle";
    public static final String CHEEK_DIST = "cheek_dist";
    public static final String IS_SMILING = "is_smiling";
    public static final int DATABASE_VERSION = 1;
    private static final String TEXT = " TEXT", REAL = " REAL", INTEGER = " INTEGER", COMMA = ",";
    private static final String DATABASE_NAME = "Faces.db";
    private static final String CREATE_TABLE = "CREATE TABLE " +
            TABLE_NAME + " (" + _ID + " INTEGER UNIQUE PRIMARY KEY" + COMMA +
            FILE_NAME + TEXT + COMMA +
            LEFT_EYE_ANGLE + REAL + COMMA +
            RIGHT_EYE_ANGLE + REAL + COMMA +
            CHEEK_ANGLE + REAL + COMMA +
            CHEEK_DIST + REAL + COMMA +
            IS_SMILING + INTEGER +
            " )";
    private static final String REMOVE_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
    private static SQLiteDatabase read;
    private static FacesStore DB;

    private FacesStore(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static void initDB(Context context) {
        DB = new FacesStore(context);
        read = DB.getReadableDatabase();
    }

    public static FacesStore getDB() {
        return DB;
    }

    public static void insert(String column, ContentValues values) {
        getWritableDB().insert(TABLE_NAME, column, values);
    }

    public static SQLiteDatabase getWritableDB() {
        return DB.getWritableDatabase();
    }

    public static SQLiteDatabase getReadableDB() {
        return read;
    }

    public static Filter getFilter(final String s) {
        final BiometricFace face = BiometricFace.fromString(s);
        return new Filter() {


            @Override
            public boolean accept(String pathname) {
                SQLiteDatabase data = getReadableDB();
                String[] file = {pathname};
                Cursor cur = data.query(TABLE_NAME, null, FILE_NAME, file, null, null, null);
                if (!cur.moveToFirst()) {
                    cur.close();
                    return false;
                }

                int leftEyeColumn = cur.getColumnIndex(LEFT_EYE_ANGLE),
                        rightEyeColumn = cur.getColumnIndex(RIGHT_EYE_ANGLE),
                        cheekAngleColumn = cur.getColumnIndex(CHEEK_ANGLE),
                        cheekDistColumn = cur.getColumnIndex(CHEEK_DIST),
                        isSmilingColumn = cur.getColumnIndex(IS_SMILING);

                do {
                    BiometricFace other = new BiometricFace(cur.getDouble(leftEyeColumn),
                            cur.getDouble(rightEyeColumn), cur.getDouble(cheekAngleColumn),
                            cur.getDouble(cheekDistColumn), cur.getInt(isSmilingColumn));
                    if (BiometricFace.compareFaces(face, other) < .5) {
                        cur.close();
                        return true;
                    }
                } while (cur.moveToNext());
                cur.close();
                return false;
            }
        };
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //no old version yet
    }

    public static interface Filter {
        public boolean accept(String pathname);
    }

}
