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
     * @param args AIReadから渡される新規CSVファイルのパス
     * @throws Exception ファイル入出力の例外処理
     */
    @Override
    public void run(String[] args) throws Exception {

        if (args.length < 1) {
            // AIReadからファイルパスが渡されなかった場合はエラー終了
            // (単体テスト時は、実行構成の引数にテスト用CSVパスを設定する必要があります)
            System.out.println("エラー: 処理対象ファイルが指定されていません。INI設定または実行引数を確認してください。");
            System.exit(1);
        }

        // AIReadから渡された新規ファイルパスを取得
        String filePath = args[0];

        File file = new File(filePath);
        if (!file.exists() || file.isDirectory()) {
            System.out.println("エラー: 渡されたファイルが見つかりません: " + filePath);
            System.exit(1);
        }

        // 全ファイルスキャンは廃止し、渡されたファイル一つだけを処理
        System.out.println("--- ファイル処理開始 (新規ファイル): " + file.getName() + " ---");

        try {
            // CSVのすべての行を dataWriterService に渡す
            processSingleFile(filePath);
        } catch (IOException e) {
            System.err.println("ファイル読み込みエラー: " + filePath);
            e.printStackTrace();
        }

        // 全ファイルの処理が完了した後、データをDBに書き込む (新しく渡されたデータでDB登録を実行)
        dataWriterService.writeDataToDbFinal();

        System.out.println("完了");
        System.exit(0);
    }

    private void processSingleFile(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // ヘッダ行スキップ

            String line;
            while ((line = br.readLine()) != null) {

                // 行の最初と最後にある二重引用符を取り除く
                String tempLine = line.trim();
                if (tempLine.startsWith("\"") && tempLine.endsWith("\"")) {
                    tempLine = tempLine.substring(1, tempLine.length() - 1);
                }

                // フィールド間の区切り文字 (",") で分割する
                String[] rawData = tempLine.split("\",\"");

                // データを DataWriterService に渡す
                dataWriterService.processCsvRow(rawData);
            }
        }
    }
}
