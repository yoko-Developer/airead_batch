package com.example.airead_batch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataWriterService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
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
            int result = jdbcTemplate.update(
                    INSERT_SQL,
                    data[0].trim(),
                    data[1].trim(),
                    data[2].trim()
            );

            // if (result > 0){} // 成功時の処理

        } catch (Exception e) {
            System.out.println("DB書き込み中にエラー発生: " + e.getMessage());
        }
    }



}
