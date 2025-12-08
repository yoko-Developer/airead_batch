package com.example.airead_batch.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DataWriterService {

    private final JdbcTemplate jdbcTemplate;

    public DataWriterService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    private static final String INSERT_SQL = "INSERT INTO non_consolidated_notes (dummy_id, year_end_executive_count, year_end_employee_count) " +
            "VALUES (?, ?, ?)";

    /**
     * csvから2つのデータを受け取る
     * @param data
     */
    public void writeDataToDb(String[] data) {
        if (data.length != 2) {
            throw new IllegalArgumentException("csvデータの列数が不正");
        }

        try {
            // 1レコードずつSQLを実行
            jdbcTemplate.update(
                INSERT_SQL,
                "0",            // ID:固定値0
                data[0].trim(), // year_end_executive_count (期末役員数)
                data[1].trim()  // year_end_employee_count (期末従業員数)
            );

        } catch (Exception e) {
            System.err.println("DB書き込み中にエラー発生: " + e.getMessage());
            throw new RuntimeException("DB書き込み失敗", e);
        }
    }
}
