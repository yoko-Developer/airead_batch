package com.example.airead_batch.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@Component
public class CsvFileReader implements CommandLineRunner {

    private final DataWriterService dataWriterService;

    public CsvFileReader(DataWriterService dataWriterService) {
        this.dataWriterService = dataWriterService;
    }

    @Override
    public void run(String[] args) throws Exception {

        if (args.length < 1) {
            System.out.println("CSVファイルが渡されていません。");
            return;
        }

        String filePath = args[0];
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("ファイルが見つかりません: " + filePath);
            return;
        }

        System.out.println("処理開始: " + file.getAbsolutePath());

        processSingleFile(file.getAbsolutePath());

        dataWriterService.writeDataToDbFinal();

        System.out.println("処理完了");
    }

    private void processSingleFile(String filePath) throws IOException {

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            // ヘッダスキップ
            br.readLine();

            String line;
            while ((line = br.readLine()) != null) {

                if (line.isBlank()) continue;

                // 両端の " を削除
                if (line.startsWith("\"") && line.endsWith("\"")) {
                    line = line.substring(1, line.length() - 1);
                }

                // ," で区切る
                String[] cols = line.split("\",\"");

                dataWriterService.processCsvRow(cols);
            }
        }
    }
}
