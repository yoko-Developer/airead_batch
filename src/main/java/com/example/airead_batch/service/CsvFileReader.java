package com.example.airead_batch.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * AIReadがCSVを作ったときに呼ばれるバッチ
 * AIReadから1つだけパスが渡される
 * このクラスはそのCSVを読み、1行ずつdataWriterServiceへ渡す。
 */
@Component
public class CsvFileReader implements CommandLineRunner {

    private final DataWriterService dataWriterService;

    /**
     * データベースに書くサービスをもらう（Springが自動で入れてくれる）
     */
    public CsvFileReader(DataWriterService dataWriterService) {
        this.dataWriterService = dataWriterService;
    }

    /**
     * バッチが起動されたときに動くメソッド。
     * @param args 0番目にAIReadが渡した「新しくできたCSVファイルのフルパス」が入る
     */
    @Override
    public void run(String[] args) throws Exception {

        if (args.length < 1) {
            System.out.println("CSVファイルが渡されていません。");
            System.exit(1);
        }

        String filePath = args[0];
        File file = new File(filePath);

        if (!file.exists() || file.isDirectory()) {
            System.out.println("ファイルが見つかりません: " + filePath);
            System.exit(1);
        }

        System.out.println("処理開始: " + file.getName());

        // ここが大事！この1行がないとDBに一切入らない
        processSingleFile(filePath);

        // 最後にDBへ確定書き込み
        dataWriterService.writeDataToDbFinal();

        System.out.println("処理完了");
        System.exit(0);
    }

    /**
     * 渡されたCSVファイルを1行ずつ読むメソッド。
     * @param filePath CSVファイルのパス
     */
    private void processSingleFile(String filePath) throws IOException {

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            br.readLine(); // ヘッダをスキップ

            String line;
            while ((line = br.readLine()) != null) {

                String tempLine = line.trim();

                // 行の最初と最後が " の場合は取る
                if (tempLine.startsWith("\"") && tempLine.endsWith("\"")) {
                    tempLine = tempLine.substring(1, tempLine.length() - 1);
                }

                // "," で区切る
                String[] rawData = tempLine.split("\",\"");

                // 1行分のデータをdataWriterServiceに渡す
                dataWriterService.processCsvRow(rawData);
            }
        }
    }
}
