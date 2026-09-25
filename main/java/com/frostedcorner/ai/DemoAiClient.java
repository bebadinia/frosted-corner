package com.frostedcorner.ai;

import com.frostedcorner.assistant.AssistantIntentType;
import com.frostedcorner.assistant.CustomerIntent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class DemoAiClient implements AiClient {

    private static final Pattern DIGIT_COUNT_PATTERN = Pattern.compile(
            "(?:for|of)?\\s*(\\d+)\\s+(?:people|guests|desserts|cupcakes|cookies|brownies)");
    private static final Pattern ADD_DIGIT_PATTERN = Pattern.compile(
            "add\\s+(\\d+)\\s+(?:of\\s+)?(?:that|those|them)");
    private static final Map<String, Integer> NUMBER_WORDS = Map.ofEntries(
            Map.entry("one", 1),
            Map.entry("two", 2),
            Map.entry("three", 3),
            Map.entry("four", 4),
            Map.entry("five", 5),
            Map.entry("six", 6),
            Map.entry("seven", 7),
            Map.entry("eight", 8),
            Map.entry("nine", 9),
            Map.entry("ten", 10),
            Map.entry("eleven", 11),
            Map.entry("twelve", 12)
    );

    @Override
    public CustomerIntent interpret(String message) {
        String normalized = normalize(message);
        if (normalized.isBlank()) {
            return new CustomerIntent(AssistantIntentType.UNSUPPORTED, null, null,
                    null, null, null, false, List.of());
        }

        String event = detectEvent(normalized);
        String flavor = detectFlavor(normalized);
        Integer servings = detectServingCount(normalized);
        Integer quantity = detectAddQuantity(normalized);
        List<String> keywords = detectKeywords(normalized);
        String faqIntent = detectFaqIntent(normalized);
        boolean addToCart = normalized.contains("add") && normalized.contains("cart");
        boolean addAll = normalized.contains("those") || normalized.contains("them");

        if (addToCart) {
            return new CustomerIntent(AssistantIntentType.ADD_TO_CART, event, flavor,
                    servings, quantity, null, addAll, keywords);
        }

        if (faqIntent != null) {
            return new CustomerIntent(AssistantIntentType.FAQ, event, flavor,
                    servings, null, faqIntent, false, keywords);
        }

        if (event != null || flavor != null || servings != null || !keywords.isEmpty()
                || isRecommendationRequest(normalized)) {
            return new CustomerIntent(AssistantIntentType.RECOMMENDATION, event, flavor,
                    servings, quantity, null, false, keywords);
        }

        return new CustomerIntent(AssistantIntentType.UNSUPPORTED, null, null,
                null, null, null, false, List.of());
    }

    private String normalize(String message) {
        return message == null
                ? ""
                : message.toLowerCase(Locale.US).replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
    }

    private String detectEvent(String normalized) {
        if (normalized.contains("birthday")) {
            return "Birthday";
        }
        if (normalized.contains("christmas") || normalized.contains("holiday")) {
            return "Christmas";
        }
        if (normalized.contains("party")) {
            return "Party";
        }
        return null;
    }

    private String detectFlavor(String normalized) {
        for (String flavor : List.of("chocolate", "vanilla", "strawberry", "caramel",
                "red velvet", "cinnamon", "pumpkin", "peppermint")) {
            if (normalized.contains(flavor)) {
                return titleCase(flavor);
            }
        }
        return null;
    }

    private Integer detectServingCount(String normalized) {
        Matcher digitMatcher = DIGIT_COUNT_PATTERN.matcher(normalized);
        if (digitMatcher.find()) {
            return Integer.valueOf(digitMatcher.group(1));
        }

        for (Map.Entry<String, Integer> entry : NUMBER_WORDS.entrySet()) {
            String token = entry.getKey() + " people";
            if (normalized.contains(token) || normalized.contains(entry.getKey() + " guests")) {
                return entry.getValue();
            }
        }

        return null;
    }

    private Integer detectAddQuantity(String normalized) {
        Matcher digitMatcher = ADD_DIGIT_PATTERN.matcher(normalized);
        if (digitMatcher.find()) {
            return Integer.valueOf(digitMatcher.group(1));
        }

        for (Map.Entry<String, Integer> entry : NUMBER_WORDS.entrySet()) {
            if (normalized.contains("add " + entry.getKey() + " of")
                    || normalized.contains("add " + entry.getKey() + " those")
                    || normalized.contains("add " + entry.getKey() + " them")
                    || normalized.contains("add " + entry.getKey() + " that")) {
                return entry.getValue();
            }
        }

        return normalized.contains("add") ? 1 : null;
    }

    private List<String> detectKeywords(String normalized) {
        Map<String, String> supportedKeywords = new LinkedHashMap<>();
        supportedKeywords.put("cake", "cake");
        supportedKeywords.put("cupcake", "cupcake");
        supportedKeywords.put("cookie", "cookie");
        supportedKeywords.put("brownie", "brownie");
        supportedKeywords.put("croissant", "croissant");
        supportedKeywords.put("cheesecake", "cheesecake");
        supportedKeywords.put("dessert", "dessert");
        supportedKeywords.put("menu", "menu");
        List<String> matches = new ArrayList<>();
        supportedKeywords.forEach((token, keyword) -> {
            if (normalized.contains(token)) {
                matches.add(keyword);
            }
        });
        return matches;
    }

    private String detectFaqIntent(String normalized) {
        if (normalized.contains("menu") || normalized.contains("categories")
                || normalized.contains("what do you have")) {
            return "MENU";
        }
        if (normalized.contains("how do i order") || normalized.contains("how ordering works")
                || normalized.contains("how does ordering work")) {
            return "ORDERING";
        }
        if (normalized.contains("delivery") || normalized.contains("takeout")
                || normalized.contains("pickup")) {
            return "FULFILLMENT";
        }
        if (normalized.equals("help") || normalized.contains("what can you do")) {
            return "HELP";
        }
        return null;
    }

    private boolean isRecommendationRequest(String normalized) {
        return normalized.contains("recommend")
                || normalized.contains("show me")
                || normalized.contains("need desserts")
                || normalized.contains("something")
                || normalized.contains("craving")
                || normalized.contains("for ");
    }

    private String titleCase(String input) {
        String[] words = input.split(" ");
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < words.length; index++) {
            if (index > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(words[index].charAt(0)))
                    .append(words[index].substring(1));
        }
        return builder.toString();
    }
}