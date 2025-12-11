package com.example.airead_batch.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * 役員数、従業員数を抽出してデータベースへ登録するSereviceクラス
 */
@Service
public class DataWriterService {

    private final JdbcTemplate jdbcTemplate;

    // NULLを許容する Integer型に変更 (DB登録時に null を渡すため)
    private Integer executiveCount = null;
    private Integer employeeCount = null;

    private static final String INSERT_SQL =
            "INSERT INTO non_consolidated_notes (dummy_id, year_end_executive_count, year_end_employee_count) " +
                    "VALUES (?, ?, ?)";

    public DataWriterService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Value の末尾のクォーテーションを削除
    public void processCsvRow(String[] data) {

        if (data == null || data.length < 5) return;

        // すべてトリミングして、ItemName, RID, Value を取得
        String itemName = data[0].trim();
        String ridStr   = data[3].trim();
        String valueStr = data[4].trim();

        // Value末尾のクォーテーションを削除
        if (valueStr.endsWith("\"")) {
            valueStr = valueStr.substring(0, valueStr.length() - 1);
        }

        // 役員数の取得: ItemName="人数" かつ RID="6" (合計値)
        if ("人数".equals(itemName) && "6".equals(ridStr)) {
            executiveCount = safelyParse(valueStr);
        }

        // 従業員数の取得: ItemName="従業員数" かつ RID="13" (合計値)
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

        // DBに書き込む値のログ出力 (NULLの場合も表示)
        String execCountStr = (executiveCount != null) ? executiveCount.toString() : "NULL";
        String empCountStr = (employeeCount != null) ? employeeCount.toString() : "NULL";

        try {
            // DBに登録（主キー '0'、NULL許容で値を登録）
            jdbcTemplate.update(INSERT_SQL, "0", executiveCount, employeeCount);

            System.out.println("DB登録成功！役員数:" + execCountStr + ", 従業員数:" + empCountStr);

        } catch (Exception e) {
            System.err.println("DB接続エラー: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            // 登録後、一時保存した値をリセット
            executiveCount = null;
            employeeCount = null;
        }
    }

    private Integer safelyParse(String valueStr) {
        if (valueStr == null) return null;

        // 数字(0-9)以外をすべて削除（カンマ、記号など）
        String cleaned = valueStr.replaceAll("[^0-9]", "");

        if (cleaned.isEmpty()) return null;

        try {
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
