package com.example.apk1;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MangaDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "manga.db";
    // Tăng version để onUpgrade chạy
    private static final int DB_VERSION = 3;

    public MangaDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Bảng chính: manga
        db.execSQL("CREATE TABLE manga (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "summary TEXT, " +
                "volume_count INTEGER, " +
                "tags TEXT, " +
                "publish_date TEXT, " +
                "cover_image TEXT" +
                ")");

        // 2. Bảng chapters: mỗi chương/tập riêng
        db.execSQL("CREATE TABLE chapters (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "manga_id INTEGER, " +
                "chapter_number INTEGER, " +
                "chapter_title TEXT, " +
                "page_count INTEGER, " +
                "FOREIGN KEY(manga_id) REFERENCES manga(id)" +
                ")");

        // 3. Bảng manga_pages: mỗi trang ảnh trong chương
        db.execSQL("CREATE TABLE manga_pages (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "chapter_id INTEGER, " +
                "page_number INTEGER, " +
                "image_path TEXT, " +
                "FOREIGN KEY(chapter_id) REFERENCES chapters(id)" +
                ")");

        // 4. Bảng recommendations: lưu các manga được đề xuất
        db.execSQL("CREATE TABLE recommendations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "manga_id INTEGER, " +
                "FOREIGN KEY(manga_id) REFERENCES manga(id)" +
                ")");

        // ===== Dữ liệu demo =====
        // a) Thêm 1 bộ manga
        db.execSQL("INSERT INTO manga (title, summary, volume_count, tags, publish_date, cover_image) VALUES (" +
                "'SPYxFAMILY', " +
                "'Hành trình phiêu lưu của Aya.', " +
                "108, " +
                "'Phiêu lưu,Hành động', " +
                "'2023-01-01', " +
                "'anhbia1'" +
                ")");

        // b) Thêm 2 chương cho manga_id = 1
        db.execSQL("INSERT INTO chapters (manga_id, chapter_number, chapter_title, page_count) VALUES " +
                "(1, 1, 'Chương 1: Khởi hành', 3), " +
                "(1, 2, 'Chương 2: Hòn đảo bí ẩn', 3)");

        // c) Thêm trang ảnh cho từng chương
        db.execSQL("INSERT INTO manga_pages (chapter_id, page_number, image_path) VALUES " +
                "(1, 1, 'mg1'), " +
                "(1, 2, 'mg2'), " +
                "(1, 3, 'mg3'), " +
                "(2, 1, 'mg21'), " +
                "(2, 2, 'mg22'), " +
                "(2, 3, 'mg23')");

        // d) Thêm demo recommendation (tham chiếu manga_id = 1)
        db.execSQL("INSERT INTO recommendations (manga_id) VALUES (1)");
        // Bạn có thể thêm nhiều record hơn nếu có nhiều manga

        // a) Thêm 1 bộ manga
        db.execSQL("INSERT INTO manga (title, summary, volume_count, tags, publish_date, cover_image) VALUES (" +
                "'DR.STONE', " +
                "'Hành trình phiêu lưu của Senku.', " +
                "108, " +
                "'Phiêu lưu,Hành động', " +
                "'2023-03-01', " +
                "'anhbia2'" +
                ")");

        // b) Thêm 2 chương cho manga_id = 1
        db.execSQL("INSERT INTO chapters (manga_id, chapter_number, chapter_title, page_count) VALUES " +
                "(2, 1, 'Chương 1: Khởi hành', 4), " +
                "(2, 2, 'Chương 2: Hóa đá', 3)");

        // c) Thêm trang ảnh cho từng chương
        db.execSQL("INSERT INTO manga_pages (chapter_id, page_number, image_path) VALUES " +
                "(3, 1, 'mg_drs_11'), " +
                "(3, 2, 'mg_drs_12'), " +
                "(3, 3, 'mg_drs_13'), " +
                "(3, 4, 'mg_drs_14'), " +
                "(4, 1, 'mg_drs_21'), " +
                "(4, 2, 'mg_drs_22'), " +
                "(4, 3, 'mg_drs_23')");

        // d) Thêm demo recommendation (tham chiếu manga_id = 1)
        db.execSQL("INSERT INTO recommendations (manga_id) VALUES (2)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xóa các bảng cũ để tạo lại hoàn toàn
        db.execSQL("DROP TABLE IF EXISTS recommendations");
        db.execSQL("DROP TABLE IF EXISTS manga_pages");
        db.execSQL("DROP TABLE IF EXISTS chapters");
        db.execSQL("DROP TABLE IF EXISTS manga");
        onCreate(db);
    }

    /**
     * Trả về Cursor chứa:
     *  - recommendations.id       AS rec_id
     *  - manga.id                 AS manga_id
     *  - manga.title              AS title
     *  - manga.cover_image        AS cover_image
     *  - chapters.id              AS chapter_id  (chương đầu tiên, số 1)
     */
    public Cursor getRecommendationsCursor() {
        String sql = "SELECT r.id     AS rec_id, "
                + "m.id     AS manga_id, "
                + "m.title  AS title, "
                + "m.cover_image AS cover_image, "
                + "ch.id    AS chapter_id "
                + "FROM recommendations r "
                + "JOIN manga m ON m.id = r.manga_id "
                + "LEFT JOIN chapters ch "
                + "  ON ch.manga_id = m.id AND ch.chapter_number = 1";
        return getReadableDatabase().rawQuery(sql, null);
    }

    public String getCoverImageName(int mangaId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT cover_image FROM manga WHERE id = ?",
                new String[]{ String.valueOf(mangaId) }
        );
        String name = null;
        if (c.moveToFirst()) {
            name = c.getString(0);
        }
        c.close();
        return name;
    }
}
