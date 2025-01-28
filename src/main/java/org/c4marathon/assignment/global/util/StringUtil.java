package org.c4marathon.assignment.global.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.helpers.MessageFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StringUtil {
    public static String format(String format, Object... objects) {
        return MessageFormatter.arrayFormat(format, objects).getMessage();
    }
}
