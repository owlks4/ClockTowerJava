package com.clockTower.fileUtilities;

public class BitConverter {

    public static short ToInt16(byte[] bytes, int pos) {

        //these ANDs are necessary because otherwise it treats the bytes as signed >:(
        short output = (short) (bytes[pos] & 0xFF);
        output |= (bytes[pos + 1] & 0xFF) << 8;
        return output;
    }

    public static int ToUInt16(byte[] bytes, int pos) {    //unsigned types don't exist in java, so we read this as an int here

        //these ANDs are necessary because otherwise it treats the bytes as signed >:(
        int output = bytes[pos] & 0xFF;
        output |= (bytes[pos + 1] & 0xFF) << 8;
        return output;
    }

    public static int ToInt32(byte[] bytes, int pos) {

        //these ANDs are necessary because otherwise it treats the bytes as signed >:(
        int output = (bytes[pos] & 0xFF);
        output |= (bytes[pos + 1] & 0xFF) << 8;
        output |= (bytes[pos + 2] & 0xFF) << 16;
        output |= (bytes[pos + 3] & 0xFF) << 24;

        return output;
    }

    public static int ToInt32(int[] bytes, int pos) {
        //alternative version for int arrays

        //these ANDs are necessary because otherwise it treats the bytes as signed >:(
        int output = (bytes[pos] & 0xFF);
        output |= (bytes[pos + 1] & 0xFF) << 8;
        output |= (bytes[pos + 2] & 0xFF) << 16;
        output |= (bytes[pos + 3] & 0xFF) << 24;

        return output;
    }

    public static long ToInt64(byte[] bytes, int pos) {

        //these ANDs are necessary because otherwise it treats the bytes as signed >:(
        long output = (bytes[pos] & 0xFF);
        output |= (bytes[pos + 1] & 0xFF) << 8;
        output |= (bytes[pos + 2] & 0xFF) << 16;
        output |= (bytes[pos + 3] & 0xFF) << 24;
        if ((bytes.length - pos) >= 8) {
            output |= (bytes[pos + 4] & 0xFF) << 32;
            output |= (bytes[pos + 5] & 0xFF) << 40;
            output |= (bytes[pos + 6] & 0xFF) << 48;
            output |= (bytes[pos + 7] & 0xFF) << 56;
        }
        return output;
    }

    public static long ToInt64(int[] bytes, int pos) {

        //these ANDs are necessary because otherwise it treats the bytes as signed >:(
        long output = (bytes[pos] & 0xFF);
        output |= (bytes[pos + 1] & 0xFF) << 8;
        output |= (bytes[pos + 2] & 0xFF) << 16;
        output |= (bytes[pos + 3] & 0xFF) << 24;
        if ((bytes.length - pos) >= 8) {
            output |= (bytes[pos + 4] & 0xFF) << 32;
            output |= (bytes[pos + 5] & 0xFF) << 40;
            output |= (bytes[pos + 6] & 0xFF) << 48;
            output |= (bytes[pos + 7] & 0xFF) << 56;
        }
        return output;
    }

    public static byte[] GetBytes(int number) {

        byte[] output = new byte[4];

        output[0] = (byte) (number & 0xFF);
        output[1] = (byte) ((number >>> 8) & 0xFF);
        output[2] = (byte) ((number >>> 16) & 0xFF);
        output[3] = (byte) ((number >>> 24) & 0xFF);

        return output;
    }

    public static byte[] GetBytes(short number) {

        byte[] output = new byte[2];

        output[0] = (byte) (number & 0xFF);
        output[1] = (byte) ((number >>> 8) & 0xFF);

        return output;
    }
}