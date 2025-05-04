package com.example.apk1;

import android.annotation.SuppressLint;
import android.database.Cursor;
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

public class RecommendationFragment extends Fragment {

    private ListView listRec;
    private ArrayList<RecItem> items = new ArrayList<>();

    static class RecItem {
        int mangaId, firstChapterId;
        String title, coverImageName;
        RecItem(int mid, int cid, String t, String cover) {
            mangaId = mid; firstChapterId = cid;
            title = t; coverImageName = cover;
        }
    }

    public RecommendationFragment() { }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inf.inflate(R.layout.fragment_recommendation, container, false);
        listRec = v.findViewById(R.id.list_recommend);
        loadRecommendations();

        ArrayAdapter<RecItem> adapter = new ArrayAdapter<RecItem>(
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
                RecItem ri = getItem(pos);
                text.setText(ri.title);
                int resId = getResources()
                        .getIdentifier(ri.coverImageName, "drawable",
                                requireContext().getPackageName());
                icon.setImageResource(resId);
                return row;
            }
        };

        listRec.setAdapter(adapter);
        listRec.setOnItemClickListener((parent, view, pos, id) -> {
            RecItem ri = items.get(pos);
            // Cập nhật MainActivity
            MainActivity act = (MainActivity) requireActivity();
            act.setCurrentManga(ri.mangaId, ri.firstChapterId);
            // Chuyển về Preview chương đầu
            PreviewFragment pf = PreviewFragment.newInstance(ri.firstChapterId);
            act.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, pf)
                    .addToBackStack(null)
                    .commit();
        });

        return v;
    }

    private void loadRecommendations() {
        items.clear();
        MangaDatabaseHelper dbh = new MangaDatabaseHelper(requireContext());
        Cursor c = dbh.getRecommendationsCursor();
        while (c.moveToNext()) {
            @SuppressLint("Range") int mid   = c.getInt(c.getColumnIndex("manga_id"));
            @SuppressLint("Range") int cid   = c.getInt(c.getColumnIndex("chapter_id"));
            @SuppressLint("Range") String t  = c.getString(c.getColumnIndex("title"));
            @SuppressLint("Range") String cv = c.getString(c.getColumnIndex("cover_image"));
            items.add(new RecItem(mid, cid, t, cv));
        }
        c.close();
    }
}
