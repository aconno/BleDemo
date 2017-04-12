package de.troido.bledemo.epd.bits;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

public final class BitArray implements Parcelable {
    public static final Creator<BitArray> CREATOR = new Creator<BitArray>() {
        @Override
        public BitArray createFromParcel(Parcel in) {
            return new BitArray(in);
        }

        @Override
        public BitArray[] newArray(int size) {
            return new BitArray[size];
        }
    };

    private final byte[] array;

    public BitArray(int size) {
        array = new byte[(int) Math.ceil(size / 8)];
    }

    public BitArray(byte[] array) {
        this.array = Arrays.copyOf(array, array.length);
    }

    protected BitArray(Parcel in) {
        array = in.createByteArray();
    }

    public void set(int index, boolean value) {
        if (value) {
            array[index / 8] |= 0x80 >> index % 8;
        } else {
            array[index / 8] &= ~(0x80 >> index % 8);
        }
    }

    public void set(int index) {
        set(index, true);
    }

    public boolean get(int index) {
        return (array[index / 8] & (0x80 >> index % 8)) != 0;
    }

    public byte[] toByteArray() {
        return Arrays.copyOf(array, array.length);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByteArray(array);
    }
}
