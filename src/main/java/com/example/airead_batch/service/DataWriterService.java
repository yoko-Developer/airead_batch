package com.example.airead_batch.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * CSVの行から必要な値（役員数・従業員数）を取り出してデータベースに書き込むServiceクラス
 * CSVを複数ファイルから読み取った後で writeDataToDbFinal()を呼び
 * まとめてDBにINSERTする（欠損値は0置換）
 */
@Service
public class DataWriterService {

    private final JdbcTemplate jdbcTemplate;

    // 一時保管
    private static Integer executiveCount = null;
    private static Integer employeeCount = null;

    private static final String INSERT_SQL =
            "INSERT INTO non_consolidated_notes (dummy_id, year_end_executive_count, year_end_employee_count) " +
                    "VALUES (?, ?, ?)";

    public DataWriterService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * CSVno
     * 1行を受け取り、必要な値を抽出してメモリに保持する
     * @param data CSV を split した配列
     */
    public void processCsvRow(String[] data) {
        if (data == null || data.length < 5) {
            return;
        }

        String itemName = data[0] != null ? data[0].trim() : "";
        String valueStr = data[4] != null ? data[4].trim() : "";

        // 役員数（CSV のラベルが「人数」で出ているケースを先生が使っているようならそのまま）
        if ("人数".equals(itemName)) {
            executiveCount = safelyParse(valueStr);
            return;
        }

        // 従業員数
        if ("従業員数".equals(itemName)) {
            employeeCount = safelyParse(valueStr);
        }
    }

    /**
     * すべてのCSVの読み込みが終わった後に呼びだす
     * 役員数・従業員数どちらかがnullの場合は0補完
     */
    public void writeDataToDbFinal() {
        if (executiveCount == null && employeeCount == null) {
            System.out.println("DB登録に必要な項目がないためスキップ");
            return;
        }

        int exec = (executiveCount != null) ? executiveCount : 0;
        int emp = (employeeCount != null) ? employeeCount : 0;

        try {
            jdbcTemplate.update(INSERT_SQL, "0", exec, emp);
            System.out.println("DB登録成功！役員数:" + exec + ", 従業員数:" + emp);
        } catch (Exception e) {
            System.err.println("DB書き込みエラー: " + e.getMessage());
        } finally {
            // リセットして次の帳票に備える
            executiveCount = null;
            employeeCount = null;
        }
    }

    /**
     * 数字以外を取り除いてIntegerにして失敗時は0を返す
     */
    private Integer safelyParse(String valueStr) {
        if (valueStr == null) return 0;
        String cleaned = valueStr.trim().replaceAll("[^0-9]", "");
        if (cleaned.isEmpty()) return 0;
        try {
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
