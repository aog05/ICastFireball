package edu.neumont.csc150.service;

import edu.neumont.csc150.model.misc.Color;
import edu.neumont.csc150.view.Console;
import javafx.scene.image.Image;

import java.util.LinkedHashMap;
import java.util.Map;

public class ColorService implements Injectable {
    /** This could be true if the terminal that the user is executing in
     * supports RGB color codes, such as "`e[38;2;255;255;255m" */
    public static final boolean FULL_ANSI_COLOR_SUPPORT = true;

    private final Map<String, Image> loadedImages = new LinkedHashMap<>();
    private Map<Short, Console.TextColor> toColorMap;
    private Map<Console.TextColor, Short> toCharacterMap;
    private Map<Integer, Console.TextColor> fromColorCodeMap;
    //    private final String LIGHT_VALUES = " .'`^\",:;Il!i><~+_-?][}{1)(|\\/tfjrxnuvczXYUJCLQ0OZmwqpdbkhao*#MW&8%B@$";
    private final String LIGHT_VALUES = " ':\"l1X%#&0W@";
    private final int[] CONSOLE_COLORS_CODES = {
            0x0C0C0C, 0xC50F1F, 0x13A10E,
            0xC19C00, 0x0037DA, 0x881798,
            0x33BBC8, 0xCCCCCC
    };
    public static final String RESET = "\u001B[0m";

    @Override
    public void startService() {
        toColorMap = new LinkedHashMap<>();
        toColorMap.put((short) 0x0000, Console.TextColor.DEFAULT);
        toColorMap.put((short) 0x1000, Console.TextColor.BLACK);
        toColorMap.put((short) 0x2000, Console.TextColor.RED);
        toColorMap.put((short) 0x3000, Console.TextColor.GREEN);
        toColorMap.put((short) 0x4000, Console.TextColor.YELLOW);
        toColorMap.put((short) 0x5000, Console.TextColor.BLUE);
        toColorMap.put((short) 0x6000, Console.TextColor.PURPLE);
        toColorMap.put((short) 0x7000, Console.TextColor.CYAN);
        toColorMap.put((short) 0x8000, Console.TextColor.WHITE);

        toCharacterMap = new LinkedHashMap<>();
        toCharacterMap.put(Console.TextColor.DEFAULT, (short) 0x0000);
        toCharacterMap.put(Console.TextColor.BLACK, (short) 0x1000);
        toCharacterMap.put(Console.TextColor.RED, (short) 0x2000);
        toCharacterMap.put(Console.TextColor.GREEN, (short) 0x3000);
        toCharacterMap.put(Console.TextColor.YELLOW, (short) 0x4000);
        toCharacterMap.put(Console.TextColor.BLUE, (short) 0x5000);
        toCharacterMap.put(Console.TextColor.PURPLE, (short) 0x6000);
        toCharacterMap.put(Console.TextColor.CYAN, (short) 0x7000);
        toCharacterMap.put(Console.TextColor.WHITE, (short) 0x8000);

        fromColorCodeMap = new LinkedHashMap<>();
        fromColorCodeMap.put(0x0C0C0C, Console.TextColor.BLACK);
        fromColorCodeMap.put(0xC50F1F, Console.TextColor.RED);
        fromColorCodeMap.put(0x13A10E, Console.TextColor.GREEN);
        fromColorCodeMap.put(0xC19C00, Console.TextColor.YELLOW);
        fromColorCodeMap.put(0x0037DA, Console.TextColor.BLUE);
        fromColorCodeMap.put(0x881798, Console.TextColor.PURPLE);
        fromColorCodeMap.put(0x33BBC8, Console.TextColor.CYAN);
        fromColorCodeMap.put(0xCCCCCC, Console.TextColor.WHITE);
    }

    @Override
    public void stopService() {
        toColorMap = null;
        toCharacterMap = null;
    }

    public byte getBrightnessFromDistance(float distance, float brightness) {
        return (byte) Math.max(
                Math.min(
                        LIGHT_VALUES.length() - distance / brightness,
                        LIGHT_VALUES.length() - 1),
                0);
    }

    public long getColoredBrightness(Color color, byte brightness) {
        brightness %= (byte) LIGHT_VALUES.length();
        char character = LIGHT_VALUES.charAt(brightness);

        if (FULL_ANSI_COLOR_SUPPORT)
            return color.toLong() + character;

        if (toCharacterMap == null) return '\0';

        short colorCode = toCharacterMap.get(color.getConsoleColor());
        return colorCode + character;
    }

    public long getColoredCharacter(Color color, char character) {
        if (FULL_ANSI_COLOR_SUPPORT)
            return color.toLong() + character;

        character = (char) Math.min(character, 255);
        if (toCharacterMap == null) return '\0';

        short colorCode = toCharacterMap.get(color.getConsoleColor());
        return (char) (colorCode + character);
    }

    public String getRawColorCode(Console.TextColor color) {
        return switch (color) {
            case BLACK -> "\u001B[30m";
            case RED -> "\u001B[31m";
            case GREEN -> "\u001B[32m";
            case YELLOW -> "\u001B[33m";
            case BLUE -> "\u001B[34m";
            case PURPLE -> "\u001B[35m";
            case CYAN -> "\u001B[36m";
            case WHITE -> "\u001B[37m";
            default -> "\0";
        };
    }

    public char getUncoloredCharacter(long character) {
        return (char) (character & 0xFFFF);
    }

    public Color getColor(long character) {
        if (FULL_ANSI_COLOR_SUPPORT) {
            int red = (int) (character >> 32) & 0xFF;
            int green = (int) (character >> 24) & 0xFF;
            int blue = (int) (character >> 16) & 0xFF;

            return new Color(red, green, blue);
        }

        short colorValue = (short) (character & 0xF000);
        if (toColorMap == null || !toColorMap.containsKey(colorValue))
            return new Color(Console.TextColor.DEFAULT);

        return new Color(toColorMap.get(colorValue));
    }

    public long getANSIColor(long character) {
        return character & 0xFFFFFF0000L;
    }

    public synchronized Image loadImage(String url) {
        if (loadedImages.containsKey(url))
            return loadedImages.get(url);

        Image image = new Image(url);
        loadedImages.put(url, image);
        return image;
    }

    /** This can be used when the console does not support full advanced, ansi color-codes */
    public synchronized Color getColorAtPoint(Image image, int x, int y) {
        x = Math.min(Math.max(x, 0), (int)image.getWidth() - 1);
        y = Math.min(Math.max(y, 0), (int)image.getHeight() - 1);

        int imageColor = image.getPixelReader().getArgb(x, (int)image.getHeight() - 1 - y);
        int alpha = (imageColor >> 24) & 0xFF;
        int red = (imageColor >> 16) & 0xFF;
        int green = (imageColor >> 8) & 0xFF;
        int blue = imageColor & 0xFF;

        if (alpha < 255) return null;

        if (FULL_ANSI_COLOR_SUPPORT)
            return new Color(red, green, blue);

        int closestColor = 0x00CCCCCC;
        int closestDistance = Integer.MAX_VALUE;
        for (int colorCode : CONSOLE_COLORS_CODES) {
            int consoleR = (colorCode >> 16) & 0xFF;
            int consoleG = (colorCode >> 8) & 0xFF;
            int consoleB = colorCode & 0xFF;
            int rDis = consoleR - red;
            int gDis = consoleG - green;
            int bDis = consoleB - blue;
            int imageDistance = rDis * rDis + gDis * gDis + bDis * bDis;

            if (imageDistance == 0)
                return new Color(fromColorCodeMap.get(colorCode));

            if (imageDistance < closestDistance) {
                closestDistance = imageDistance;
                closestColor = colorCode;
            }
        }

        return new Color(fromColorCodeMap.get(closestColor));
    }

    public String getANSIColorString(long color) {
        int red = (int) (color >> 32) & 0xFF;
        int green = (int) (color >> 24) & 0xFF;
        int blue = (int) (color >> 16) & 0xFF;

        return getANSIColorString(red, green, blue);
    }

    private String getANSIColorString(int red, int green, int blue) {
        StringBuilder sb = new StringBuilder(20);
        sb.append("\u001B[38;2;");
        sb.append(red); sb.append(';');
        sb.append(green); sb.append(';');
        sb.append(blue); sb.append('m');
        return sb.toString();
    }
}
