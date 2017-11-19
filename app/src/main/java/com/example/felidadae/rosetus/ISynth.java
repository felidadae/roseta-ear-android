package com.example.felidadae.rosetus;

public interface ISynth {
    public void start(int sample_rate, int buf_size);

    public void attackNote(int positionX, int positionY);
    public void releaseNote(int positionX, int positionY);

    public void bendNote(int positionX, int positionY, float bendingIndexX, float bendingIndexY);
    public void unbendNote(int positionX, int positionY);
}
