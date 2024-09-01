package com.github.lkiran.util;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ImportHelper {

    private static final Pattern GENERIC_PATTERN = Pattern.compile("([\\w\\.]+)<(.+)>");

    public static String getSimpleName(String fullyQualifiedType) {
        Matcher matcher = GENERIC_PATTERN.matcher(fullyQualifiedType);

        if (matcher.matches()) {
            String rawType = matcher.group(1);
            String genericType = matcher.group(2);

            // Simplify the raw type
            String simpleRawType = rawType.substring(rawType.lastIndexOf('.') + 1);

            // Recursively simplify the generic type
            String simpleGenericType = getSimpleName(genericType);

            return simpleRawType + "<" + simpleGenericType + ">";
        }

        // If it's not a generic type, simplify the type directly
        return fullyQualifiedType.substring(fullyQualifiedType.lastIndexOf('.') + 1);
    }


    public static Set<String> getImports(String fullyQualifiedType) {
        Set<String> imports = new HashSet<>();
        extractImportsRecursive(fullyQualifiedType, imports);
        return imports;
    }

    private static void extractImportsRecursive(String type, Set<String> imports) {
        Matcher matcher = GENERIC_PATTERN.matcher(type);

        if (matcher.matches()) {
            String rawType = matcher.group(1);
            String genericType = matcher.group(2);

            // Add raw type to imports
            imports.add(rawType);

            // Recursively process the generic type
            extractImportsRecursive(genericType, imports);
        } else {
            // If it's a non-generic type, add it to imports
            imports.add(type);
        }
    }

}
