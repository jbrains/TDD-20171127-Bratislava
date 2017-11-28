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
        final Sale sale = new Sale(display, HashMap.of(
                "12345", "€ 5.50",
                "23456", "€ 7.95"));

        sale.onBarcode("12345");

        Assert.assertEquals("€ 5.50", display.getText());
    }

    @Test
    public void anotherProductFound() throws Exception {
        final Display display = new Display();
        final Sale sale = new Sale(display, HashMap.of(
                "12345", "€ 5.50",
                "23456", "€ 7.95"));

        sale.onBarcode("23456");

        Assert.assertEquals("€ 7.95", display.getText());
    }

    @Test
    public void productNotFound() throws Exception {
        final Display display = new Display();
        final Sale sale = new Sale(display, HashMap.of(
                "12345", "€ 5.50",
                "23456", "€ 7.95"));

        sale.onBarcode("99999");

        Assert.assertEquals("Product not found for 99999", display.getText());
    }

    @Test
    public void emptyBarcode() throws Exception {
        final Display display = new Display();
        final Sale sale = new Sale(display, null);

        sale.onBarcode("");

        Assert.assertEquals("Scanning error: empty barcode", display.getText());

    }

    public static class Sale {
        private Display display;
        private final Map<String, String> pricesByBarcode;

        public Sale(final Display display, final Map<String, String> pricesByBarcode) {
            this.display = display;
            this.pricesByBarcode = pricesByBarcode;
        }

        public void onBarcode(final String barcode) {
            if ("".equals(barcode)) {
                display.setText("Scanning error: empty barcode");
            }
            else {
                final Option<String> maybePrice = pricesByBarcode.get(barcode);
                final String message = maybePrice.getOrElse(
                                String.format("Product not found for %s", barcode));
                display.setText(message);
            }
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
