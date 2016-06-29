package co.neweden.perks.extras;

public class Util {

    private Util() {  }

    public static String formatString(String test) {
        return test.replaceAll("&([a-z0-9])", "\u00A7$1");
    }

}
