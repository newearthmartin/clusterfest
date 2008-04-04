package com.flaptor.clusterfest.action;

/**
 * subclass this to receive actions
 * 
 * @author marto
 */
public abstract class ActionReceiverImplementation implements ActionReceiver{

    public boolean ping() throws Exception {
        return true;
    }

    public abstract void action(String action, Object[] params) throws Exception;
}
