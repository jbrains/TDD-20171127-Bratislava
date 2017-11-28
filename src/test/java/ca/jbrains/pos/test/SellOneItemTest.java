package ca.jbrains.pos.test;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import org.junit.Assert;
import org.junit.Test;

public class SellOneItemTest {
    @Test
    public void productFound() throws Exception {
        final Display display = new Display();
        final MessageFormat messageFormat = new EnglishLanguageMessageFormat();
        final Sale sale = new Sale(display, HashMap.of(
                "12345", "€ 5.50",
                "23456", "€ 7.95"), messageFormat);

        sale.onBarcode("12345");

        Assert.assertEquals("€ 5.50", display.getText());
    }

    @Test
    public void anotherProductFound() throws Exception {
        final Display display = new Display();
        final MessageFormat messageFormat = new EnglishLanguageMessageFormat();
        final Sale sale = new Sale(display, HashMap.of(
                "12345", "€ 5.50",
                "23456", "€ 7.95"), messageFormat);

        sale.onBarcode("23456");

        Assert.assertEquals("€ 7.95", display.getText());
    }

    @Test
    public void productNotFound() throws Exception {
        final Display display = new Display();
        final MessageFormat messageFormat = new EnglishLanguageMessageFormat();
        final Sale sale = new Sale(display, HashMap.of(
                "12345", "€ 5.50",
                "23456", "€ 7.95"), messageFormat);

        sale.onBarcode("99999");

        Assert.assertEquals("Product not found for 99999", display.getText());
    }

    @Test
    public void emptyBarcode() throws Exception {
        final Display display = new Display();
        final MessageFormat messageFormat = new EnglishLanguageMessageFormat();
        final Sale sale = new Sale(display, null, messageFormat);

        sale.onBarcode("");

        Assert.assertEquals("Scanning error: empty barcode", display.getText());

    }

    public static class Sale {
        private final Map<String, String> pricesByBarcode;
        private final MessageFormat messageFormat;
        private final Catalog catalog;
        private Display display;

        public Sale(final Display display, final Map<String, String> pricesByBarcode, final MessageFormat messageFormat) {
            this.display = display;
            this.pricesByBarcode = pricesByBarcode;
            this.catalog = new InMemoryCatalog(pricesByBarcode);
            this.messageFormat = messageFormat;
        }

        public void onBarcode(final String barcode) {
            if ("".equals(barcode)) {
                final String message = messageFormat.formatEmptyBarcodeMessage();
                display.setText(message);
            } else {
                final Option<String> maybePrice = findPrice(barcode);
                final Option<String> maybeProductFoundMessage = maybePrice.map(messageFormat::formatProductFoundMessage);
                final String message = maybeProductFoundMessage.getOrElse(messageFormat.formatProductNotFoundMessage(barcode));
                display.setText(message);
            }
        }

        private Option<String> findPrice(final String barcode) {
            return pricesByBarcode.get(barcode);
        }
    }

    public interface Catalog {}

    public static class InMemoryCatalog implements Catalog {
        private final Map<String, String> pricesByBarcode;

        public InMemoryCatalog(final Map<String, String> pricesByBarcode) {
            this.pricesByBarcode = pricesByBarcode;
        }
    }

    public interface MessageFormat {
        String formatProductFoundMessage(String price);

        String formatProductNotFoundMessage(String barcode);

        String formatEmptyBarcodeMessage();
    }

    public static class EnglishLanguageMessageFormat implements MessageFormat {
        @Override
        public String formatProductFoundMessage(final String price) {
            return price;
        }

        @Override
        public String formatProductNotFoundMessage(final String barcode) {
            return String.format("Product not found for %s", barcode);
        }

        @Override
        public String formatEmptyBarcodeMessage() {
            return "Scanning error: empty barcode";
        }
    }

    public static class Display {
        private String text;

        public String getText() {
            return text;
        }

        public void setText(final String text) {
            this.text = text;
        }
    }
}
