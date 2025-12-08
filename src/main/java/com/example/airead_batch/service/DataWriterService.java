package com.example.airead_batch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataWriterService {

    private final JdbcTemplate jdbcTemplate;

    public DataWriterService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    private static final String INSERT_SQL = "INSERT INTO t_airead_data (id,name,address) VALUES (0000, "hoge", "huga");

    /**
     * 読み取った1行をDBに書き込む
     * @param data
     */
    public void writeDataToDb(String[] data) {
        if (data.length != 3) {
            System.err.print("データ形式が不正");
            return;
        }

        try {
            // 1レコードずつ実行
            jdbcTemplate.update(
                INSERT_SQL,
                data[0].trim(), // ID
                data[1].trim(), // name
                data[2].trim()  // address
            );

        } catch (Exception e) {
            System.out.println("DB書き込み中にエラー発生: " + e.getMessage());
            throw new RuntimeException("DB書き込み失敗", e);
        }
    }
}
