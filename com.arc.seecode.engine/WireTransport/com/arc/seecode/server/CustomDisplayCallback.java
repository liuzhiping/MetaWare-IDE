/*
 * WARNING!
 * This file was auto-generated from com.arc.seecode.engine.gen.GenDisplayCallback
 * DO NOT MODIFY BY HAND!
 */

package com.arc.seecode.server;

import com.arc.seecode.engine.ICustomDisplayCallback;
import com.arc.seecode.scwp.ScwpCommandPacket;
import com.arc.seecode.scwp.ScwpReplyPacket;
import java.io.IOException;
import com.arc.seecode.connect.IConnection;
import com.arc.seecode.connect.TimeoutException;
import com.arc.seecode.connect.VMDisconnectedException;

public class CustomDisplayCallback extends AbstractObserver implements ICustomDisplayCallback {

    private IConnection mConnection;

    /**
     */
    public CustomDisplayCallback(CallbackThread thread, IConnection connection, int pid) { 
        super(thread,pid*ScwpCommandPacket.REQUIRED_CHANNELS+ScwpCommandPacket.CUSTOM_DISPLAY_MONITOR);
        mConnection = connection;
    }

    @Override
    public void createDisplay(String properties, int id) {
        Object[] args = new Object[]{
                properties,
                new Integer(id)};
        this.dispatch("createDisplay",args);
    }

    @Override
    public void updateComponent(int displayID, String properties) {
        Object[] args = new Object[]{
                new Integer(displayID),
                properties};
        this.dispatch("updateComponent",args);
    }

    @Override
    public void updateComponentData(int displayID, String componentID, String data) {
        Object[] args = new Object[]{
                new Integer(displayID),
                componentID,
                data};
        this.dispatch("updateComponentData",args);
    }

    @Override
    public void setLine(int id, int lineNumber, String line) {
        Object[] args = new Object[]{
                new Integer(id),
                new Integer(lineNumber),
                line};
        this.dispatch("setLine",args);
    }

    @Override
    public void setBlanks(int id, int lineNumber, int cnt) {
        Object[] args = new Object[]{
                new Integer(id),
                new Integer(lineNumber),
                new Integer(cnt)};
        this.dispatch("setBlanks",args);
    }

    @Override
    public void setHighlight(int id, int lineNumber, int highlightValue) {
        Object[] args = new Object[]{
                new Integer(id),
                new Integer(lineNumber),
                new Integer(highlightValue)};
        this.dispatch("setHighlight",args);
    }

    @Override
    public void removeHighlights(int id, int highlight) {
        Object[] args = new Object[]{
                new Integer(id),
                new Integer(highlight)};
        this.dispatch("removeHighlights",args);
    }

    @Override
    public void createUserDisplay(int id, String guihiliSpec) {
        Object[] args = new Object[]{
                new Integer(id),
                guihiliSpec};
        this.dispatch("createUserDisplay",args);
    }

    @Override
    public void setTimerUpdate(int id, int millisec) {
        Object[] args = new Object[]{
                new Integer(id),
                new Integer(millisec)};
        this.dispatch("setTimerUpdate",args);
    }

    @Override
    public void doChangeDialog(int id, String name, String old, String cmd) {
        Object[] args = new Object[]{
                new Integer(id),
                name,
                old,
                cmd};
        this.dispatch("doChangeDialog",args);
    }

    @Override
    public void scroll(int id, int amount, int displaySize) {
        Object[] args = new Object[]{
                new Integer(id),
                new Integer(amount),
                new Integer(displaySize)};
        this.dispatch("scroll",args);
    }

    @Override
    public void setScrollBar(int id, int current, int displaySize, int first, int last, int blockIncrement) {
        Object[] args = new Object[]{
                new Integer(id),
                new Integer(current),
                new Integer(displaySize),
                new Integer(first),
                new Integer(last),
                new Integer(blockIncrement)};
        this.dispatch("setScrollBar",args);
    }

    @Override
    public void setStatus(int id, String msg) {
        Object[] args = new Object[]{
                new Integer(id),
                msg};
        this.dispatch("setStatus",args);
    }

    @Override
    public void displayErrorBox(int id, String msg) {
        Object[] args = new Object[]{
                new Integer(id),
                msg};
        this.dispatch("displayErrorBox",args);
    }

    @Override
    public void clear(int id) {
        Object[] args = new Object[]{
                new Integer(id)};
        this.dispatch("clear",args);
    }

    @Override
    public void createProgressDisplay(int id, String properties) {
        Object[] args = new Object[]{
                new Integer(id),
                properties};
        this.dispatch("createProgressDisplay",args);
    }

    @Override
    public void destroyProgressDisplay(int id) {
        Object[] args = new Object[]{
                new Integer(id)};
        this.dispatch("destroyProgressDisplay",args);
    }

    @Override
    public void setProgressPercentage(int id, int percent) {
        Object[] args = new Object[]{
                new Integer(id),
                new Integer(percent)};
        this.dispatch("setProgressPercentage",args);
    }

    @Override
    public void doOptionsWindow(int displayID, int gui_id, String props) {
        Object[] args = new Object[]{
                new Integer(displayID),
                new Integer(gui_id),
                props};
        this.dispatch("doOptionsWindow",args);
    }

    @Override
    public void setTitle(int id, String title) {
        Object[] args = new Object[]{
                new Integer(id),
                title};
        this.dispatch("setTitle",args);
    }

    @Override
    public void refreshDisplaysMenu() {
        Object[] args = new Object[]{};
        this.dispatch("refreshDisplaysMenu",args);
    }

    @Override
    public void updateViews() {
        Object[] args = new Object[]{};
        this.dispatch("updateViews",args);
    }

    @Override
    public int chooseFromList(String prompt, String[] list) {
        Object[] args = new Object[]{
                prompt,
                list};
        ScwpCommandPacket cmd = this.makePacket("chooseFromList",args);
        try {
            ScwpReplyPacket result = mConnection.sendCommand(cmd);
            return result.dataInStream().readInt();
        }
        catch(TimeoutException e){
            return -1;
        }
        catch(VMDisconnectedException e){
            return -1;
        }
        catch(IOException e){
            return -1;
        }
    }

    @Override
    public void deleteDisplay(int id) {
        Object[] args = new Object[]{
                new Integer(id)};
        this.dispatch("deleteDisplay",args);
    }

    @Override
    public void refreshDisplay(int id) {
        Object[] args = new Object[]{
                new Integer(id)};
        this.dispatch("refreshDisplay",args);
    }

    @Override
    public void setProfileData(int id, int column, String data) {
        Object[] args = new Object[]{
                new Integer(id),
                new Integer(column),
                data};
        this.dispatch("setProfileData",args);
    }

    @Override
    public void show(int id) {
        Object[] args = new Object[]{
                new Integer(id)};
        this.dispatch("show",args);
    }

    @Override
    public void typeText(int id, String componentName, String text, boolean tail) {
        Object[] args = new Object[]{
                new Integer(id),
                componentName,
                text,
                new Boolean(tail)};
        this.dispatch("typeText",args);
    }

    @Override
    public void queueCommand(String command) {
        Object[] args = new Object[]{
                command};
        this.dispatch("queueCommand",args);
    }

    @Override
    public void setDisplaySelectors(String[] selectors) {
        Object[] args = new Object[]{
                selectors};
        this.dispatch("setDisplaySelectors",args);
    }

    @Override
    public void setProperty(String key, String value) {
        Object[] args = new Object[]{
                key,
                value};
        this.dispatch("setProperty",args);
    }

    @Override
    public void animate(int tid, String cmd) {
        Object[] args = new Object[]{
                new Integer(tid),
                cmd};
        this.dispatch("animate",args);
    }

    @Override
    public void onLicenseRequestStart(int timeoutMillis) {
        Object[] args = new Object[]{
                new Integer(timeoutMillis)};
        this.dispatch("onLicenseRequestStart",args);
    }

    @Override
    public void onLicenseRequestEnd(boolean granted) {
        Object[] args = new Object[]{
                new Boolean(granted)};
        this.dispatch("onLicenseRequestEnd",args);
    }

    @Override
    public String promptForFile(String props) {
        Object[] args = new Object[]{
                props};
        ScwpCommandPacket cmd = this.makePacket("promptForFile",args);
        try {
            ScwpReplyPacket result = mConnection.sendCommand(cmd);
            return result.dataInStream().readUTF();
        }
        catch(TimeoutException e){
            return null;
        }
        catch(VMDisconnectedException e){
            return null;
        }
        catch(IOException e){
            return null;
        }
    }

    @Override
    public void showSplash(String imagePath, String props) {
        Object[] args = new Object[]{
                imagePath,
                props};
        this.dispatch("showSplash",args);
    }

    @Override
    public void showHelp(String props) {
        Object[] args = new Object[]{
                props};
        this.dispatch("showHelp",args);
    }

    @Override
    public void copyToClipboard(String s) {
        Object[] args = new Object[]{
                s};
        this.dispatch("copyToClipboard",args);
    }

    @Override
    public void copyVisibleToClipboard(int id) {
        Object[] args = new Object[]{
                new Integer(id)};
        this.dispatch("copyVisibleToClipboard",args);
    }

    @Override
    public void setSelection(int id, int line1, int col1, int line2, int col2) {
        Object[] args = new Object[]{
                new Integer(id),
                new Integer(line1),
                new Integer(col1),
                new Integer(line2),
                new Integer(col2)};
        this.dispatch("setSelection",args);
    }

    @Override
    public void selectStackframe(int threadID, int stacklevel) {
        Object[] args = new Object[]{
                new Integer(threadID),
                new Integer(stacklevel)};
        this.dispatch("selectStackframe",args);
    }

    @Override
    public void recordInitState(int id, String state) {
        Object[] args = new Object[]{
                new Integer(id),
                state};
        this.dispatch("recordInitState",args);
    }
}
