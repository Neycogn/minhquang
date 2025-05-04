package com.example.apk1;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PreviewFragment extends Fragment {

    private LinearLayout mangaContainer;
    private TextView summaryTextView;

    // Mặc định chapterId = 1; nếu cần truyền động thì dùng newInstance()
    private static final int DEFAULT_CHAPTER_ID = 1;

    public PreviewFragment() {
        // Required empty public constructor
    }

    // Phương thức tiện lợi để truyền chapterId từ EpisodesFragment
    public static PreviewFragment newInstance(int chapterId) {
        Bundle args = new Bundle();
        args.putInt("chapter_id", chapterId);
        PreviewFragment f = new PreviewFragment();
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate layout
        View view = inflater.inflate(R.layout.fragment_preview, container, false);

        // Ánh xạ
        summaryTextView = view.findViewById(R.id.summaryTextView);
        mangaContainer   = view.findViewById(R.id.mangaContainer);

        // Lấy chapterId (mặc định hoặc từ arguments)
        int chapterId = DEFAULT_CHAPTER_ID;
        if (getArguments() != null) {
            chapterId = getArguments().getInt("chapter_id", DEFAULT_CHAPTER_ID);
        }

        // Mở DB
        MangaDatabaseHelper dbHelper = new MangaDatabaseHelper(requireContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 1. Lấy summary từ bảng manga, join qua bảng chapters
        Cursor c1 = db.rawQuery(
                "SELECT m.summary " +
                        "FROM manga m " +
                        "JOIN chapters c ON m.id = c.manga_id " +
                        "WHERE c.id = ?",
                new String[]{ String.valueOf(chapterId) }
        );
        if (c1.moveToFirst()) {
            summaryTextView.setText(c1.getString(0));
        }
        c1.close();

        // 2. Lấy và hiển thị ảnh truyện theo chapter_id
        Cursor c2 = db.rawQuery(
                "SELECT image_path " +
                        "FROM manga_pages " +
                        "WHERE chapter_id = ? " +          // <-- Đã đổi từ manga_id sang chapter_id
                        "ORDER BY page_number ASC",
                new String[]{ String.valueOf(chapterId) }
        );
        while (c2.moveToNext()) {
            String imageName = c2.getString(0);
            int resId = getResources()
                    .getIdentifier(imageName, "drawable",
                            requireContext().getPackageName());

            ImageView iv = new ImageView(requireContext());
            iv.setLayoutParams(new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));
            iv.setAdjustViewBounds(true);
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            iv.setImageResource(resId);
            iv.setPadding(0, 0, 0, 16);

            mangaContainer.addView(iv);
        }
        c2.close();

        db.close();
        return view;
    }
}
