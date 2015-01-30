package com.securitydialog.androidit;

import java.security.InvalidParameterException;
import java.util.Calendar;
import java.lang.reflect.Method;
import android.os.Build;

/**
 * Created by Mike on 30/01/2015.
 * RFC5424 http://tools.ietf.org/html/rfc5424
 */
public class Syslog {

    private int pri;
    private String header;
    private String msg;

    public Syslog(int pri, String msg) {
        if (pri >= 0 && pri <= 191) {
            this.pri = pri;
        } else {
            throw new InvalidParameterException("prival must be between 0 and 191!");
        }
        setHeader();
        this.msg = msg;
    }

    public Syslog(int facility, int severity, String msg) {
        if (facility >= 0 && facility <= 23 && severity >= 0 && severity <= 7) {
            this.pri = (facility * 8) + severity;
        } else {
            throw new InvalidParameterException("facility must be between 0 and 23, severity between 0 and 7!");
        }
        setHeader();
        this.msg = msg;
    }

    public String getPri() {
        return "<" + pri + ">";
    }

    public void setPri(int pri) {
        this.pri = pri;
    }

    public void setPri(int facility, int severity) {
        if (facility >= 0 && facility <= 23 && severity >= 0 && severity <= 7) {
            this.pri = (facility * 8) + severity;
        } else {
            throw new InvalidParameterException("facility must be between 0 and 23, severity between 0 and 7!");
        }
    }

    public String getHeader() {
        return header;
    }

    /**
     * Retrieves the net.hostname system property
     */
    private static String getHostName() {
        try {
            Method getString = Build.class.getDeclaredMethod("getString", String.class);
            getString.setAccessible(true);
            return getString.invoke(null, "net.hostname").toString();
        } catch (Exception ex) {
            return "hostname";
        }
    }

    /**
     * The TIMESTAMP field is a formalized timestamp derived from [RFC3339]. http://tools.ietf.org/html/rfc33
     */
    public void setHeader() {
        Calendar c = Calendar.getInstance();
        this.header = preZero(c.get(Calendar.DAY_OF_MONTH)) + "-" + preZero(c.get(Calendar.MONTH)+1) + "-" +  preZero(c.get(Calendar.YEAR))
                + "T" +  preZero(c.get(Calendar.HOUR_OF_DAY)) + ":" +  preZero(c.get(Calendar.MINUTE)) + ":" +  preZero(c.get(Calendar.SECOND))+ "Z"
                + " " + getHostName();
    }

    private String preZero(int n1) {
        String n2 = String.valueOf(n1);
        if (n2.length() == 1) {
            return "0"+n2;
        } else {
            return n2;
        }
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return getPri() + "1 " + header + " " + msg;
    }
}
