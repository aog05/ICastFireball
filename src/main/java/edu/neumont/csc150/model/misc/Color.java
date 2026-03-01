package edu.neumont.csc150.model.misc;

import edu.neumont.csc150.service.ColorService;
import edu.neumont.csc150.view.Console;

public class Color {
    private Console.TextColor consoleColor = Console.TextColor.DEFAULT;
    private int red = -1;
    private int green = -1;
    private int blue = -1;

    public Color(int red, int green, int blue) {
        if (!ColorService.FULL_ANSI_COLOR_SUPPORT)
            throw new RuntimeException("Full ANSI Colors are not turned on/supported." +
                    "Either mark FULL_ANSI_COLOR_SUPPORT as true or supply the Console.TextColor instead");

        setRed(red);
        setGreen(green);
        setBlue(blue);
    }

    public Color(Console.TextColor consoleColor) {
        setConsoleColor(consoleColor);
    }

    /** Use this when using the color of an image. This will be parsed in the format AARRGGBB */
    public Color(int color) {
        setRed(color >> 16 & 0xFF);
        setGreen(color >> 8 & 0xFF);
        setBlue(color & 0xFF);
    }

    /** Use this when using an existing color in the program. This will be parsed in the format RRGGBBCCCC */
    public Color(long color) {
        setRed((int) (color >> 32) & 0xFF);
        setGreen((int) (color >> 24) & 0xFF);
        setBlue((int) (color >> 16) & 0xFF);
    }

    public Color(Color color) {
        setConsoleColor(color.consoleColor);
        setRed(color.getRed());
        setGreen(color.getGreen());
        setBlue(color.getBlue());
    }

    public long toLong() {
        if (red < 0 || green < 0 && blue < 0)
            throw new RuntimeException("Color needs to be initialized. Colors cannot be negative");

        return ((long) red << 32) + ((long) green << 24) + ((long) blue << 16);
    }

    public int getRed() {
        return red;
    }

    public void setRed(int red) {
        this.red = Math.max(Math.min(red, 255), 0);
    }

    public int getGreen() {
        return green;
    }

    public void setGreen(int green) {
        this.green = Math.max(Math.min(green, 255), 0);
    }

    public int getBlue() {
        return blue;
    }

    public void setBlue(int blue) {
        this.blue = Math.max(Math.min(blue, 255), 0);
    }

    public Console.TextColor getConsoleColor() {
        return consoleColor;
    }

    public void setConsoleColor(Console.TextColor consoleColor) {
        this.consoleColor = consoleColor;

        int color32 = switch (consoleColor) {
            case BLACK -> 0x0C0C0C;
            case RED -> 0xC50F1F;
            case GREEN -> 0x13A10E;
            case YELLOW -> 0xC19C00;
            case BLUE -> 0x0037DA;
            case PURPLE -> 0x881798;
            case CYAN -> 0x33BBC8;
            default -> 0xCCCCCC;
        };

        setRed((color32 >> 16) & 0xFF);
        setGreen((color32 >> 8) & 0xFF);
        setBlue(color32 & 0xFF);
    }
}
