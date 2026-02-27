package edu.neumont.csc150.view;

import edu.neumont.csc150.service.ColorService;
import edu.neumont.csc150.service.Injectable;

public abstract class UITask {
    protected static volatile long[][] frameBuffer;
    protected ColorService colorService;

    protected UITask(Injectable color) {
        colorService = (ColorService) color;
    }

    protected void setFrameBufferAt(int x, int y, long character) {
        if (x < 0 || x >= frameBuffer[0].length || y < 0 || y >= frameBuffer.length)
            throw new IndexOutOfBoundsException("x or y out of bounds for framebuffer: " + x + " " + y);

        frameBuffer[y][x] = character;
    }
}
