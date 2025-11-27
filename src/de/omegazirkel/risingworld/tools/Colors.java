package de.omegazirkel.risingworld.tools;

/**
 * This class is used to have a syncronized color theme through different
 * plugins
 */
public class Colors {

    public final String error = "<color=#FF0000>";
    public final String warning = "<color=#808000>";
    public final String okay = "<color=#00FF00>";
    public final String text = "<color=#EEEEEE>";
    public final String command = "<color=#997d4a>";
    public final String info = "<color=#0099ff>";
    public final String comment = "<color=#478c59>";
    public final String endTag = "</color>";

    protected static Colors C = new Colors();

    /**
     * 
     * @return
     */
    public static Colors getInstance() {
        return C;
    }

    /**
     * 
     */
    private Colors() {

    }
}