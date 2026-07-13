package dev.danvega.initializr.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record BootVersionRange(
        String lowerOperator,
        BootVersion lower,
        String upperOperator,
        BootVersion upper
) {

    private static final Pattern PATTERN = Pattern.compile(
            "([\\[(])" +
                    "(\\d+\\.\\d+\\.\\d+\\.(?:RELEASE|M\\d+|RC\\d+))," +
                    "(\\d+\\.\\d+\\.\\d+\\.(?:RELEASE|M\\d+|RC\\d+))" +
                    "([])])"
    );


    public static BootVersionRange parse(String value) {
        if (value == null) {
            return null;
        }

        Matcher matcher = PATTERN.matcher(value);

        if (!matcher.matches()) {
            return null;
        }

        return new BootVersionRange(
                matcher.group(1).equals("[") ? ">=" : ">",
                BootVersion.parse(matcher.group(2)),
                matcher.group(4).equals("]") ? "<=" : "<",
                BootVersion.parse(matcher.group(3))
        );
    }


    public boolean supports(BootVersion version) {

        boolean lowerOk = switch (lowerOperator) {
            case ">=" -> version.compareTo(lower) >= 0;
            case ">" -> version.compareTo(lower) > 0;
            default -> true;
        };


        boolean upperOk = switch (upperOperator) {
            case "<=" -> version.compareTo(upper) <= 0;
            case "<" -> version.compareTo(upper) < 0;
            default -> true;
        };


        return lowerOk && upperOk;
    }


    @Override
    public String toString() {
        return "[Requires Spring Boot %s %s %s %s]"
                .formatted(
                        lowerOperator,
                        lower,
                        upperOperator,
                        upper
                );
    }
}
