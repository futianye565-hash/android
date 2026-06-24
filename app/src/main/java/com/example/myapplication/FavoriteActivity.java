package com.example.myapplication;  // 改成你的包名

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.content.DialogInterface;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.appcompat.app.AlertDialog;

import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.model.Quote;

import java.util.List;

public class FavoriteActivity extends AppCompatActivity {
    private static final String TAG = "FavoriteActivity";
    private RecyclerView recyclerView;
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

    @Override
    protected void onResume() {
        super.onResume();
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
                        if (favorites == null || favorites.isEmpty()) {
                            Toast.makeText(FavoriteActivity.this,
                                    "还没有收藏哦，去收藏一些名言吧！", Toast.LENGTH_LONG).show();
                        }

                        FavoriteAdapter adapter = new FavoriteAdapter(favorites);
                        recyclerView.setAdapter(adapter);
                    }
                });
            }
        }).start();
    }

    // ========== 适配器 ==========
    class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {
        private List<Quote> list;

        public FavoriteAdapter(List<Quote> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_quote, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (list == null || position >= list.size()) return;

            Quote quote = list.get(position);
            holder.tvContent.setText(quote.getContent());
            holder.tvAuthor.setText("—— " + quote.getAuthor());

            // 短按：取消收藏
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            quote.setFavorite(false);
                            db.quoteDao().updateQuote(quote);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(FavoriteActivity.this,
                                            "已取消收藏", Toast.LENGTH_SHORT).show();
                                    loadFavorites();
                                }
                            });
                        }
                    }).start();
                }
            });

            // 长按：彻底删除
            holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    new AlertDialog.Builder(FavoriteActivity.this)
                            .setTitle("删除名言")
                            .setMessage("确定要删除这条名言吗？\n\n" + quote.getContent())
                            .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            // 从数据库彻底删除
                                            db.quoteDao().deleteQuote(quote);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(FavoriteActivity.this,
                                                            "已删除", Toast.LENGTH_SHORT).show();
                                                    loadFavorites();
                                                }
                                            });
                                        }
                                    }).start();
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return list == null ? 0 : list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            CardView cardView;
            TextView tvContent, tvAuthor;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                cardView = (CardView) itemView;
                tvContent = itemView.findViewById(R.id.tv_quote_content);
                tvAuthor = itemView.findViewById(R.id.tv_quote_author);
            }
        }
    }
}