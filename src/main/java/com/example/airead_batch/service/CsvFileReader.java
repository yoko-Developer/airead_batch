package com.example.airead_batch.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.*;

/**
 * CSVフォルダ（C:\AIRead\output）内のすべてのCSVファイルを読み込み、
 * 1行ずつ DataWriterService に渡して値を抽出し、最終的にDB登録を実行する。
 */
@Component
public class CsvFileReader implements CommandLineRunner {

    private final DataWriterService dataWriterService;
    private static final String INPUT_DIR = "C:\\AIRead\\output"; // 処理対象フォルダ

    public CsvFileReader(DataWriterService dataWriterService) {
        this.dataWriterService = dataWriterService;
    }

    @Override
    public void run(String[] args) throws Exception {

        File directory = new File(INPUT_DIR);

        if (!directory.isDirectory()) {
            System.out.println("Error: 処理対象ディレクトリが見つからない: " + INPUT_DIR);
            System.exit(1);
            return;
        }

        File[] csvFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));

        if (csvFiles == null || csvFiles.length == 0) {
            System.out.println("Error: 処理対象のCSVファイルが見つからない");
            return;
        }

        System.out.println("全CSVファイル処理開始");

        // 全ファイルをループして処理し、値を DataWriterService に累積させる
        for (File file : csvFiles) {
            System.out.println("処理中ファイル: " + file.getName());

            try {
                processSingleFile(file.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("ファイル読み込みエラー: " + file.getName());
                e.printStackTrace();
            }
        }

        // 全ファイルの処理が完了した後、集めたデータで一度だけ DB 登録を実行
        dataWriterService.writeDataToDbFinal();

        System.out.println("全CSVファイル処理完了");
    }

    private void processSingleFile(String filePath) throws IOException {

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), "UTF-8"))) {

            // ヘッダスキップ
            br.readLine();

            String line;
            while ((line = br.readLine()) != null) {

                if (line.isBlank()) continue;

                String tempLine = line.trim();

                // 両端の " を削除（AIReadのCSV形式対応）
                if (tempLine.startsWith("\"") && tempLine.endsWith("\"")) {
                    tempLine = tempLine.substring(1, tempLine.length() - 1);
                }

                // ," で区切る（AIReadのCSV形式対応）
                String[] cols = tempLine.split("\",\"");

                dataWriterService.processCsvRow(cols);
            }
        }
    }
}