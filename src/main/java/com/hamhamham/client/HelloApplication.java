package com.hamhamham.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {


        Scanner scanner = new Scanner(System.in);

        // Lặp vô hạn cho đến khi người dùng thoát
        while (true) {
            HamRadioClient client = new MockHamRadioClient();

            // Kết nối đến tần số
            client.connect("101.5 MHz");
            // Đợi người dùng nhấn Enter để bắt đầu ghi âm
            System.out.println("Press ENTER to start recording for 3 seconds (type 'exit' to quit)...");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("exit")) {
                break;  // Thoát vòng lặp nếu người dùng nhập 'exit'
            }

            // Ghi âm 3 giây
            byte[] recordedData = recordAudio(3);

            if (recordedData != null) {
                // Gửi dữ liệu âm thanh đã ghi vào client
                client.transmit(recordedData);
                System.out.println("Audio data sent to client!");

                // Tạo thread để xử lý việc nhận và phát tín hiệu từ client
                Thread audioThread = new Thread(() -> {
                    try {
                        playAudioFromClient(client);
                    } catch (LineUnavailableException e) {
                        e.printStackTrace();
                    }
                });

                // Bắt đầu phát tín hiệu ra loa
                audioThread.start();

                // Đợi thread phát tín hiệu kết thúc trước khi lặp lại
                try {
                    audioThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            client.disconnect();
        }

        // Ngắt kết nối và dừng chương trình

        System.out.println("Client disconnected. Exiting...");
    }

    // Hàm để ghi âm trong thời gian xác định (seconds)
    private static byte[] recordAudio(int seconds) {
        AudioFormat format = new AudioFormat(8000, 8, 1, true, true); // 8000Hz, 8-bit, Mono
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        try {
            TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            long endTime = System.currentTimeMillis() + seconds * 1000;

            System.out.println("Recording...");

            while (System.currentTimeMillis() < endTime) {
                int bytesRead = microphone.read(buffer, 0, buffer.length);

                // Tăng cường độ âm thanh trước khi ghi vào ByteArrayOutputStream
                for (int i = 0; i < bytesRead; i++) {
                    buffer[i] = (byte) Math.min((buffer[i] * 2), Byte.MAX_VALUE); // Tăng gấp đôi cường độ tín hiệu
                }

                out.write(buffer, 0, bytesRead);
            }

            microphone.stop();
            microphone.close();

            System.out.println("Recording complete!");

            return out.toByteArray();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void playAudioFromClient(HamRadioClient client) throws LineUnavailableException {
        // Thiết lập thông số âm thanh
        AudioFormat format = new AudioFormat(8000, 8, 1, true, true); // 8000Hz, 8-bit, Mono
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        // Mở dòng dữ liệu âm thanh
        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();

        // Phát liên tục dữ liệu âm thanh mỗi 10ms
        while (true) {
            byte[] signalData = client.receive();
            if (signalData != null) {
                line.write(signalData, 0, signalData.length);
            } else {
                break; // Dừng khi không còn tín hiệu
            }

            // Dừng 10ms trước khi nhận tiếp dữ liệu
            try {
                Thread.sleep(46); // Giả lập thời gian nhận 10ms mỗi lần
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Kết thúc phát âm thanh
        line.drain();
        line.close();
    }
}
