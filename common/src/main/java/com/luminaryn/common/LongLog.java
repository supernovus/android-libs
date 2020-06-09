package com.luminaryn.common;

import java.util.ArrayList;
import android.util.Log;

/**
 * A simple wrapper around Log which can split really long logs that would normally
 * be truncated into multiple parts and output the parts individually.
 *
 * This has no support for passing Throwables, and doesn't have the wtf() level logs.
 * Use the main Log class if you need those.
 */
public class LongLog {
    static final int MAXLEN = 4000;

    static final int VERBOSE = Log.VERBOSE;
    static final int DEBUG   = Log.DEBUG;
    static final int INFO    = Log.INFO;
    static final int WARN    = Log.WARN;
    static final int ERROR   = Log.ERROR;
    static final int ASSERT  = Log.ASSERT;

    /**
     * Override this in a subclass to have a default tag.
     */
    static String TAG() { return null; }

    /**
     * Override this in a subclass to change the default showPager value.
     */
    static boolean SHOW_PAGER() { return true; }

    /**
     * Override this in a subclass to change the default showLength value.
     */
    static boolean SHOW_LENGTH() { return false; }

    public static ArrayList<String> splitLog (String msg, boolean showPager, boolean showLength) {
        ArrayList<String> msgs = new ArrayList<String>();
        int len = msg.length();
        if (len > MAXLEN) {
            if (showLength) {
                msgs.add("<<Log.length="+len+">>");
            }
            int chunkCount = len / MAXLEN;
            for (int i = 0; i <= chunkCount; i++) {
                String prefix = showPager ? "<" + i + "/" + chunkCount + "> " : "";
                int max = MAXLEN * (i + 1);
                int offset = MAXLEN * i;
                if (max >= len) {
                    msgs.add(prefix+msg.substring(offset));
                }
                else {
                    msgs.add(prefix+msg.substring(offset, max));
                }
            }
        }
        else
        {
            msgs.add(msg);
        }
        return msgs;
    }

    public static int log (int prio, String tag, String msg, boolean showPager, boolean showLength) {
        if (msg.length() > MAXLEN) {
            ArrayList<String> msgs = splitLog(msg, showPager, showLength);
            int count = 0;
            for (String msgi : msgs) {
                count += Log.println(prio, tag, msgi);
            }
            return count;
        }
        else
        {
            return Log.println(prio, tag, msg);
        }
    }

    public static int log (int prio, String tag, String msg) {
        return log(prio, tag, msg, SHOW_PAGER(), SHOW_LENGTH());
    }

    public static int v (String tag, String msg) {
        return log(VERBOSE, tag, msg);
    }

    public static int d (String tag, String msg) {
        return log(DEBUG, tag, msg);
    }

    public static int i (String tag, String msg) {
        return log(INFO, tag, msg);
    }

    public static int w (String tag, String msg) {
        return log(WARN, tag, msg);
    }

    public static int e (String tag, String msg) {
        return log(ERROR, tag, msg);
    }

    public static int v (String msg) {
        return v(TAG(), msg);
    }

    public static int d (String msg) {
        return d(TAG(), msg);
    }

    public static int i (String msg) {
        return i(TAG(), msg);
    }

    public static int w (String msg) {
        return w(TAG(), msg);
    }

    public static int e (String msg) {
        return e(TAG(), msg);
    }

}
