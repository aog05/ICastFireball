package edu.neumont.csc150.view;

import edu.neumont.csc150.model.misc.Color;
import edu.neumont.csc150.service.ConfigService;
import edu.neumont.csc150.service.Injectable;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;

import java.awt.image.BufferedImage;

public class Overlay extends UITask {
    private final ConfigService configService;
    private final String logo = "\n" +
            "\n" +
            "           ________--7              __________                              |_         ________   ____________||_|_||____________\n" +
            "          `\\        /         _--‾‾‾          ‾‾‾--             /‾‾‾‾‾‾‾‾7 ‾|      _---        ---_              ^               \\\n" +
            "            |      |         /                     ‾\\          /       /‾         /         ||     \\       __    O    __          |\n" +
            "            |      |        |      ‾‾‾:--            \\        |        \\         |     /        __ |     ‾‾      V      ‾‾     7  |\n" +
            "            |   |  |        |       ‾‾‾              |        |         |       |     /   __---‾  V   ________       ________     |\n" +
            "            |   |  |       /              /‾‾‾‾--_  /        |          |      /     /   /       |   |        |  |  |        |   |\n" +
            "            |  ||  |       |             /        \\/       \\\\|           |     |    |   |         \\ /         |  |  |         \\ /\n" +
            "            |  ||  |       |                               /‾‾‾\\  ^      |     |        |          V          |  |  |          V\n" +
            "            |  ||  |       |                            __-  /  }/ \\      |    /       /                      |  |  |\n" +
            "            |  ||  |      /        /       _________----    []  }   \\     |    \\       |                      |  |  |\n" +
            "            |  ||  |      |       ||         -------_____  /   /     |     |    |      \\                      |  |  |\n" +
            "            |  ||  |      |      |||               ‾‾---\\    / |__--‾      |    |       \\_                    |  |  |\n" +
            "            |  ||  |      |                       ^      \\_--               |    \\_       --_                 |  |  |\n" +
            "            |  ||  |      |               |      / \\     |                  |      \\         \\                |  |  |\n" +
            "            |  ||  |      \\                \\____-   |    |       -^          |      \\         \\               |  |  |\n" +
            "            |  |   |       |                         \\  |       /   \\        |.___--‾          |              |  |  |\n" +
            "            |  |   |        \\               - -      |  |      /     |        |                /              |  |  |\n" +
            "            |  |   |         \\                      /  /      /      |        \\          __---‾               |  |  |\n" +
            "            |       \\         ‾‾--__           __--‾  /      |        \\        \\ __---‾‾‾                     |     |\n" +
            "           /         \\               ‾‾‾‾‾‾‾‾‾‾      /        \\        ‾--______⅃                              \\    /\n" +
            "            ‾‾‾‾‾----⅃                              /___----‾‾                                                   \\/\n" +
            "\n" +
            "                      ________\n" +
            "                    /‾        \\  <‾‾‾‾‾‾>   /‾‾‾‾‾‾‾\\    |‾‾‾‾‾‾‾‾|   |‾‾‾‾‾‾‾\\    /‾‾‾>      |‾‾‾|        |‾‾‾|\n" +
            "                   |    _____/    |    |   |    /\\   |   |   _____|   |    \\  |   |   |       |   |        |   |\n" +
            "                   |   |          |    |   |    \\/  /   |   |____    |     /  /   |    |     |   |        |   |\n" +
            "                   |   \\___      |    |   |   -__  \\    |    ____|   |      --   |  /\\  |    |   |        |   |\n" +
            "                  |    ___ >     |    |   |  |   \\  |   |   |___     |    /\\  \\  | |__| |    |   |____    |   |____\n" +
            "                  |   /          |   |    |  |   | |   |        |   |     \\/  /  /       \\  |         |  |         |\n" +
            "                  |  |           |   |   /__/     \\|   |________|   |________/  /__/‾‾‾\\_⅃  |_________|  |_________|\n" +
            "                   \\/             ‾V‾";

    public Overlay(Injectable color, Injectable config) {
        super(color);
        configService = (ConfigService) config;
    }

    public void titleScreen() {
        int xCoord = 0;
        int yCoord = 0;
        for (int c = 0; c < logo.length(); c++) {
            char character = logo.charAt(c);

            if (character == '\n') {
                xCoord = 0;
                yCoord++;
                continue;
            }

            long color = new Color(255, 255, 255).toLong();
            setFrameBufferAt(xCoord, yCoord, character + color);

            xCoord++;
        }
    }

    public void drawHealth(float percentStart, float percentEnd, float remainingHealth) {
        int startHealthBar = (int) ((configService.screenWidth - 1) * percentStart);
        int endHealthBar = (int) ((configService.screenWidth - 1) * percentEnd);

        for (int x = startHealthBar; x <= endHealthBar; x++) {
            long healthValue;

            float lerpX = (float) (x - startHealthBar) / (endHealthBar - startHealthBar);
            if (remainingHealth >= lerpX)
                healthValue = colorService.getColoredCharacter(new Color(Console.TextColor.RED), '#');
            else healthValue = colorService.getColoredCharacter(new Color(Console.TextColor.DEFAULT), ' ');

            setFrameBufferAt(x, configService.screenHeight - 2, healthValue);
            setFrameBufferAt(x, configService.screenHeight - 1, healthValue);
        }

        long yellowTiltLeft = colorService.getColoredCharacter(new Color(Console.TextColor.YELLOW), '\\');
        long yellowTiltRight = colorService.getColoredCharacter(new Color(Console.TextColor.YELLOW), '/');
        setFrameBufferAt(startHealthBar - 1,configService.screenHeight - 2, yellowTiltRight);
        setFrameBufferAt(startHealthBar - 1,configService.screenHeight - 1, yellowTiltLeft);

        setFrameBufferAt(endHealthBar + 1,configService.screenHeight - 2, yellowTiltLeft);
        setFrameBufferAt(endHealthBar + 1,configService.screenHeight - 1, yellowTiltRight);
    }

    /** x and y are in percents, (0,0) being the top left and (1, 1) being bottom right */
    public void displayImage(float x, float y, int width, int height, String url) {
        if (url == null) return;

        Image image = colorService.loadImage(url);
        if (image == null) return;

        for (int pixelX = 0; pixelX < image.getWidth() * width; pixelX++) {
            for (int pixelY = 0; pixelY < image.getHeight() * height; pixelY++) {
                PixelReader pixelReader = image.getPixelReader();
                int alpha = (pixelReader.getArgb(pixelX / width, pixelY / height) >> 24) & 0xFF;
                if (alpha < 255) continue;

                int coordX = (int) (configService.screenWidth * x) + pixelX - (int)image.getWidth() * width / 2;
                int coordY = (int) (configService.screenHeight * y) + pixelY - (int)image.getHeight() * height / 2;

                int color = pixelReader.getArgb(pixelX / width, pixelY / height);
                long character = colorService.getColoredCharacter(new Color(color), '∎');

                try {
                    setFrameBufferAt(coordX, coordY, character);
                } catch (IndexOutOfBoundsException _) {}
            }
        }
    }

    /** x and y are in pixels, (0,0) being the top left and (max, max) being bottom right */
    public void displayText(int x, int y, String text) {
        if (text == null) return;

        int newlineBuffer = 0;

        for (int c = 0; c < text.length(); c++) {
            char character = text.charAt(c);
            if (character == '\n') {
                y++;
                newlineBuffer = 0;
                continue;
            }

            try {
                setFrameBufferAt(x + newlineBuffer, y, colorService.getColoredCharacter(new Color(0xFFFFFFFF), character));
            } catch (IndexOutOfBoundsException _) {}

            newlineBuffer++;
        }
    }
}
