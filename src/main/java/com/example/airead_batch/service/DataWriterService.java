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

        // 役員数 (1ページ目: ItemName='人数' and 種類='計' の行を探す)
        if ("人数".equals(itemName)) {
            try {
                executiveCount = Integer.parseInt(valueStr);
            } catch (NumberFormatException ignored) {}
        }

        // 従業員数
        if ("従業員数".equals(itemName)) {
            try {
                employeeCount = Integer.parseInt(valueStr);
            } catch (NumberFormatException ignored) {}
        }
    }

    /**
     * すべてのCSVファイルの読み込みが完了した後
     * 抽出した役員数と従業員数をデータベースにまとめて書き込む
     * 書き込み後、一時保存したデータはリセットされる
     */
    public void writeDataToDbFinal() {

        // 役員数と従業員数の両方が見つかった場合のみDBに書き込む
        if (executiveCount != null && employeeCount != null) {
            try {
                jdbcTemplate.update(
                        INSERT_SQL,
                        "0", // dummy_id
                        executiveCount,
                        employeeCount
                );
                System.out.println("DB登録成功！役員数:" + executiveCount + ", 従業員数:" + employeeCount);

                // 登録後、一時保存した値をリセット
                executiveCount = null;
                employeeCount = null;

            } catch (Exception e) {
                System.err.println("DB書き込みエラー: " + e.getMessage());
            }
        }
    }
}