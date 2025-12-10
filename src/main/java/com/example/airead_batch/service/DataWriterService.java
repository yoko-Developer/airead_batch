package com.example.airead_batch.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DataWriterService {

    private final JdbcTemplate jdbcTemplate;

    // 役員数と従業員数を一時保存するための static 変数
    private static Integer executiveCount = null;
    private static Integer employeeCount = null;

    private static final String INSERT_SQL =
            "INSERT INTO non_consolidated_notes (dummy_id, year_end_executive_count, year_end_employee_count) " +
                    "VALUES (?, ?, ?)";

    public DataWriterService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // CSVの1行を読み込む
    // data[0] = ItemName, data[4] = Value
    public void processCsvRow(String[] data) {

        // 必須列チェック（ItemNameとValueが最低限必要）
        if (data.length < 5) {
            return;
        }

        String itemName = data[0].trim();
        String valueStr = data[4].trim();

        // 役員数
        if ("人数".equals(itemName)) {
            executiveCount = safelyParse(valueStr);
        }

        // 従業員数
        if ("従業員数".equals(itemName)) {
            employeeCount = safelyParse(valueStr);
        }
    }

    /**
     * すべてのCSVファイルの読み込みが完了した後
     * 抽出した役員数と従業員数をデータベースにまとめて書き込む
     * 書き込み後、一時保存したデータはリセットされる
     */
    public void writeDataToDbFinal() {

        // 役員数と従業員数のどちらかあればDBに書き込む
        if (executiveCount != null && employeeCount != null) {
            try {
                jdbcTemplate.update(
                        INSERT_SQL,
                        "0", // dummy_id
                        executiveCount,
                        employeeCount
                );

                String execCount = (executiveCount != null) ? executiveCount.toString() : "NULL";
                String empCount = (employeeCount != null) ? employeeCount.toString() : "NULL";

                System.out.println("DB登録成功！役員数:" + executiveCount + ", 従業員数:" + employeeCount);

                // 登録後、一時保存した値をリセット
                executiveCount = null;
                employeeCount = null;

            } catch (Exception e) {
                System.err.println("DB書き込みエラー: " + e.getMessage());
            }
        } else {
            // どちらの項目もCSVに見つからなかった場合はスキップ
            System.out.println("スキップ: DB登録に必要項目なし");
        }
    }

    private Integer safelyParse(String valueStr) {
        // 数字以外を全て削除
        String cleaned = valueStr.trim().replaceAll("[^0-9]", "");

        if (cleaned.isEmpty()) {
            return 0; // 空文字はゼロとして扱う
        }
        try {
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException ignored) {
            return 0; // 解析失敗時はゼロとして扱う
        }
    }
}
