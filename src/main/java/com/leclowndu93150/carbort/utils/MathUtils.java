package com.leclowndu93150.carbort.utils;


public final class MathUtils {
    public static boolean isWithinArea(int xPos, int yPos, int x, int y, int width, int height) {
        return xPos >= x && yPos >= y && xPos < x + width && yPos < y + height;
    }

}