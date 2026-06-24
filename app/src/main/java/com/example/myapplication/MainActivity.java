package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.textfield.TextInputEditText;
import android.view.LayoutInflater;

import com.example.myapplication.R;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.model.Quote;

import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private TextView tvQuote, tvAuthor,tvTotal, tvFavoriteCount;
    private Button btnNext, btnFavorite, btnViewFavorites,btnAddQuote;
    private List<Quote> allQuotes;
    private Quote currentQuote;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化控件
        tvQuote = findViewById(R.id.tv_quote);
        tvAuthor = findViewById(R.id.tv_author);
        btnNext = findViewById(R.id.btn_next);
        btnFavorite = findViewById(R.id.btn_favorite);
        btnViewFavorites = findViewById(R.id.btn_view_favorites);
        btnAddQuote = findViewById(R.id.btn_add_quote);
        tvTotal = findViewById(R.id.tv_total);
        tvFavoriteCount = findViewById(R.id.tv_favorite_count);

        // 获取数据库实例
        db = AppDatabase.getInstance(this);

        // 加载所有名言
        loadQuotes();

        // 点击"换一句"
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRandomQuote();
            }
        });
        
        btnAddQuote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddQuoteDialog();
            }
        });


        // 点击"收藏"
        btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentQuote != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            currentQuote.setFavorite(true);
                            db.quoteDao().updateQuote(currentQuote);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "✅ 已收藏！", Toast.LENGTH_SHORT).show();
                                    updateFavoriteButton();
                                }
                            });
                        }
                    }).start();
                }
            }
        });

        // 点击"查看收藏"
        btnViewFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FavoriteActivity.class);
                startActivity(intent);
            }
        });

        // 首次显示随机名言
        showRandomQuote();
    }

    private void showAddQuoteDialog() {
        // 创建对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_quote, null);
        builder.setView(dialogView);

        // 获取控件
        TextInputEditText etContent = dialogView.findViewById(R.id.et_content);
        TextInputEditText etAuthor = dialogView.findViewById(R.id.et_author);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSave = dialogView.findViewById(R.id.btn_save);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);  // 点击外部不关闭

        // 取消按钮
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // 保存按钮
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = etContent.getText().toString().trim();
                String author = etAuthor.getText().toString().trim();

                if (content.isEmpty()) {
                    Toast.makeText(MainActivity.this, "请输入名言内容", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (author.isEmpty()) {
                    author = "佚名";
                }

                // 保存到数据库
                String finalAuthor = author;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Quote newQuote = new Quote(content, finalAuthor, false);
                        db.quoteDao().insertQuote(newQuote);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "✅ 添加成功！", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                loadQuotes();  // 刷新数据
                            }
                        });
                    }
                }).start();
            }
        });

        dialog.show();
    }

    private void loadQuotes() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                allQuotes = db.quoteDao().getAllQuotes();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (allQuotes.isEmpty()) {
                            Toast.makeText(MainActivity.this, "暂无数据，请重启App", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    private void showRandomQuote() {
        if (allQuotes == null || allQuotes.isEmpty()) {
            loadQuotes();
            return;
        }
        Random random = new Random();
        int index = random.nextInt(allQuotes.size());
        currentQuote = allQuotes.get(index);
        tvQuote.setText(currentQuote.getContent());
        tvAuthor.setText("—— " + currentQuote.getAuthor());
        updateFavoriteButton();
    }

    private void updateFavoriteButton() {
        if (currentQuote != null && currentQuote.isFavorite()) {
            btnFavorite.setText("✅ 已收藏");
            btnFavorite.setEnabled(false);
            btnFavorite.setAlpha(0.5f);
        } else {
            btnFavorite.setText("❤️ 收藏");
            btnFavorite.setEnabled(true);
            btnFavorite.setAlpha(1.0f);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 每次返回主页时刷新数据（可能有新的收藏状态变化）
        loadQuotes();
    }
}