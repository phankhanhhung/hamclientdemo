package com.hamhamham.client;

public class MockHamRadioClient implements HamRadioClient {
    private String transmissionFrequency;
    private String receptionFrequency;
    private int transmissionPower;
    private int antennaGain;
    private boolean isConnected = false;

    // Buffer có thể lưu nhiều hơn 3 giây dữ liệu để tránh lỗi "Buffer is full"
    private byte[] receivedBuffer = new byte[40000]; // Đủ để lưu 5 giây dữ liệu
    private int bufferIndex = 0; // Vị trí ghi vào buffer khi transmit
    private int currentIndex = 0; // Vị trí đọc khi receive
    private final int dataPer10ms = 400; // 10ms của âm thanh 8000Hz là 400 mẫu dữ liệu
    private boolean hasData = false; // Kiểm tra xem đã nhận dữ liệu qua transmit chưa

    @Override
    public void connect(String frequency) {
        this.transmissionFrequency = frequency;
        this.receptionFrequency = frequency;
        isConnected = true;
        System.out.println("Connected to frequency: " + frequency);
    }

    @Override
    public void transmit(byte[] signalData) {
        if (isConnected && bufferIndex + signalData.length <= receivedBuffer.length) {
            // Ghi dữ liệu vào buffer
            System.arraycopy(signalData, 0, receivedBuffer, bufferIndex, signalData.length);
            bufferIndex += signalData.length;
            System.out.println("Data transmitted and stored in buffer.");
            hasData = true;  // Đánh dấu rằng đã có dữ liệu trong buffer
        } else {
            System.out.println("Buffer is full or not connected.");
        }
    }

    @Override
    public byte[] receive() {
        if (isConnected && hasData) {
            // Kiểm tra và trả về 10ms tín hiệu tại mỗi lần nhận
            if (currentIndex + dataPer10ms <= bufferIndex) {  // bufferIndex lưu vị trí của dữ liệu có hiệu lực
                byte[] signalData = new byte[dataPer10ms];
                System.arraycopy(receivedBuffer, currentIndex, signalData, 0, dataPer10ms);
                currentIndex += dataPer10ms;

                // Thêm nhiễu vào dữ liệu nhận
                for (int i = 0; i < signalData.length; i++) {
                    signalData[i] += (byte) (Math.random() * 10); // Thêm nhiễu nhẹ (10% cường độ)
                }

                return signalData;
            } else {
                // Nếu đã đọc hết dữ liệu có trong buffer
                System.out.println("All data has been received.");
                return null;
            }
        } else {
            System.out.println("No data to receive or not connected.");
            return null;
        }
    }

    @Override
    public void disconnect() {
        isConnected = false;
        System.out.println("Disconnected from frequency: " + transmissionFrequency);
        transmissionFrequency = null;
        receptionFrequency = null;
        reset();
    }

    @Override
    public void setTransmissionFrequency(String frequency) {
        this.transmissionFrequency = frequency;
    }

    @Override
    public void setReceptionFrequency(String frequency) {
        this.receptionFrequency = frequency;
    }

    @Override
    public void setTransmissionPower(int power) {
        this.transmissionPower = power;
    }

    @Override
    public void setAntennaGain(int gain) {
        this.antennaGain = gain;
    }

    // Hàm để reset trạng thái sau mỗi vòng lặp
    public void reset() {
        bufferIndex = 0;
        currentIndex = 0;
        hasData = false;
        System.out.println("Mock has been reset.");
    }
}
