package com.example.felidadae.rosetus;

/* interface to communicate UI with looper */
public interface LooperControlerUIInterface {
    /* state where you can click the UI and it will perfom action */
    public void enable(boolean ifAnimate);

    /* hide; make it unusable; for instance you cannot overdube if there is no loop */
    public void disable(boolean ifAnimate);

    /* showing progress of performing the main task given; e.g. recording, overdubing */
    public void makeActive(boolean ifAnimate);

    /* indicate last */
    public void indicateLast(boolean ifAnimate);

    /* indicate waiting time (waiting for first note to start recording) */
    public void indicateWaiting(boolean ifAnimate);
}
