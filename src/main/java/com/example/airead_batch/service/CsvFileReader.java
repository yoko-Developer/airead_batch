package com.example.airead_batch.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileReader;
import java.io.IOException;

/**
 * AIReadの後処理として実行されるバッチのメインクラス
 * 固定の出力ディレクトリをスキャンし
 * 条件に一致するすべてのCSVファイルを読み込み、DB登録処理をする
 */
@Component
public class CsvFileReader implements CommandLineRunner {

    private final DataWriterService dataWriterService;
    private static final String INPUT_DIR = "C:\\AIRead\\output";

    /**
     * SpringがDIを行う
     * @param dataWriterService DB書き込み
     */
    public CsvFileReader(DataWriterService dataWriterService) {
        this.dataWriterService = dataWriterService;
    }

    /**
     * バッチ起動時に実行されるメソッド
     * @param args 全てのファイルを処理する
     * @throws Exception ファイル入出力の例外処理
     */
    @Override
    public void run(String[] args) throws Exception {
        File folder = new File(INPUT_DIR);

        // _detail.csv を処理
        File[] listOfFiles = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith("_detail.csv");
            }
        });

        if (listOfFiles == null || listOfFiles.length == 0) {
            System.out.println("処理対象のCSVファイルがない");
            System.exit(0);
        }

        System.out.println("対象ファイル数: " + listOfFiles.length + "件");

        for (File file : listOfFiles) {
            if (file.isFile()) {
                String filePath = file.getAbsolutePath();
                System.out.println("--- ファイル処理開始: " + file.getName() + " ---");
                try {
                    // CSVのすべての行を dataWriterService に渡す
                    processSingleFile(filePath);
                } catch (IOException e) {
                    System.err.println("ファイル読み込みエラー: " + filePath);
                    e.printStackTrace();
                }
            }
        }

        // 全ファイルの処理が完了した後、データをDBに書き込む
        dataWriterService.writeDataToDbFinal();

        System.out.println("完了");
        System.exit(0);
    }

    private void processSingleFile(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // ヘッダ行スキップ

            String line;
            while ((line = br.readLine()) != null) {
                String cleanedLine = line.replaceAll("^\"|\"$", "");
                String[] rawData = cleanedLine.split("\",\"");
                // データを DataWriterService に渡す
                dataWriterService.processCsvRow(rawData);
            }
        }
    }
}