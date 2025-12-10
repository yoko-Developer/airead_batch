package com.example.airead_batch.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * AIReadの後処理で呼ばれるバッチのエントリポイント
 * AIReadから渡された「新しく作られた CSV ファイルのフルパス」を受け取り
 * 同じ帳票（baseName）に該当する出力ディレクトリ内のCSVをすべて処理する
 */
@Component
public class CsvFileReader implements CommandLineRunner {

    private final DataWriterService dataWriterService;

    public CsvFileReader(DataWriterService dataWriterService) {
        this.dataWriterService = dataWriterService;
    }

    /**
     * バッチ起動時に1回だけ呼ばれるメソッド
     * @param args コマンドライン引数（args[0] = 対象CSVのフルパス）
     * @throws Exception ファイル入出力の例外
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

        // 同じ帳票（prefix）に該当する CSV をすべて探して処理する
        File parent = file.getParentFile();
        String fileName = file.getName();

        // "_<数字>" 以降を取り除くルール（例: 102_name_0.csv, 102_name_0_detail.csv に対応）
        String base = fileName.replaceFirst("(_\\d+).*", "");

        // CSV を列挙（同じプレフィックスで始まる .csv を処理）
        File[] csvFiles = parent.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(base) && name.toLowerCase().endsWith(".csv");
            }
        });

        if (csvFiles == null || csvFiles.length == 0) {
            System.out.println("該当するCSVが見つかりません: base=" + base + " dir=" + parent.getAbsolutePath());
            System.exit(1);
        }

        // 各CSVを読み込んでサービスに渡す
        for (File csv : csvFiles) {
            System.out.println(" - 読込: " + csv.getName());
            processSingleFile(csv.getAbsolutePath());
        }

        // 全ファイル処理後にDBへ書き込み（役員数・従業員数をまとめて登録）
        dataWriterService.writeDataToDbFinal();

        System.out.println("処理完了");
        System.exit(0);
    }

    /**
     * 指定したCSVを1行ずつ読み取り、DataWriterServiceに渡す
     * @param filePath CSV のフルパス
     * @throws IOException ファイル読み込み例外
     */
    private void processSingleFile(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // ヘッダ行スキップ
            br.readLine();

            String line;
            while ((line = br.readLine()) != null) {
                String tempLine = line.trim();

                // 先頭と末尾が " の場合は削る
                if (tempLine.startsWith("\"") && tempLine.endsWith("\"")) {
                    tempLine = tempLine.substring(1, tempLine.length() - 1);
                }

                // フィールドを分割
                String[] rawData = tempLine.split("\",\"");

                dataWriterService.processCsvRow(rawData);
            }
        }
    }
}
