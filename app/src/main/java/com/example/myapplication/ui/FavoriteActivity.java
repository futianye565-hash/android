package com.example.myapplication.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.adapter.FavoriteAdapter;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.model.Quote;

import java.util.List;

public class FavoriteActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FavoriteAdapter adapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        recyclerView = findViewById(R.id.rv_favorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = AppDatabase.getInstance(this);

        loadFavorites();
    }

    private void loadFavorites() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Quote> favorites = db.quoteDao().getFavoriteQuotes();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter = new FavoriteAdapter(favorites, new FavoriteAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(Quote quote) {
                                // 点击取消收藏
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        quote.setFavorite(false);
                                        db.quoteDao().updateQuote(quote);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(FavoriteActivity.this, "已取消收藏", Toast.LENGTH_SHORT).show();
                                                loadFavorites(); // 刷新列表
                                            }
                                        });
                                    }
                                }).start();
                            }
                        });
                        recyclerView.setAdapter(adapter);

                        // 如果没有收藏，显示提示
                        if (favorites.isEmpty()) {
                            Toast.makeText(FavoriteActivity.this, "暂无收藏", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }
}