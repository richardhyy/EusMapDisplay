package cc.eumc.eusmapdisplay.util;


import net.kyori.adventure.text.Component;

import java.lang.reflect.Field;

public class TextComponentUtil {
    public static String getContent(Component component) {
        try {
            Field contentField;
            contentField = component.getClass().getDeclaredField("content");
            contentField.setAccessible(true);
            return (String) contentField.get(component);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
