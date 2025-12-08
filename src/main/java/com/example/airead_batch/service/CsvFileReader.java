package com.example.airead_batch.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.imageio.IIOException;
import java.io.BufferedReader;
import java.io.FileReader;

@Component
public class CsvFileReader implements CommandLineRunner {

    private static final String CSV_FILE_PATH = "C:\\AIRead\\output\\ai_read_result.csv";
    private final DataWriterService dataWriterService;

    public CsvFileReader(DataWriterService dataWriterService) {
        this.dataWriterService = dataWriterService;
    }

    @Override
    public void run(String[] args) throws Exception {
        System.out.println("csv読込開始");

        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE_PATH))) {
            // ヘッダ行スキップ
            br.readLine();

            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                dataWriterService.writeDataToDb(data);
            }
            System.out.println("csv処理完了");
        } catch (IIOException e) {
            System.err.println("ファイルが見つからない又は読み込みエラー" + CSV_FILE_PATH);
            e.printStackTrace();
        }
        System.exit(0);
    }
}
