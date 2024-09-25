package com.hamhamham.client;

public interface HamRadioClient {
    void connect(String frequency);
    void transmit(byte[] signalData); // Gửi chuỗi data mẫu
    byte[] receive(); // Nhận chuỗi data mẫu
    void disconnect();

    // Thêm các phương thức mới
    void setTransmissionFrequency(String frequency);
    void setReceptionFrequency(String frequency);
    void setTransmissionPower(int power); // Cường độ phát
    void setAntennaGain(int gain); // Độ lợi anten
}

