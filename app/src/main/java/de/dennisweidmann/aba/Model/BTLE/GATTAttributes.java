package de.dennisweidmann.aba.Model.BTLE;

/*
The MIT License (MIT)

Copyright (c) 2017 Dennis Weidmann

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

import java.util.HashMap;

public class GATTAttributes {
    private static HashMap<String, String> attributes = new HashMap<String, String>();
    //public static String CLIENT_CHARACTERISTIC_CONFIG = "yyy-xxx-zzz";

    public static String FIRST_SERVICE = "00001800-0000-1000-8000-00805f9b34fb";
    public static String FIRST_FIRST_CHARACTERISTIC = "00002a00-0000-1000-8000-00805f9b34fb";
    public static String FIRST_SECOND_CHARACTERISTIC = "00002a01-0000-1000-8000-00805f9b34fb";
    public static String FIRST_THIRD_CHARACTERISTIC = "00002a04-0000-1000-8000-00805f9b34fb";

    public static String SECOND_SERVICE = "00001801-0000-1000-8000-00805f9b34fb";
    public static String SECOND_FIRST_CHARACTERISTIC = "00002a05-0000-1000-8000-00805f9b34fb";

    public static String THIRD_SERVICE = "f0080001-0451-4000-b000-000000000000";
    public static String HEART_FREQUENCY_CHARACTERISTIC = "f0080002-0451-4000-b000-000000000000";
    public static String THIRD_SECOND_CHARACTERISTIC = "f0080003-0451-4000-b000-000000000000";

    public static String FOURTH_SERVICE = "f0020001-0451-4000-b000-000000000000";
    public static String FOURTH_FIRST_CHARACTERISTIC = "f0020002-0451-4000-b000-000000000000";
    public static String FOURTH_SECOND_CHARACTERISTIC = "f0020003-0451-4000-b000-000000000000";

    public static String FIFTH_SERVICE = "0000fee7-0000-1000-8000-00805f9b34fb";
    public static String BLOOD_PRESSURE_CHARACTERISTIC = "0000fea1-0000-1000-8000-00805f9b34fb";
    public static String FIFTH_SECOND_CHARACTERISTIC = "0000fea2-0000-1000-8000-00805f9b34fb";
    public static String FIFTH_THIRD_CHARACTERISTIC = "0000fec9-0000-1000-8000-00805f9b34fb";

    static {
        attributes.put(FIRST_SERVICE , "First Service");
        attributes.put(FIRST_FIRST_CHARACTERISTIC , "First Service");
        attributes.put(FIRST_SECOND_CHARACTERISTIC , "First Service");
        attributes.put(FIRST_THIRD_CHARACTERISTIC , "First Service");

        attributes.put(SECOND_SERVICE , "Second Service");
        attributes.put(SECOND_FIRST_CHARACTERISTIC , "Second Service");

        attributes.put(THIRD_SERVICE , "Third Service");
        attributes.put(HEART_FREQUENCY_CHARACTERISTIC , "Third Service");
        attributes.put(THIRD_SECOND_CHARACTERISTIC , "Third Service");

        attributes.put(FOURTH_SERVICE , "Fourth Service");
        attributes.put(FOURTH_FIRST_CHARACTERISTIC , "Fourth Service");
        attributes.put(FOURTH_SECOND_CHARACTERISTIC , "Fourth Service");

        attributes.put(FIFTH_SERVICE , "Fifth Service");
        attributes.put(BLOOD_PRESSURE_CHARACTERISTIC , "Fifth Service");
        attributes.put(FIFTH_SECOND_CHARACTERISTIC , "Fifth Service");
        attributes.put(FIFTH_THIRD_CHARACTERISTIC , "Fifth Service");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
