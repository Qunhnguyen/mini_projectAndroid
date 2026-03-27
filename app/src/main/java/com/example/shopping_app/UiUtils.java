package com.example.shopping_app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import java.text.NumberFormat;
import java.util.Locale;

public final class UiUtils {
    private static final Locale VIETNAMESE = new Locale("vi", "VN");
    private static final int[] PRODUCT_SWATCHES = {
            R.color.swatch_1,
            R.color.swatch_2,
            R.color.swatch_3,
            R.color.swatch_4,
            R.color.swatch_5
    };

    private UiUtils() {
    }

    public static String formatCurrency(double amount) {
        return NumberFormat.getCurrencyInstance(VIETNAMESE).format(amount);
    }

    public static String formatOrderCode(int orderId) {
        return String.format(Locale.getDefault(), "ODR-%04d", orderId);
    }

    public static String buildInitials(String value) {
        if (value == null) {
            return "?";
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return "?";
        }

        String[] tokens = trimmed.split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String token : tokens) {
            if (!token.isEmpty()) {
                builder.append(Character.toUpperCase(token.charAt(0)));
            }
            if (builder.length() == 2) {
                break;
            }
        }

        if (builder.length() == 0) {
            builder.append(Character.toUpperCase(trimmed.charAt(0)));
        }

        return builder.toString();
    }

    public static void bindProductBadge(TextView textView, String seedText) {
        Context context = textView.getContext();
        int colorRes = resolveSwatch(seedText);
        int fillColor = ContextCompat.getColor(context, colorRes);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fillColor);
        drawable.setCornerRadius(28f * context.getResources().getDisplayMetrics().density);
        textView.setBackground(drawable);
        textView.setText(buildInitials(seedText));
        textView.setTextColor(Color.WHITE);
    }

    private static int resolveSwatch(String seedText) {
        int index = 0;
        if (seedText != null && !seedText.isEmpty()) {
            index = Math.abs(seedText.hashCode()) % PRODUCT_SWATCHES.length;
        }
        return PRODUCT_SWATCHES[index];
    }
}
