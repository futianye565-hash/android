package com.example.myapplication.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.model.Quote;

import java.util.List;

@Dao
public interface QuoteDao {
    @Query("SELECT * FROM quotes")
    List<Quote> getAllQuotes();

    @Query("SELECT * FROM quotes WHERE isFavorite = 1")
    List<Quote> getFavoriteQuotes();

    @Insert
    void insertQuote(Quote quote);

    @Update
    void updateQuote(Quote quote);

    // ← 确保这个方法存在
    @Delete
    void deleteQuote(Quote quote);

    // 可选：获取总数
    @Query("SELECT COUNT(*) FROM quotes")
    int getCount();
}