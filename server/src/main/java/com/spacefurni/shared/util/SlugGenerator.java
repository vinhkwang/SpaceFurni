package com.spacefurni.shared.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.function.Predicate;

public final class SlugGenerator {

    private SlugGenerator() {
    }

    public static String generateUniqueSlug(String sourceText, Predicate<String> slugAlreadyExists) {
        String baseSlug = slugify(sourceText);
        String candidateSlug = baseSlug;
        int suffix = 2;
        while (slugAlreadyExists.test(candidateSlug)) {
            candidateSlug = baseSlug + "-" + suffix;
            suffix++;
        }
        return candidateSlug;
    }

    private static String slugify(String sourceText) {
        String withoutDiacritics = Normalizer.normalize(sourceText, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return withoutDiacritics.toLowerCase(Locale.ROOT).trim().replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }
}
