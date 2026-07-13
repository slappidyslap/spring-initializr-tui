package dev.danvega.initializr.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record BootVersion(
        int major,
        int minor,
        int patch,
        Type type,
        int typeVersion
) implements Comparable<BootVersion> {

    private static final Pattern PATTERN = Pattern.compile(
            "(\\d+)\\.(\\d+)\\.(\\d+)\\.(BUILD-SNAPSHOT|RELEASE|M\\d+|RC\\d+)"
    );

    public enum Type {
        SNAPSHOT,
        MILESTONE,
        RC,
        RELEASE
    }

    public static BootVersion parse(String value) {
        if (value == null || value.isBlank()) return null;

        Matcher matcher = PATTERN.matcher(value);

        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Unsupported boot version: " + value
            );
        }

        Type type;
        int typeVersion = 0;
        String qualifier = matcher.group(4);
        if (qualifier.equals("BUILD-SNAPSHOT")) {
            type = Type.SNAPSHOT;
        } else if (qualifier.equals("RELEASE")) {
            type = Type.RELEASE;
        } else if (qualifier.startsWith("M")) {
            type = Type.MILESTONE;
            typeVersion = Integer.parseInt(
                    qualifier.substring(1)
            );
        } else if (qualifier.startsWith("RC")) {
            type = Type.RC;
            typeVersion = Integer.parseInt(
                    qualifier.substring(2)
            );
        } else {
            throw new IllegalArgumentException(value);
        }


        return new BootVersion(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)),
                type,
                typeVersion
        );
    }


    @Override
    public int compareTo(BootVersion other) {
        int result;

        result = Integer.compare(major, other.major);
        if (result != 0) return result;

        result = Integer.compare(minor, other.minor);
        if (result != 0) return result;

        result = Integer.compare(patch, other.patch);
        if (result != 0) return result;

        result = Integer.compare(weight(type), weight(other.type));
        if (result != 0) return result;

        return Integer.compare(typeVersion, other.typeVersion);
    }


    private static int weight(Type type) {
        return switch (type) {
            case SNAPSHOT -> 0;
            case MILESTONE -> 1;
            case RC -> 2;
            case RELEASE -> 3;
        };
    }

    @Override
    public String toString() {
        return "%d.%d.%d.%s".formatted(
                major,
                minor,
                patch,
                switch (type) {
                    case SNAPSHOT -> "BUILD-SNAPSHOT";
                    case RELEASE -> "RELEASE";
                    case MILESTONE -> "M" + typeVersion;
                    case RC -> "RC" + typeVersion;
                }
        );
    }
}
