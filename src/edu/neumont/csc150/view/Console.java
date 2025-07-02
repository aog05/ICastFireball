package edu.neumont.csc150.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/************************************************************
 * Console app by Brett Beardall                            *
 * Contributed by:                                          *
 *      - Arthur Grover (Implemented Color32)               *
 * **********************************************************/

//CTRL SHIFT -  = Collapse all code
//CTRL SHIFT +  = Expand all code
@SuppressWarnings("unused")
public class Console {
    private static final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    //If the console should request the value on the same line as the question or get it from the following line
    public static boolean getInputOnSameLine = false;
    //Pattern to validate the date with. (e.g. "MM-dd-yyyy", "M/dd/yyyy", "dd/MM/yyyy")
    public static String defaultDateFormat = "MM/dd/yyyy";
    //Boolean to know if the console has full ANSI support (Disable if unexpected behavior occurs)
    private static final boolean FULL_ANSI_COLOR_SUPPORT = true;

    //region TextColors (expand)
    public enum TextColor {BLACK, RED, GREEN, YELLOW, BLUE, PURPLE, CYAN, WHITE, DEFAULT}
    private static final String BLACK = "\u001B[30m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";
    //endregion

    //region BackgroundColors  (expand)
    public enum BackgroundColor {BLACK, RED, GREEN, YELLOW, BLUE, PURPLE, CYAN, WHITE, DEFAULT}
    private static final String BLACK_BACKGROUND = "\u001B[40m";
    private static final String RED_BACKGROUND = "\u001B[41m";
    private static final String GREEN_BACKGROUND = "\u001B[42m";
    private static final String YELLOW_BACKGROUND = "\u001B[43m";
    private static final String BLUE_BACKGROUND = "\u001B[44m";
    private static final String PURPLE_BACKGROUND = "\u001B[45m";
    private static final String CYAN_BACKGROUND = "\u001B[46m";
    private static final String WHITE_BACKGROUND = "\u001B[47m";
    //endregion

    public static class Color32 {
        private Console.TextColor consoleColor = Console.TextColor.DEFAULT;
        private int red = -1;
        private int green = -1;
        private int blue = -1;

        /**
         * Set the red, green, and blue values of the color you want
         * @param red Amount of red in the color
         * @param green Amount of green in the color
         * @param blue Amount of blue in the color
         * */
        public Color32(int red, int green, int blue) {
            if (!FULL_ANSI_COLOR_SUPPORT)
                throw new RuntimeException("Full ANSI Colors are not turned on/supported." +
                        "Either mark FULL_ANSI_COLOR_SUPPORT as true or supply the Console.TextColor instead");

            setRed(red);
            setGreen(green);
            setBlue(blue);
        }

        /**
         * Use the legacy Console.TextColor enum
         * @param consoleColor Predefined enum values for color
         * @see TextColor
         * */
        public Color32(Console.TextColor consoleColor) {
            setConsoleColor(consoleColor);
        }

        /**
         * Supply a hexadecimal color code in the form 0xFFFFFF
         * @param hexcode This will be parsed in the format RRGGBB
         * */
        public Color32(int hexcode) {
            hexcode = Math.min(Math.max(hexcode, 0), 0xFFFFFF);

            setRed(hexcode >> 16 & 0xFF);
            setGreen(hexcode >> 8 & 0xFF);
            setBlue(hexcode & 0xFF);
        }

        /**
         * Pass in an existing color
         * @param color Reuse a color object
         * */
        public Color32(Color32 color) {
            setConsoleColor(color.consoleColor);
            setRed(color.getRed());
            setGreen(color.getGreen());
            setBlue(color.getBlue());
        }

        /**
         * Set the RGB values through the hue, saturation, and lightness values.
         * @param hue Hue of the color (0 - 360)
         * @param saturation Saturation of the color (0 - 1)
         * @param lightness Lightness or brightness of the color (0 - 1)
         * */
        public void setHSLValues(float hue, float saturation, float lightness) {
            hue %= 360;
            saturation = Math.max(Math.min(saturation, 1), 0);
            lightness = Math.max(Math.min(lightness, 1), 0);

            // Formula from RapidTables
            float C = (1 - Math.abs(2 * lightness - 1)) * saturation;
            float X = C * (1 - Math.abs((hue / 60) % 2 - 1));
            float m = lightness - C / 2;

            // Derived values
            float d_r = 0;
            float d_g = 0;
            float d_b = 0;

            int hueSection = (int)(hue / 60);

            switch (hueSection) {
                case 0 -> {
                    d_r = C; d_g = X; d_b = 0;
                }
                case 1 -> {
                    d_r = X; d_g = C; d_b = 0;
                }
                case 2 -> {
                    d_r = 0; d_g = C; d_b = X;
                }
                case 3 -> {
                    d_r = 0; d_g = X; d_b = C;
                }
                case 4 -> {
                    d_r = X; d_g = 0; d_b = C;
                }
                case 5 -> {
                    d_r = C; d_g = 0; d_b = X;
                }
            }

            setRed((int)((d_r + m) * 255));
            setGreen((int)((d_g + m) * 255));
            setBlue((int)((d_b + m) * 255));
        }

        /**
         * Get the hexadecimal color code of the current color
         * */
        public int getHexColor() {
            return getRed() << 16 | getGreen() << 8 | getBlue();
        }

        /**
         * Get the Hexadecimal color code from the color
         * @param color Color to convert to hexadecimal
         * */
        public static int getHexColor(Color32 color) {
            return getHexColor(color.getRed(), color.getGreen(), color.getBlue());
        }

        /**
         * Get the Hexadecimal color code from the values
         * @param red Red value
         * @param green Green value
         * @param blue Green value
         * */
        public static int getHexColor(int red, int green, int blue) {
            return red << 16 | green << 8 | blue;
        }

        /**
         * Get the hue of the provided color
         * @param color current color
         * */
        public static float getHue(Color32 color) {
            float d_r = (float)color.getRed() / 255;
            float d_g = (float)color.getGreen() / 255;
            float d_b = (float)color.getBlue() / 255;

            int CmaxIndex = 0;
            if (d_g > d_r && d_g > d_b) CmaxIndex = 1;
            else if (d_b > d_r) CmaxIndex = 2;

            float Cmax = Math.max(Math.max(d_r, d_g), d_b);
            float Cmin = Math.min(Math.min(d_r, d_g), d_b);
            float delta = Cmax - Cmin;

            if (delta == 0) {
                return 0;
            }

            return switch (CmaxIndex) {
                case 0 -> 60 * (((d_g - d_b) / delta) % 6);
                case 1 -> 60 * (((d_b - d_r) / delta) + 2);
                case 2 -> 60 * (((d_r - d_g) / delta) + 4);
                default -> 0;
            };
        }

        /**
         * Get the saturation of the provided color
         * @param color current color
         * */
        public static float getSaturation(Color32 color) {
            float d_r = (float)color.getRed() / 255;
            float d_g = (float)color.getGreen() / 255;
            float d_b = (float)color.getBlue() / 255;

            float Cmax = Math.max(Math.max(d_r, d_g), d_b);
            float Cmin = Math.min(Math.min(d_r, d_g), d_b);
            float delta = Cmax - Cmin;

            return delta / (1 - Math.abs(2 * getLightness(color) - 1));
        }

        /**
         * Get the lightness of the provided color
         * @param color current color
         * */
        public static float getLightness(Color32 color) {
            float d_r = (float)color.getRed() / 255;
            float d_g = (float)color.getGreen() / 255;
            float d_b = (float)color.getBlue() / 255;

            float Cmax = Math.max(Math.max(d_r, d_g), d_b);
            float Cmin = Math.min(Math.min(d_r, d_g), d_b);

            return (Cmax + Cmin) / 2;
        }

        /**
         * Red value of the color
         * */
        public int getRed() {
            return red;
        }

        /**
         * Set the red value
         * @param red Set red value anywhere from 0 to 255
         * */
        public void setRed(int red) {
            this.red = Math.max(Math.min(red, 255), 0);
        }

        /**
         * Green value of the color
         * */
        public int getGreen() {
            return green;
        }

        /**
         * Set the green value
         * @param green Set green value anywhere from 0 to 255
         * */
        public void setGreen(int green) {
            this.green = Math.max(Math.min(green, 255), 0);
        }

        /**
         * Blue value of the color
         * */
        public int getBlue() {
            return blue;
        }

        /**
         * Set the blue value
         * @param blue Set blue value anywhere from 0 to 255
         * */
        public void setBlue(int blue) {
            this.blue = Math.max(Math.min(blue, 255), 0);
        }

        /**
         * Current TextColor that the console is using
         * @note This may not get the color you're looking for.
         * Try using a different getter, like getHexColor() if necessary.
         * */
        public Console.TextColor getConsoleColor() {
            return consoleColor;
        }

        /**
         * Set the color to a predefined color value
         * @param consoleColor Color enum for existing colors
         * @see TextColor
         * */
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


    /**
     * Resets the text color and background color to console default values
     */
    private static final String RESET = "\u001B[0m";
	
    /**
     * Gets an CHAR input from the console.
     * Console will keep asking until a valid response is provided.
     * Uses the default text color
     * @param message  Message to show to the user as to what you are requesting
     * @return Returns a char value representing the users input
     */
    public static char getCharInput(String message){
        return getCharInput(message, TextColor.DEFAULT);
    }

    /**
     * Gets an CHAR input from the console.
     * Console will keep asking until a valid response is provided.
     * @param message  Message to show to the user as to what you are requesting
     * @param textColor ConsoleTextColor of the text when written
     * @return Returns a char value representing the users input
     */
    public static char getCharInput(String message, TextColor textColor){
        return getStringInput(message, false, textColor).charAt(0);
    }

    /**
     * Gets an BOOLEAN input from the console.
     * Console will keep asking until a valid response is provided. Case-insensitive.
     * Uses the default text color
     * @param message  Message to show to the user as to what you are requesting
     * @param positive The positive value to ask the user for (e.g. "Yes")
     * @param negative The negative value to ask the user for (e.g. "No");
     * @return Returns an boolean value representing the users input
     */
    public static boolean getBooleanInput(String message, String positive, String negative){
        return getBooleanInput(message, positive, negative, TextColor.DEFAULT);
    }

    /**
     * Gets an BOOLEAN input from the console.
     * Console will keep asking until a valid response is provided. Case-insensitive.
     * @param message  Message to show to the user as to what you are requesting
     * @param positive The positive value to ask the user for (e.g. "Yes")
     * @param negative The negative value to ask the user for (e.g. "No");
     * @param textColor ConsoleTextColor of the text when written
     * @return Returns an boolean value representing the users input
     */
    public static boolean getBooleanInput(String message, String positive, String negative, TextColor textColor){
        boolean response = false;
        boolean isValidResponse = false;
        do{
            String possibleAnswers = " (" + positive + " or " + negative + ")";
            String responseS = getStringInput(message + possibleAnswers, false, textColor);
            responseS = responseS.toLowerCase().trim();
            if(responseS.equals(positive.toLowerCase())){
                response = isValidResponse = true;
            } else if(responseS.equals(negative.toLowerCase())){
                isValidResponse = true;
                response = false;
            }
            if(!isValidResponse){
                Console.writeLn("Not a valid answer. Please write '" + positive + "' OR '" + negative + "' only.", TextColor.RED);
            }
        }while(!isValidResponse);
        return response;
    }

    /**
     * Gets an BYTE input from the console.
     * Console will keep asking until a valid response is provided.
     * Uses the default text color
     * @param message  Message to show to the user as to what you are requesting
     * @return Returns a byte value representing the users input
     */
    public static byte getByteInput(String message) {
        return getByteInput(message, TextColor.DEFAULT);
    }

    /**
     * Gets an BYTE input from the console.
     * Console will keep asking until a valid response is provided.
     * @param message  Message to show to the user as to what you are requesting
     * @param textColor ConsoleTextColor of the text when written
     * @return Returns a byte value representing the users input
     */
    public static byte getByteInput(String message, TextColor textColor) {
        return getByteInput(message, Byte.MIN_VALUE, Byte.MAX_VALUE, textColor);
    }

    /**
     * Gets an BYTE input from the console.
     * Console will keep asking until a valid response is provided.
     * @param message  Message to show to the user as to what you are requesting
     * @param min Minimum allowed byte value
     * @param max Max allowed byte value
     * @param textColor ConsoleTextColor of the text when written
     * @return Returns a byte value representing the users input
     */
    public static byte getByteInput(String message, byte min, byte max, TextColor textColor) {
        Byte response = null;
        do {
            try {
                String responseS = getStringInput(message, false, textColor);
                response = Byte.parseByte(responseS);
                if(response < min || response > max){
                    response = null;
                    Console.writeLn("Number must be between " + min + " and " + max + "!", TextColor.RED);
                }
            } catch (NumberFormatException n) {
                Console.writeLn("You entered an invalid number.", TextColor.RED);
            }
        }while(response == null);
        return response;
    }

    /**
     * Gets an SHORT input from the console.
     * Console will keep asking until a valid response is provided.
     * Uses the default text color
     * @param message  Message to show to the user as to what you are requesting
     * @return Returns a short value representing the users input
     */
    public static short getShortInput(String message) {
        return getShortInput(message, TextColor.DEFAULT);
    }

    /**
     * Gets an SHORT input from the console.
     * Console will keep asking until a valid response is provided.
     * @param message  Message to show to the user as to what you are requesting
     * @param textColor ConsoleTextColor of the text when written
     * @return Returns a short value representing the users input
     */
    public static short getShortInput(String message, TextColor textColor) {
        return getShortInput(message, Short.MIN_VALUE, Short.MAX_VALUE, textColor);
    }

    /**
     * Gets an SHORT input from the console.
     * Console will keep asking until a valid response is provided.
     * @param message  Message to show to the user as to what you are requesting
     * @param min Minimum allowed short value
     * @param max Max allowed short value
     * @param textColor ConsoleTextColor of the text when written
     * @return Returns a short value representing the users input
     */
    public static short getShortInput(String message, short min, short max, TextColor textColor) {
        Short response = null;
        do {
            try {
                String responseS = getStringInput(message, false, textColor);
                response = Short.parseShort(responseS);
                if(response < min || response > max){
                    response = null;
                    Console.writeLn("Number must be between " + min + " and " + max + "!", TextColor.RED);
                }
            } catch (NumberFormatException n) {
                Console.writeLn("You entered an invalid number.", TextColor.RED);
            }
        }while(response == null);
        return response;
    }

    /**
     * Gets an DOUBLE input from the console.
     * Console will keep asking until a valid response is provided.
     * Uses the default text color
     * @param message  Message to show to the user as to what you are requesting
     * @return Returns an double value representing the users input
     */
    public static double getDoubleInput(String message) {
       return getDoubleInput(message, TextColor.DEFAULT);
    }

    /**
     * Gets an DOUBLE input from the console.
     * Console will keep asking until a valid response is provided.
     * @param message  Message to show to the user as to what you are requesting
     * @param textColor ConsoleTextColor of the text when written
     * @return Returns a double value representing the users input
     */
    public static double getDoubleInput(String message, TextColor textColor) {
        return getDoubleInput(message, Double.MIN_VALUE, Double.MAX_VALUE, textColor);
    }

    /**
     * Gets an DOUBLE input from the console.
     * Console will keep asking until a valid response is provided.
     * @param message   Message to show to the user as to what you are requesting
     * @param min       Minimum double value allowed
     * @param max       Maximum double value allowed
     * @param textColor ConsoleTextColor of the text when written
     * @return Returns a double value representing the users input
     */
    public static double getDoubleInput(String message, double min, double max, TextColor textColor) {
        Double response = null;
        do {
            try {
                String responseS = getStringInput(message, false, textColor);
                response = Double.parseDouble(responseS);
                if (response < min || response > max) {
                    response = null;
                    Console.writeLn("Number must be between " + min + " and " + max + "!", TextColor.RED);
                }
            } catch (NumberFormatException n) {
                Console.writeLn("You entered an invalid number.", TextColor.RED);
            }
        } while (response == null);
        return response;
    }

    /**
     * Gets an FLOAT input from the console.
     * Console will keep asking until a valid response is provided.
     * Uses the default text color
     * @param message  Message to show to the user as to what you are requesting
     * @return Returns an float value representing the users input
     */
    public static float getFloatInput(String message) {
        return getFloatInput(message, TextColor.DEFAULT);
    }

    /**
     * Gets an FLOAT input from the console.
     * Console will keep asking until a valid response is provided.
     * @param message  Message to show to the user as to what you are requesting
     * @param textColor ConsoleTextColor of the text when written
     * @return Returns an float value representing the users input
     */
    public static float getFloatInput(String message, TextColor textColor) {
       return getFloatInput(message, Long.MIN_VALUE, Long.MAX_VALUE, textColor);
    }

    /**
     * Gets an FLOAT input from the console.
     * Console will keep asking until a valid response is provided.
     * @param message  Message to show to the user as to what you are requesting
     * @param min Minimum allowed float value
     * @param max Max allowed float value
     * @param textColor ConsoleTextColor of the text when written
     * @return Returns an float value representing the users input
     */
    public static float getFloatInput(String message, float min, float max, TextColor textColor) {
        Float response = null;
        do {
            try {
                String responseS = getStringInput(message, false, textColor);
                response = Float.parseFloat(responseS);
                if(response < min || response > max){
                    response = null;
                    Console.writeLn("Number must be between " + min + " and " + max + "!", TextColor.RED);
                }
            } catch (NumberFormatException n) {
                Console.writeLn("You entered an invalid number.", TextColor.RED);
            }
        }while(response == null);
        return response;
    }

    /**
     * Gets an LONG input from the console.
     * Console will keep asking until a valid response is provided.
     * Uses the default text color
     * @param message  Message to show to the user as to what you are requesting
     * @return Returns an long value representing the users input
     */
    public static long getLongInput(String message) {
        return getLongInput(message, TextColor.DEFAULT);
    }

    /**
     * Gets an LONG input from the console.
     * Console will keep asking until a valid response is provided.
     * @param message  Message to show to the user as to what you are requesting
     * @param textColor ConsoleTextColor of the text when written
     * @return Returns a long value representing the users input
     */
    public static long getLongInput(String message, TextColor textColor) {
        return getLongInput(message, Long.MIN_VALUE, Long.MAX_VALUE, textColor);
    }

    /**
     * Gets an LONG input from the console.
     * Console will keep asking until a valid response is provided.
     * @param message  Message to show to the user as to what you are requesting
     * @param min Miniumum allowed long value
     * @param max Maximum allowed long value
     * @param textColor ConsoleTextColor of the text when written
     * @return Returns an long value representing the users input
     */
    public static long getLongInput(String message, long min, long max, TextColor textColor) {
        Long response = null;
        do {
            try {
                String responseS = getStringInput(message, false, textColor);
                response = Long.parseLong(responseS);
                if(response < min || response > max){
                    response = null;
                    Console.writeLn("Number must be between " + min + " and " + max + "!", TextColor.RED);
                }
            } catch (NumberFormatException n) {
                Console.writeLn("You entered an invalid number.", TextColor.RED);
            }
        }while(response == null);
        return response;
    }

    /**
     * Gets an INT input from the console.
     * Console will keep asking until a valid response is provided.
     * Uses the default text color
     * @param message  Message to show to the user as to what you are requesting
     * @return Returns an int value representing the users input
     */
    public static int getIntInput(String message) {
        return getIntInput(message, TextColor.DEFAULT);
    }

    /**
     * Gets an INT input from the console.
     * Console will keep asking until a valid response is provided.
     * Uses the default text color
     * @param message  Message to show to the user as to what you are requesting
     * @param min Minimum number the user can provide
     * @param max Maximum number the user can provide
     * @return Returns an int value representing the users input
     */
    public static int getIntInput(String message, int min, int max){
        return getIntInput(message, min, max, TextColor.DEFAULT);
    }

    /**
     * Gets an INT input from the console.
     * Console will keep asking until a valid response is provided.
     * @param message  Message to show to the user as to what you are requesting
     * @param textColor ConsoleTextColor of the text when written
     * @return Returns an int value representing the users input
     */
    public static int getIntInput(String message, TextColor textColor) {
        return getIntInput(message, Integer.MIN_VALUE, Integer.MAX_VALUE, textColor);
    }

    /**
     * Gets an INT input from the console.
     * Console will keep asking until a valid response is provided.
     * @param message  Message to show to the user as to what you are requesting
     * @param min Minimum number the user can provide
     * @param max Maximum number the user can provide
     * @param textColor ConsoleTextColor of the text when written
     * @return Returns an int value representing the users input
     */
    public static int getIntInput(String message, int min, int max, TextColor textColor){
        Integer response = null;
        do {
            try {
                String responseS = getStringInput(message, false, textColor);
                response = Integer.parseInt(responseS);
                if(response < min || response > max){
                    response = null;
                    Console.writeLn("Number must be between " + min + " and " + max + "!", TextColor.RED);
                }
            } catch (NumberFormatException n) {
                Console.writeLn("You entered an invalid number.", TextColor.RED);
            }
        }while(response == null);
        return response;
    }

    /**
     * Gets a String input from the console.
     * Uses the default text color and does not allow empty
     * Console will keep asking until a valid response is provided
     * @param message Message to show to the user as to what you are requesting
     * @return The String the user typed meeting the requirements
     */
    public static String getStringInput(String message){
        return getStringInput(message, false);
    }

    /**
     * Gets a String input from the console.
     * Uses the default text color
     * Console will keep asking until a valid response is provided
     * @param message Message to show to the user as to what you are requesting
     * @param allowEmpty If the method should allow empty (e.g. enter key or space)
     * @return The String the user typed meeting the requirements
     */
    public static String getStringInput(String message, boolean allowEmpty) {
        return getStringInput(message, allowEmpty, TextColor.DEFAULT);
    }

    /**
     * Gets a String input from the console.
     * Console will keep asking until a valid response is provided
     * @param message Message to show to the user as to what you are requesting
     * @param allowEmpty If the method should allow empty (e.g. enter key or space)
     * @param textColor ConsoleTextColor of the text when written
     * @return The String the user typed meeting the requirements
     */
    public static String getStringInput(String message, boolean allowEmpty, TextColor textColor){
        String response = null;
        do{
            try {
                if(getInputOnSameLine){
                    write(message, textColor);
                } else {
                    writeLn(message, textColor);
                }
                response = br.readLine();
                if (!allowEmpty && response.trim().isEmpty()) {
                    Console.writeLn("You must enter a non empty answer!", TextColor.RED);
                    response = null;
                }
            }catch(IOException ex){
                writeLn(ex.getMessage(), TextColor.RED);
            }
        }while(response == null);
        return response;
    }

    /**
     * Gets a String input from the console.
     * Console will keep asking until a valid response is provided
     * @param message Message to show to the user as to what you are requesting
     * @param minCharacters The minimum amount of characters allowed in this string.
     * @param maxCharacters The maximum amount of characters allowed in this string.
     * @param textColor ConsoleTextColor of the text when written
     * @return The String the user typed meeting the requirements
     */
    public static String getStringInput(String message, int minCharacters, int maxCharacters, TextColor textColor){
        String response;
        do {
            response = getStringInput(message, minCharacters <= 0, textColor);
            if(response.length() < minCharacters || response.length() > maxCharacters){
                Console.writeLn("Your input must be between " + minCharacters + " and " + maxCharacters + " characters in length!", TextColor.RED);
                response = null;
            }
        }while(response == null);
        return response;
    }

    /**
     * Asks the user to select one of the Enum values from the given list.
     * This assumes you are using SCREAMING_SNAKE_CASE in your enums.
     * <p>Example:<p>
     * MyEnum selection = Console.getEnumValue("Select an option", MyEnum.values(), false,
     *                         Console.TextColor.YELLOW, Console.TextColor.GREEN);
     * <p>
     * @param message The message to show the user under the Options list.
     * @param enumList The list of possible enumerations to show the user. Use the EnumName.values() method to get this.
     * @param allowEmpty If you allow the user to just hit entire and not pick one. If false, they are asked until they give a valid answer.
     * @param listColor The color of the options list values
     * @param messageColor The color of the message under the options list
     * @return Returns the selected enum value, or null if allowsEmpty was true and they just hit enter.
     * @param <T> This is a generic Enum type. Can be any Enumeration value.
     */
    public static <T extends Enum<T>> T getEnumValue(String message, T[] enumList, boolean allowEmpty, TextColor listColor, TextColor messageColor){
        if(enumList != null && enumList.length > 0 && enumList[0] != null) {
            do {
                String enumOptions = "Options: " + String.join(", ", java.util.Arrays.stream(enumList).map(Enum::name).map(name -> name.replace("_", " ")).toArray(String[]::new));
                writeLn(enumOptions, listColor);
                String response = getStringInput(message, allowEmpty, messageColor);
                if((response == null || response.isEmpty()) && allowEmpty){
                    return null;
                }
                try {
                    Class<T> enumType = (Class<T>) enumList[0].getDeclaringClass();
                    T chosenEnum = T.valueOf(enumType, response.trim().toUpperCase().replace(" ", "_"));
                    return chosenEnum;
                } catch (IllegalArgumentException ex) {
                    writeLn("You must pick one of the options and type it exactly (case insensitive).", TextColor.RED);
                    writeLn("");
                }
            } while (true);
        }
        return null;
    }

    /**
     * Gets a date from the user by asking for each part (Month, Day, Year)
     * @param message Message to show to the user as to what you are requesting
     * @param textColor ConsoleTextColor of the text when written
     * @return returns a java.time.LocalDate object populated with the users entered date
     */
    public static LocalDate getDateInput(String message, TextColor textColor) {
        do {
            if(message != null) {
                Console.writeLn(message, textColor);
            }
            boolean getInputOnSameLineCurrent = getInputOnSameLine;
            getInputOnSameLine = true;
            int month = getIntInput("Enter month (1-12): ",1, 12, TextColor.DEFAULT);
            int day = getIntInput("Enter day (1-31): ", 1, 31, TextColor.DEFAULT);
            int year = getIntInput("Enter year: ", 1000, 9999, TextColor.DEFAULT);
            getInputOnSameLine = getInputOnSameLineCurrent;
            try {
                return LocalDate.of(year, month, day);
            } catch (DateTimeException ex) {
                Console.writeLn("Invalid date, try again.", TextColor.RED);
            }
        }while(true);
    }

    /**
     * Gets a date from the user by asking for each part (Month, Day, Year)
     * Makes sure that the date typed falls between the two values provided.
     * @param message Message to show to the user as to what you are requesting
     * @param minDate The minimum date allowed
     * @param maxDate The maximum date allowed
     * @param textColor ConsoleTextColor of the text when written
     * @return returns a java.time.LocalDate object populated with the users entered date
     */
    public static LocalDate getDateInput(String message,LocalDate minDate, LocalDate maxDate, TextColor textColor) {
        do {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(defaultDateFormat);
            String betweenRequirement = String.format("between %s and %s", minDate.format(formatter), maxDate.format(formatter));
            if(message != null) {
                Console.writeLn(String.format("%s (%s):", message, betweenRequirement), textColor);
            }
            boolean getInputOnSameLineCurrent = getInputOnSameLine;
            getInputOnSameLine = true;
            int month = getIntInput("Enter month (1-12): ",1, 12, TextColor.DEFAULT);
            int day = getIntInput("Enter day (1-31): ", 1, 31, TextColor.DEFAULT);
            int year = getIntInput("Enter year: ", 1000, 9999, TextColor.DEFAULT);
            getInputOnSameLine = getInputOnSameLineCurrent;
            try {
                LocalDate parsedDate = LocalDate.of(year, month, day);
                if(parsedDate.isBefore(minDate) || parsedDate.isAfter(maxDate)){
                    throw new DateTimeException("Date out of range");
                }
                return parsedDate;
            } catch (DateTimeException ex) {
                Console.writeLn(String.format("Invalid date, must be %s and match the pattern. Try again.", betweenRequirement), TextColor.RED);
            }
        }while(true);
    }

    /**
     * Gets a date from the user by asking for a date that matches the defaultDateFormat of Console class.
     * @param message Message to show to the user as to what you are requesting
     * @param textColor ConsoleTextColor of the text when written
     * @return returns a java.time.LocalDate object populated with the users entered date
     */
    public static LocalDate getDateInputInline(String message, TextColor textColor) {
        do {
            String formatHelper = " [Format: \"" + defaultDateFormat + "\"]:";
            String dateSubmission = getStringInput(message + formatHelper, false, textColor);

            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(defaultDateFormat);
                return LocalDate.parse(dateSubmission, formatter);
            } catch (DateTimeException ex) {
                Console.writeLn("Invalid date, try again.", TextColor.RED);
            }
        }while(true);
    }

    /**
     * Gets a date from the user expecting it to match the defaultDateFormat of Console class
     * Makes sure that the date typed falls between the minDate and maxDate.
     * @param message Message to show to the user as to what you are requesting
     * @param minDate The minimum date allowed
     * @param maxDate The maximum date allowed
     * @param textColor ConsoleTextColor of the text when written
     * @return returns a java.time.LocalDate object populated with the users entered date
     */
    public static LocalDate getDateInputInline(String message, LocalDate minDate, LocalDate maxDate, TextColor textColor){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(defaultDateFormat);
        LocalDate date;
        do {
            date = getDateInputInline(String.format("%s (between %s and %s)", message, minDate.format(formatter), maxDate.format(formatter)), textColor);
            if(date.isBefore(minDate) || date.isAfter(maxDate)){
                date = null;
                String betweenRequirement = String.format("between %s and %s", minDate.format(formatter), maxDate.format(formatter));
                Console.writeLn(String.format("Invalid date, must be %s. Try again.", betweenRequirement), TextColor.RED);
            }
        }while(date == null);

        return date;
    }

    /**
     * Writes to the console using the default text color and background color.
     * Does not append the newline. Text stays on the same line.
     * @param message The message to write to the console
     */
    public static void write(String message){
        write(message, TextColor.DEFAULT, BackgroundColor.DEFAULT);
    }

    /**
     * Writes to the console using the provided text color and default background color.
     * Does not append the newline. Text stays on the same line.
     * @param message The message to write to the console
     * @param textColor The ConsoleTextColor to write the text in
     */
    public static void write(String message, TextColor textColor){
        write(message, textColor, BackgroundColor.DEFAULT);
    }

    /**
     * Writes the message to the console using the provided text and background color
     * Does not append the newline. Text stays on the same line.
     * @param message The message to write to the console
     * @param textColor The ConsoleTextColor to write the text in
     * @param backgroundColor The ConsoleBackgroundColor to place behind the text
     */
    public static void write(String message, TextColor textColor, BackgroundColor backgroundColor){
        System.out.print(getBackgroundColorText(backgroundColor) + getTextColorText(textColor) + message + RESET);
    }

    /**
     * Writes the message to the console using the provided text and RGB color
     * Does not append the newline. Text stays on the same line.
     * @param message The message to write to the console
     * @param red Amount of red value in the color
     * @param green Amount of green value in the color
     * @param blue Amount of blue value in the color
     * */
    public static void write(String message, int red, int green, int blue) {
        System.out.print(getANSIColorString(red, green, blue, false) + message + RESET);
    }

    /**
     * Writes the message to the console using the provided text and RGB color
     * Does not append the newline. Text stays on the same line.
     * @param message The message to write to the console
     * @param red Amount of red value in the color
     * @param green Amount of green value in the color
     * @param blue Amount of blue value in the color
     * @param redBack Amount of red value in the background color
     * @param greenBack Amount of green value in the background color
     * @param blueBack Amount of blue value in the background color
     * */
    public static void write(String message, int red, int green, int blue, int redBack, int greenBack, int blueBack) {
        System.out.print(
                getANSIColorString(red, green, blue, false) +
                getANSIColorString(redBack, greenBack, blueBack, true) +
                message +
                RESET
        );
    }

    /**
     * Writes the message to the console using the provided text and RGB color
     * Does not append the newline. Text stays on the same line.
     * @param message The message to write to the console
     * @param textColor The color for the text
     * */
    public static void write(String message, Color32 textColor) {
        System.out.print(getANSIColorString(textColor, false) + message + RESET);
    }

    /**
     * Writes the message to the console using the provided text and RGB color
     * Does not append the newline. Text stays on the same line.
     * @param message The message to write to the console
     * @param textColor The color for the text
     * @param backgroundColor The color for the background
     * */
    public static void write(String message, Color32 textColor, Color32 backgroundColor) {
        System.out.print(
                getANSIColorString(textColor, false) +
                getANSIColorString(backgroundColor, true) +
                message +
                RESET
        );
    }

    /**
     * Writes to the console using the default text color and background color.
     * Appends the newline to the end.
     * @param message The message to write to the console
     */
    public static void writeLn(String message){ writeLn(message, TextColor.DEFAULT, BackgroundColor.DEFAULT);}

    /**
     * Writes to the console using the provided text color and default background color.
     * Appends the newline to the end.
     * @param message The message to write to the console
     * @param textColor The ConsoleTextColor to write the text in
     */
    public static void writeLn(String message, TextColor textColor) { writeLn(message, textColor, BackgroundColor.DEFAULT);}

    /**
     * Writes the message to the console using the provided text and background color
     * Append the newline to the end.
     * @param message The message to write to the console
     * @param textColor The ConsoleTextColor to write the text in
     * @param backgroundColor The ConsoleBackgroundColor to place behind the text
     */
    public static void writeLn(String message, TextColor textColor, BackgroundColor backgroundColor){
        System.out.println(getBackgroundColorText(backgroundColor) + getTextColorText(textColor) + message + RESET);
    }

    /**
     * Writes the message to the console using the provided text and RGB color
     * Append the newline to the end.
     * @param message The message to write to the console
     * @param red Amount of red value in the color
     * @param green Amount of green value in the color
     * @param blue Amount of blue value in the color
     * @param redBack Amount of red value in the background color
     * @param greenBack Amount of green value in the background color
     * @param blueBack Amount of blue value in the background color
     * */
    public static void writeLn(String message, int red, int green, int blue, int redBack, int greenBack, int blueBack) {
        System.out.println(
                getANSIColorString(red, green, blue, false) +
                        getANSIColorString(redBack, greenBack, blueBack, true) +
                        message +
                        RESET
        );
    }

    /**
     * Writes the message to the console using the provided text and RGB color
     * Append the newline to the end.
     * @param message The message to write to the console
     * @param textColor The color for the text
     * */
    public static void writeLn(String message, Color32 textColor) {
        System.out.println(getANSIColorString(textColor, false) + message + RESET);
    }

    /**
     * Writes the message to the console using the provided text and RGB color
     * Append the newline to the end.
     * @param message The message to write to the console
     * @param textColor The color for the text
     * @param backgroundColor The color for the background
     * */
    public static void writeLn(String message, Color32 textColor, Color32 backgroundColor) {
        System.out.println(
                getANSIColorString(textColor, false) +
                        getANSIColorString(backgroundColor, true) +
                        message +
                        RESET
        );
    }

    /**
     * "Clears" the console by writing 50 blank lines since Java doesn't natively support it
     */
    public static void clear(){
       clear(50);
    }

    /**
     * "Clears" the console by writing 50 blank lines since Java doesn't natively support it
     * @param emptyLineCount The number of empty lines to print
     */
    public static void clear(int emptyLineCount){
        for (int i = 0; i < emptyLineCount; i++) {
            System.out.println();
        }
    }

    /**
     * Writes the string to the console using System.out.print,
     * but it's more exciting 🌈
     * @param message The message to write
     */
    public static void writeRainbow(String message){
        writeRainbow(message, 0, (1.0f / message.length()) * 360);
    }

    /**
     * Writes the string to the console using System.out.print,
     * but it's more exciting 🌈
     * @param message The message to write
     * @param rotateHueAmount Amount to rotate the hue by each character
     * @param startingHue Where the hue should start
     */
    public static void writeRainbow(String message, float startingHue, float rotateHueAmount){
        Color32 color = new Color32(0);

        float rotation = startingHue;
        for (int c = 0; c < message.length(); c++) {
            color.setHSLValues(rotation, 1, 0.5f);
            write(String.valueOf(message.charAt(c)), color);
            rotation += rotateHueAmount;
        }
    }

    /**
     * Writes the string to the console using System.out.print alternating colors for each character
     * Uses the colors from the passed in TextColors array.
     * Spaces do not cause the color to change.
     * @param message The message to write
     * @param colors Array of colors to cycle through. <strong>It must be either an array of Color32 or TextColor.</strong>
     */
    public static <T> void writeMultiColored(String message, T[] colors) {
        if(message.isEmpty() || colors == null || colors.length == 0){
            writeLn(message);
            return;
        }
        if (colors[0] instanceof Color32 || colors[0] instanceof TextColor){
            int colorIndexToUse = 0;
            for (char c : message.toCharArray()){
                if(Character.isWhitespace(c)){
                    write(String.valueOf(c));
                    continue;
                }
                if (colors[colorIndexToUse] instanceof Color32 color32){
                    write(String.valueOf(c), color32);
                } else if (colors[colorIndexToUse] instanceof TextColor textColor){
                    write(String.valueOf(c), textColor);
                }
                colorIndexToUse++;
                colorIndexToUse %= colors.length;
            }
        } else {
            throw new IllegalArgumentException("Color must be a Color32 or TextColor");
        }
    }

    //region writeLnCustoms (expand)
    public static void writeLnYellow(String text){
        System.out.println(Console.YELLOW + text + RESET);
    }
    public static void writeLnBlue(String text){
        System.out.println(Console.BLUE + text + RESET);
    }
    public static void writeLnRed(String text){
        System.out.println(Console.RED + text + RESET);
    }
    public static void writeLnGreen(String text){
        System.out.println(Console.GREEN + text + RESET);
    }
    public static void writeLnPurple(String text){
        System.out.println(Console.PURPLE + text + RESET);
    }
    public static void writeLnCyan(String text){
        System.out.println(Console.CYAN + text + RESET);
    }
    public static void writeLnWhite(String text){
        System.out.println(Console.WHITE + text + RESET);
    }
    public static void writeLnBlack(String text){
        System.out.println(Console.BLACK + text + RESET);
    }

    public static void writeYellow(String text){
        System.out.print(Console.YELLOW + text + RESET);
    }
    public static void writeBlue(String text){
        System.out.print(Console.BLUE + text + RESET);
    }
    public static void writeRed(String text){
        System.out.print(Console.RED + text + RESET);
    }
    public static void writeGreen(String text){
        System.out.print(Console.GREEN + text + RESET);
    }
    public static void writePurple(String text){
        System.out.print(Console.PURPLE + text + RESET);
    }
    public static void writeCyan(String text){
        System.out.print(Console.CYAN + text + RESET);
    }
    public static void writeWhite(String text){
        System.out.print(Console.WHITE + text + RESET);
    }
    public static void writeBlack(String text){
        System.out.print(Console.BLACK + text + RESET);
    }

    //endregion

    //BLACK, RED, GREEN, YELLOW, BLUE, PURPLE, CYAN, WHITE
    private static String getTextColorText(TextColor textColor){
        switch(textColor){
            case RED:
                return Console.RED;
            case GREEN:
                return Console.GREEN;
            case YELLOW:
                return Console.YELLOW;
            case BLUE:
                return Console.BLUE;
            case PURPLE:
                return Console.PURPLE;
            case CYAN:
                return Console.CYAN;
            case BLACK:
                return Console.BLACK;
            case WHITE:
                return Console.WHITE;
            default:
                return RESET;
        }
    }

    private static String getBackgroundColorText(BackgroundColor backgroundColor){
        switch(backgroundColor){
            case RED:
                return Console.RED_BACKGROUND;
            case GREEN:
                return Console.GREEN_BACKGROUND;
            case YELLOW:
                return Console.YELLOW_BACKGROUND;
            case BLUE:
                return Console.BLUE_BACKGROUND;
            case PURPLE:
                return Console.PURPLE_BACKGROUND;
            case CYAN:
                return Console.CYAN_BACKGROUND;
            case BLACK:
                return Console.BLACK_BACKGROUND;
            case WHITE:
                return Console.WHITE_BACKGROUND;
            default:
                return RESET;
        }
    }

    private static String getANSIColorString(int red, int green, int blue, boolean isBackground) {
        StringBuilder sb = new StringBuilder(20);
        sb.append(isBackground ? "\u001B[38;5;" : "\u001B[38;2;");
        sb.append(red); sb.append(';');
        sb.append(green); sb.append(';');
        sb.append(blue); sb.append('m');
        return sb.toString();
    }

    private static String getANSIColorString(Color32 color, boolean isBackground) {
        StringBuilder sb = new StringBuilder(20);
        sb.append(isBackground ? "\u001B[48;2;" : "\u001B[38;2;");
        sb.append(color.getRed()); sb.append(';');
        sb.append(color.getGreen()); sb.append(';');
        sb.append(color.getBlue()); sb.append('m');
        return sb.toString();
    }

    /**
     * Closes the reader used for this instance.
     * Call this before exiting your application.
     */
    public static void close(){
        try {
            if (br != null) {
                br.close();
            }
        } catch (IOException e) {
            System.out.println("An error occurred while closing the input stream.");
        }
    }
}
