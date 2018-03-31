/*

*/
package com.example.felidadae.rosetus;


import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.Semaphore;


/* private class; used only in this file; */
class LooperMemoryItem {
    /* Looper event info */
    public LooperEventType event_type; //ATTACK, REALEASE, BREAK etc..
    public int x,y;
    public float xBending, yBending; /* overdub */  

    /* in miliseconds; time 0 is the time of first event in memory; property of memory */
    public long normalized_time_event; 

    /* in miliseconds; time which passed from previous event in memory; property of memory */
    public long delta_time_from_previous_event;

    /* to undo overdub history */
    public int iteration_creation;

    public LooperMemoryItem() {}
    public LooperMemoryItem(LooperMemoryItem other) {
        this.event_type = other.event_type;
        this.x = other.x; this.y = other.y;
        this.xBending = other.xBending; this.yBending = other.yBending;
        this.normalized_time_event = other.normalized_time_event;
        this.delta_time_from_previous_event = other.delta_time_from_previous_event;
        this.iteration_creation = other.iteration_creation;
    }

    @Override
    public String toString() {
        return String.format("(x=%d, y=%d, event_type=%s, delta_time_from_previous_event=%d) ", x, y, event_type, delta_time_from_previous_event);
    }
}


/* private class; used only in this file */
class LooperMemory {
    /* if to be clear; */
    public AtomicBoolean is_to_be_clear = new AtomicBoolean(false);

    /* list of events; memory of the looper;  */
    public List<LooperMemoryItem> memory = new ArrayList<LooperMemoryItem>();

    /* how many items in memory */
    public int getN() { return memory.size(); }  

    /* length of loop in miliseconds */
    public long getL() { return L; }

    /* reset to initial state the object */
    public void clear() {
        this.memory.clear();
        this.L = 0;
        this.last_event_absolute_time = -1;
    }

    /* add new event to looper memory */
    public void add(int x, int y, LooperEventType event_type, long begin) { 
        this.add(x,y, event_type, begin, 0.0f, 0.0f);
    }
    public void add(int x, int y, LooperEventType event_type, long begin, float xBending, float yBending) {
        if (is_to_be_clear.get()) {
            this.clear();
            is_to_be_clear.set(false);
        }

        LooperMemoryItem item = new LooperMemoryItem();
        item.x = x; item.y = y; item.event_type = event_type;
        item.xBending = xBending; item.yBending = yBending;
        Long absolute_time_event = System.currentTimeMillis();

        if (memory.size() == 0) {
            if (begin == -1) {
                item.delta_time_from_previous_event = 0;
                item.normalized_time_event = 0;
            }
            else {
                item.delta_time_from_previous_event = absolute_time_event - begin;
                item.normalized_time_event = absolute_time_event - begin;
            }
        }
        else {
            LooperMemoryItem last = memory.get(memory.size() - 1);
            item.delta_time_from_previous_event = absolute_time_event - this.last_event_absolute_time;
            item.normalized_time_event = this.L + item.delta_time_from_previous_event;
        }
        memory.add(item);
        this.last_event_absolute_time = absolute_time_event;
        this.L += item.delta_time_from_previous_event;
    }

    public void add(LooperMemoryItem memoryItem) {
        this.L += memoryItem.delta_time_from_previous_event;
        this.memory.add(memoryItem);
    }

    @Override
    public String toString() {
        String result = new String("");
        for (LooperMemoryItem item: memory) { result += item.toString(); }
        return result;
    }

    private long L = 0;
    private long last_event_absolute_time = -1;
}


public class Looper {
    public Context context;
    private AtomicReference<State> state = new AtomicReference<State>();
    private AtomicLong time_begin_iteration = new AtomicLong();
    private AtomicLong time_overdub_begin = new AtomicLong();
    private Semaphore state_changing_logic_semaphore = new Semaphore(1, true);

    private LooperControlerUIInterface recordControler, overdubControler, undoControler; // UI buttons
    public void setRecordControler(LooperControlerUIInterface recordControler)   { this.recordControler  = recordControler; }
    public void setOverdubControler(LooperControlerUIInterface overdubControler) { this.overdubControler = overdubControler; }
    public void setUndoControler(LooperControlerUIInterface undoControler)       { this.undoControler    = undoControler; }
    
    private LooperMemory mainMemory    = new LooperMemory();
    private LooperMemory overdubMemory = new LooperMemory();
    final private ISynth synthDelegate;

    public Looper(Context context, ISynth synthDelegate) {
        this.synthDelegate = synthDelegate;
        state.set(State.OFF);
        this.context = context;
    }
    private enum State {
        OFF, RECORD, OVERDUB, PLAYBACK, PLAYBACK_LAST, OVERDUB_NOT_MIXED
    }

    private void logLooperGeneric(String s) {
        Log.d("LooperEvent", String.format(s));
    }
    private void logLooper() {
        Log.d("LooperEvent", 
            String.format("State of looper %s (main: %d) (overdub: %d)", 
                state.get(), mainMemory.memory.size(), overdubMemory.memory.size()));
    }
    private void logLooper_memory(LooperMemory m) {
        Log.d("LooperEvent", String.format("State of memory %d", m.memory.size()));
    }
    private void logLooper_play() {
        Log.d("LooperEvent", String.format("Looper will now play from its memory to synthDelegate %s", synthDelegate));
    }
    
    public void toggle_state(ControlerType input) {
        /* Function to change looper state by user input */

        /* (*A*) the state can be also changed from inside play-thread;
        so we need semaphore to assure that state will not be changed during execution blocks of code; */
        state_changing_logic_semaphore.acquireUninterruptibly();

        /* Button RECORD */
        if (input == ControlerType.LOOPER_RECORD) {
            if (this.state.get() == State.PLAYBACK) {
                this.state.set(State.PLAYBACK_LAST);
                this.recordControler.disable(true);
                this.overdubControler.disable(true);
                /* @TODO */ //this.undoControler.disable(true);
            }
            else if (this.state.get() == State.RECORD) {
                //add break after last note and clicking looper button
                mainMemory.add(-1, -1, LooperEventType.PAUSE, 0);

                /* Lets start playing the loop */
                this.state.set(State.PLAYBACK);
                this.recordControler.enable(true);
                this.overdubControler.enable(true);
                this.play();
            }
            else if (this.state.get() == State.OFF) {
                this.state.set(State.RECORD);
                this.recordControler.makeActive(true);
            }
            else if (this.state.get() == State.PLAYBACK_LAST) {
                ; /* do nothing - maybe it should start recording after end of playback ? */
            }
            else if (this.state.get() == State.OVERDUB) {
                ; /* do nothing */
            }
            else if (this.state.get() == State.OVERDUB_NOT_MIXED) {
                ; /* do nothing; maybe it should move to state playback_last ? */
            }
        }
        /* Button OVERDUBE */
        else if (input == ControlerType.LOOPER_OVERDUB) {
            if (this.state.get() == State.PLAYBACK) {
                /* the most obvious functionality; it starts just after touching the button (not waiting for the end of the loop) */
                this.overdubMemory.clear();
                this.state.set(State.OVERDUB);
                this.time_overdub_begin.set(this.time_begin_iteration.get());
                this.overdubMemory.add(0,0, LooperEventType.PAUSE, this.time_overdub_begin.get());
                this.recordControler.disable(true);
                /* undo recorder @TODO */  //this.undoControler.disable(true);
                this.overdubControler.makeActive(true);
            }
            else if (this.state.get() == State.OVERDUB) {
                /* stop overdube, mix and wait for the end of the current loop to replace mainMemory with overdubMemory */

                //add break after last note and clicking looper button
                overdubMemory.add(-1, -1, LooperEventType.PAUSE, 0);

                //mix overdube and main memory and save to overdube var reference;
                //later in play-thread it will be switched on the begin of loop;
                mixMemories();
                this.state.set(State.OVERDUB_NOT_MIXED);
                this.overdubControler.disable(true);
            }
            else if (this.state.get() == State.RECORD) {
                ; /* we should just in that moment finish the loop and start overdubing (and playing ofc the loop in the background) */
            }
            else if (this.state.get() == State.OVERDUB_NOT_MIXED) {
                ; /* do nothing; we could wait until end of the loop, and start overdubing again with new iteration of the loop */
            }
            else if (this.state.get() == State.PLAYBACK_LAST) {
                ; /* what then? just make it unavailable */
            }
            else if (this.state.get() == State.OFF) {
                ; /* cannot overdub empty */
            }
        }
        /* Button UNDO */
        else if (input == ControlerType.LOOPER_UNDO) {
            ;
        }
        /* Here is place to add new buttons */
        // @TODO

        state_changing_logic_semaphore.release(); /* RELEASE SEMAPHORE ->>A */
        logLooper();
    }
    public void play() {
        logLooper_play();
        final Looper looper__ = this;
        new Thread(new Runnable() {
            public void run() {
                state_changing_logic_semaphore.acquireUninterruptibly(); /* ACQUIRE SEMAPHORE */
                State state_ = state.get();
                while (state_ == State.PLAYBACK || state_ == State.OVERDUB || state_ == State.OVERDUB_NOT_MIXED) {
                    if (state_ == State.OVERDUB_NOT_MIXED) {
                        logLooperGeneric("We are inside OVERDUB");
                        LooperMemory tmp = mainMemory;
                        mainMemory = overdubMemory;
                        overdubMemory = tmp;
                        overdubMemory.is_to_be_clear.set(true);
                        state.set(State.PLAYBACK);

                        /* post to main thread - as animations can be only done from main thread */
                        /* https://stackoverflow.com/questions/11123621/running-code-in-main-thread-from-another-thread */
                        Handler mainHandler = new Handler(context.getMainLooper());
                        Runnable myRunnable = new Runnable() {
                            @Override
                            public void run() {
                                looper__.recordControler.enable(true);
                                looper__.overdubControler.enable(true);
                                /* @TODO undo controller */ //looper__.undoControler.enable(true);
                            }
                        };
                        mainHandler.post(myRunnable);
                    }
                    state_changing_logic_semaphore.release(); /* RELEASE SEMAPHORE */

                    time_begin_iteration.set(System.currentTimeMillis());
                    for (LooperMemoryItem memoryItem: mainMemory.memory) {
                        try { Thread.sleep(memoryItem.delta_time_from_previous_event); }
                        catch (InterruptedException e) {;}
                        if (memoryItem.event_type == LooperEventType.ATTACK) {
                            synthDelegate.attackNote(memoryItem.x, memoryItem.y);
                        }
                        else if (memoryItem.event_type == LooperEventType.RELEASE) {
                            synthDelegate.releaseNote(memoryItem.x, memoryItem.y);
                        }
                        else if (memoryItem.event_type == LooperEventType.BEND) {
                            synthDelegate.bendNote( memoryItem.x, memoryItem.y, 
                                memoryItem.xBending, memoryItem.yBending);
                        }
                        else if (memoryItem.event_type == LooperEventType.UNBEND) {
                            synthDelegate.unbendNote(memoryItem.x, memoryItem.y);
                        }
                    }

                    state_changing_logic_semaphore.acquireUninterruptibly(); /* ACQUIRE SEMAPHORE */
                    state_ = state.get();
                }
                if (state_ == State.PLAYBACK_LAST) {
                    state.set(State.OFF);

                    /* post to main thread - as animations can be only done from main thread */
                    /* https://stackoverflow.com/questions/11123621/running-code-in-main-thread-from-another-thread */
                    Handler mainHandler = new Handler(context.getMainLooper());
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {looper__.recordControler.enable(true);}
                    };
                    mainHandler.post(myRunnable);
                }
                mainMemory.clear();
                overdubMemory.clear();
                state_changing_logic_semaphore.release(); /* RELEASE SEMAPHORE */
            }
        }).start();
    }
    public void notifyEvent(int x, int y, LooperEventType event_type) {
        notifyEvent(x,y,event_type,0.0f,0.0f);
    }
    public void notifyEvent(int x, int y, LooperEventType event_type, float xBending, float yBending) {
        if (this.state.get() == State.RECORD) {
            mainMemory.add(x,y,event_type, -1, xBending, yBending);
        }
        else if (this.state.get() == State.OVERDUB) {
            overdubMemory.add(x,y,event_type, this.time_overdub_begin.get(), xBending, yBending);
        }
    }
    private void addFromMain(int i, int ik, int K, long L, int N, LooperMemory m, LooperMemory r) {
        LooperMemoryItem cp = new LooperMemoryItem(m.memory.get(i%N));
        cp.normalized_time_event = cp.normalized_time_event + ik*L;
        if (r.memory.size() > 0) {
            cp.delta_time_from_previous_event = cp.normalized_time_event - r.memory.get(r.memory.size()-1).normalized_time_event;
        } else {
            cp.delta_time_from_previous_event = 0;
        }
        // cp.iteration = -1; // @TODO
        r.add(cp);
    }
    private void addFromOverdube(int i_, LooperMemory m_, LooperMemory r) {
        LooperMemoryItem cp = new LooperMemoryItem(m_.memory.get(i_));
        if (r.memory.size() > 0) {
            cp.delta_time_from_previous_event = cp.normalized_time_event - r.memory.get(r.memory.size()-1).normalized_time_event;
        } else {
            cp.delta_time_from_previous_event = 0;
        }
        // cp.iteration = -1; // @TODO
        r.add(cp);
    }
    private void mixMemories() {
        logLooperGeneric("Just started to mix memories.");
        LooperMemory m  = this.mainMemory;
        LooperMemory m_ = this.overdubMemory;
        int N  = this.mainMemory.getN();
        int N_ = this.overdubMemory.getN();
        long L  = this.mainMemory.getL();
        long L_ = this.overdubMemory.getL();
        int K = (int)(Math.ceil((double)L_/(double)L));
        logLooperGeneric(String.format("N <- %d, N_ <- %d, L <- %d, L_ <- %d, K <- %d", N, N_, L, L_, K));
        LooperMemory r = new LooperMemory();

        int i_ = 0, i = 0;
        while (!(i_ == N_ && i == N*K)) {
            int ik = (int) i/N;
            if (i_ == N_) {
                addFromMain(i, ik, K, L, N, m, r);
                i++;
            }
            else if (i == N*K) {
                addFromOverdube(i_, m_, r);
                i_++;
            }
            else if ((m.memory.get(i%N).normalized_time_event + ik*L) < m_.memory.get(i_).normalized_time_event) {
                addFromMain(i, ik, K, L, N, m, r);
                i++;
            }
            else {
                addFromOverdube(i_, m_, r);
                i_++;
            }
        }

        this.overdubMemory = r;
        logLooper();
        logLooper_memory(r);
        logLooperGeneric("Just finished to mix memories.");
    }
    private void undo() {;}
}
