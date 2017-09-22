package de.dennisweidmann.aba.Model.SOAP;

import java.util.Hashtable;

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;

public final class SensorValue implements KvmSerializable {
    private java.util.Calendar measured;
    private int type;
    private double value;

    public SensorValue() {
    }

    public void setMeasured(java.util.Calendar measured) {
        this.measured = measured;
    }

    public java.util.Calendar getMeasured(java.util.Calendar measured) {
        return this.measured;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType(int type) {
        return this.type;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getValue(double value) {
        return this.value;
    }

    public int getPropertyCount() {
        return 3;
    }

    public Object getProperty(int __index) {
        switch (__index) {
            case 0:
                return measured.getTime();
            case 1:
                return new Integer(type);
            case 2:
                return new Double(value);
        }
        return null;
    }

    public void setProperty(int __index, Object __obj) {
        switch (__index) {
            case 0:
                measured = (java.util.Calendar) __obj;
                break;
            case 1:
                type = Integer.parseInt(__obj.toString());
                break;
            case 2:
                value = Double.parseDouble(__obj.toString());
                break;
        }
    }

    public void getPropertyInfo(int __index, Hashtable __table, PropertyInfo __info) {
        switch (__index) {
            case 0:
                __info.name = "measured";
                __info.type = java.util.Date.class;
                break;
            case 1:
                __info.name = "type";
                __info.type = Integer.class;
                break;
            case 2:
                __info.name = "value";
                __info.type = Double.class;
                break;
        }
    }

}
