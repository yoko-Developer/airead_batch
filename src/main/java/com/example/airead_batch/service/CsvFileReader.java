package com.example.airead_batch.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.imageio.IIOException;
import java.io.BufferedReader;
import java.io.FileReader;

@Component
public class CsvFileReader implements CommandLineRunner {

    private final DataWriterService dataWriterService;

    public CsvFileReader(DataWriterService dataWriterService) {
        this.dataWriterService = dataWriterService;
    }

    @Override
    public void run(String[] args) throws Exception {

        if (args.length == 0) {
            System.err.println("csvファイルのパスの指定が違う");
            System.err.println("後処理設定でファイルパスを渡すように設定されていない");
            System.exit(1); // エラー終了コード
            return;
        }

        String filePath = args[0];
        System.out.println("csv読込開始");

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // ヘッダ行スキップ
            br.readLine();

            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                dataWriterService.writeDataToDb(data);
            }
            System.out.println("csv処理完了");
        } catch (IIOException e) {
            System.err.println("ファイルが見つからない又は読み込みエラー" + filePath);
            e.printStackTrace();
        }
        System.exit(0);
    }
}
