package com.refinedmods.refinedstorage.common.storage.externalstorage;

class ExternalStorageWorkRate {
    private static final int[] OPERATION_COUNTS = new int[] {
        40, // slowest, every 2 sec
        30, // faster, every 1.5 sec
        20, // medium, every 1 sec
        10, // faster, every 0.5 sec
        5  // fastest, every 0.25 sec
    };

    private int idx = 2; // medium
    private int counter = 0;
    private int threshold = OPERATION_COUNTS[idx];

    boolean canDoWork() {
        counter++;
        if (counter >= threshold) {
            counter = 0;
            return true;
        }
        return false;
    }

    void faster() {
        if (idx + 1 < OPERATION_COUNTS.length) {
            idx++;
            updateThreshold();
        }
    }

    void slower() {
        if (idx - 1 >= 0) {
            idx--;
            updateThreshold();
        }
    }

    private void updateThreshold() {
        threshold = OPERATION_COUNTS[idx];
    }
}
