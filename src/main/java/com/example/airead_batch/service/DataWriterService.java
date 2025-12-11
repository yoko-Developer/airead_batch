package com.example.airead_batch.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DataWriterService {

    private final JdbcTemplate jdbcTemplate;

    // NULLを許容
    private Integer executiveCount = null;
    private Integer employeeCount = null;

    private static final String INSERT_SQL =
            "INSERT INTO non_consolidated_notes (dummy_id, year_end_executive_count, year_end_employee_count) " +
                    "VALUES (?, ?, ?)";

    public DataWriterService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void processCsvRow(String[] data) {

        if (data == null || data.length < 5) return;

        String itemName = data[0].trim();
        String ridStr   = data[3].trim();
        String valueStr = data[4].trim();

        // 役員数の取得
        if ("人数".equals(itemName) && "6".equals(ridStr)) {
            executiveCount = safelyParse(valueStr);
        }

        // 従業員数の取得
        if ("従業員数".equals(itemName) && "13".equals(ridStr)) {
            employeeCount = safelyParse(valueStr);
        }
    }

    public void writeDataToDbFinal() {

        // どちらも null の場合は DB 登録をスキップ
        if (executiveCount == null && employeeCount == null) {
            System.out.println("DB登録に必要な項目がないためスキップ");
            return;
        }

        // DBに書き込む値を取得（nullの場合は0として扱うか、DB定義に従いnullを渡す）
        String execCountStr = (executiveCount != null) ? executiveCount.toString() : "NULL";
        String empCountStr = (employeeCount != null) ? employeeCount.toString() : "NULL";

        try {
            jdbcTemplate.update(INSERT_SQL, "0", executiveCount, employeeCount);

            System.out.println("DB登録成功！役員数:" + execCountStr + ", 従業員数:" + empCountStr);

        } catch (Exception e) {
            System.err.println("DB書き込みエラー: " + e.getMessage());

        } finally {
            // 登録後、一時保存した値をリセット
            executiveCount = null;
            employeeCount = null;
        }
    }

    private Integer safelyParse(String valueStr) {
        if (valueStr == null) return null;

        String cleaned = valueStr.replaceAll("[^0-9]", "");

        if (cleaned.isEmpty()) return null;

        try {
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
