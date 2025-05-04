package com.example.apk1;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class EpisodesFragment extends Fragment {

    private static final String ARG_MANGA_ID = "manga_id";
    private int mangaId = 1; // default

    private ListView listChapters;
    private ArrayList<ChapterItem> items = new ArrayList<>();

    static class ChapterItem {
        int chapterId;
        String title, coverImageName;
        ChapterItem(int id, String t, String cover) {
            chapterId = id; title = t; coverImageName = cover;
        }
    }

    public EpisodesFragment() { }

    public static EpisodesFragment newInstance(int mangaId) {
        EpisodesFragment f = new EpisodesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MANGA_ID, mangaId);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mangaId = getArguments().getInt(ARG_MANGA_ID, 1);
        }
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_episodes, container, false);
        listChapters = v.findViewById(R.id.list_chapters);
        loadChapters();

        ArrayAdapter<ChapterItem> adapter = new ArrayAdapter<ChapterItem>(
                requireContext(),
                android.R.layout.activity_list_item,
                android.R.id.text1,
                items
        ) {
            @Override
            public View getView(int pos, View cv, ViewGroup parent) {
                View row = super.getView(pos, cv, parent);
                ImageView icon = row.findViewById(android.R.id.icon);
                TextView text = row.findViewById(android.R.id.text1);
                ChapterItem ci = getItem(pos);
                text.setText(ci.title);
                int resId = getResources()
                        .getIdentifier(ci.coverImageName, "drawable",
                                requireContext().getPackageName());
                icon.setImageResource(resId);
                return row;
            }
        };

        listChapters.setAdapter(adapter);
        listChapters.setOnItemClickListener((parent, view, pos, id) -> {
            ChapterItem ci = items.get(pos);
            PreviewFragment pf = PreviewFragment.newInstance(ci.chapterId);
            ((FragmentActivity) requireContext()).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, pf)
                    .addToBackStack(null)
                    .commit();
        });

        return v;
    }

    private void loadChapters() {
        items.clear();
        MangaDatabaseHelper dbh = new MangaDatabaseHelper(requireContext());
        SQLiteDatabase db = dbh.getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT ch.id, ch.chapter_number, ch.chapter_title, mp.image_path " +
                        "FROM chapters ch " +
                        "LEFT JOIN manga_pages mp " +
                        "  ON mp.chapter_id = ch.id AND mp.page_number = 1 " +
                        "WHERE ch.manga_id = ? " +
                        "ORDER BY ch.chapter_number ASC",
                new String[]{ String.valueOf(mangaId) }
        );

        while (c.moveToNext()) {
            int cid  = c.getInt(0);
            int num  = c.getInt(1);
            String t = c.getString(2);
            String img = c.getString(3);
            if (img == null) img = "one_piece_cover";
            items.add(new ChapterItem(cid, t, img));
//          items.add(new ChapterItem(cid, "Chương " + num + ": " + t, img));
        }
        c.close();
        db.close();
    }
}
