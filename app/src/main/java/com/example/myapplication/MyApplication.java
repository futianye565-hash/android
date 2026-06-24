package com.example.myapplication;

import android.app.Application;

import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.model.Quote;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // 在子线程初始化数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                AppDatabase db = AppDatabase.getInstance(MyApplication.this);
                if (db.quoteDao().getAllQuotes().isEmpty()) {
                    // 插入初始名言（你可以自己加更多）
                    db.quoteDao().insertQuote(new Quote("生活不止眼前的苟且", "高晓松", false));
                    db.quoteDao().insertQuote(new Quote("世界上只有一种真正的英雄主义", "罗曼·罗兰", false));
                    db.quoteDao().insertQuote(new Quote("要么读书，要么旅行", "佚名", false));
                    db.quoteDao().insertQuote(new Quote("人生如逆旅，我亦是行人", "苏轼", false));
                    db.quoteDao().insertQuote(new Quote("愿你出走半生，归来仍是少年", "孙衍", false));
                    db.quoteDao().insertQuote(new Quote("梦想还是要有的，万一实现了呢", "马云", false));
                    db.quoteDao().insertQuote(new Quote("不抛弃，不放弃", "许三多", false));
                    db.quoteDao().insertQuote(new Quote("知识就是力量", "培根", false));
                }
            }
        }).start();
    }
}