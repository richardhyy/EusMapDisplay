package cc.eumc.eusmapdisplay.model;

public class DisplayCursor {
    static transient String NormalCursor = "1000000000000\n" +
            "1100000000000\n" +
            "1210000000000\n" +
            "1221000000000\n" +
            "1222100000000\n" +
            "1222210000000\n" +
            "1222221000000\n" +
            "1222222100000\n" +
            "1222222210000\n" +
            "1222222221000\n" +
            "1222222222100\n" +
            "1222222222210\n" +
            "1222222111111\n" +
            "1222122100000\n" +
            "1221122100000\n" +
            "1210012210000\n" +
            "1100012210000\n" +
            "1000001221000\n" +
            "0000001221000\n" +
            "0000000110000";
    private byte fillColor;
    private byte outlineColor;
    private byte[][] pixels;
    private int offsetX;
    private int offsetY;

    public DisplayCursor(byte fillColor, byte outlineColor) {
        this.pixels = new byte[24][24];
        this.fillColor = fillColor;
        this.outlineColor = outlineColor;
        setNormal(this, fillColor, outlineColor);
    }

    public byte[][] getPixels() {
        return pixels;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    public byte getFillColor() {
        return fillColor;
    }

    public byte getOutlineColor() {
        return outlineColor;
    }

    public static void setNormal(DisplayCursor cursor, byte fillColor, byte outlineColor) {
        cursor.setOffsetX(-2);
        cursor.setOffsetY(-3);

        byte[][] pixels = cursor.getPixels();

        String[] lines = NormalCursor.split("\n");
        for (int y = 0; y < lines.length; y++) {
            for (int x = 0; x < lines[y].length(); x++) {
                switch (lines[y].charAt(x)) {
                    case '1':
                        pixels[x][y] = outlineColor;
                        break;
                    case '2':
                        pixels[x][y] = fillColor;
                        break;
                }
            }
        }
//        for (int y=0; y<18; y++) {
//            pixels[0][y] = outlineColor;
//        }
//
//        for (int x=1; x<13; x++) {
//            pixels[x][x] = outlineColor;
//        }
//
//        for (int x=1; x<4; x++) {
//            pixels[x][17-x] = outlineColor;
//        }
//
//        for (int x=7; x<12; x++) {
//            pixels[x][12] = outlineColor;
//        }
//
//        int x = 4;
//        for (int y=13; y<19; y++) {
//            pixels[x][y] = outlineColor;
//            pixels[x][y+3] = outlineColor;
//            if (y%2 == 0) {
//                x++;
//            }
//        }
//
//        pixels[7][19] = outlineColor;
//        pixels[8][19] = outlineColor;
    }
}
