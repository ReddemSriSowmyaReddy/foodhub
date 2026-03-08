package com.example.foodhub;


import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HelloController {

    private static final Logger logger = Logger.getLogger(HelloController.class.getName());

    @FXML private VBox menuVBox;
    @FXML private VBox billVBox;
    @FXML private Label subTotalLabel;
    @FXML private Label gstLabel;
    @FXML private Label taxLabel;
    @FXML private Label grandTotalLabel;
    @FXML private ComboBox<String> paymentOption;

    private final HashMap<String, CartItem> cart = new HashMap<>();

    private static final double GST_RATE = 0.05;
    private static final double TAX_RATE = 0.03;

    private final MenuItem[] menuItems = {
            new MenuItem("Burger", 150, "burger.png"),
            new MenuItem("Pizza", 250, "pizza.png"),
            new MenuItem("Coke", 40, "coke.png"),
            new MenuItem("Fries", 80, "fries.png"),
            new MenuItem("Sandwich", 120, "sandwich.png"),
            new MenuItem("Shawarma", 180, "shawarma.png"),
            new MenuItem("Milkshake", 110, "milkshake.png"),
            new MenuItem("Fruit Juice", 90, "juice.png"),
            new MenuItem("Ice Cream", 100, "icecream.png")
    };

    @FXML
    public void initialize() {

        // Payment options
        paymentOption.getItems().addAll("Cash", "Online Payment");
        paymentOption.setValue("Cash");

        // Create menu rows
        for (MenuItem item : menuItems) {
            HBox row = new HBox(10);
            row.setStyle("-fx-background-color: #FFF8DC; -fx-padding: 10; -fx-border-radius: 10; -fx-background-radius: 10;");

            // 🔹 Safe image loading from same folder as FXML
            ImageView img = new ImageView();
            InputStream is = getClass().getResourceAsStream(item.image);
            if (is != null) {
                img.setImage(new Image(is));
            } else {
                logger.log(Level.WARNING, "Image not found: {0}", item.image);
            }
            img.setFitWidth(70);
            img.setFitHeight(70);

            Label nameLabel = new Label(item.name + "\n₹" + item.price);
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

            Button minus = new Button("-");
            Button plus = new Button("+");
            Label qtyLabel = new Label("0");

            minus.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white;");
            plus.setStyle("-fx-background-color: #51cf66; -fx-text-fill: black;");

            // Button actions
            plus.setOnAction(e -> {
                cart.computeIfAbsent(item.name, k -> new CartItem(item)).qty++;
                qtyLabel.setText(String.valueOf(cart.get(item.name).qty));
                updateBill();
            });

            minus.setOnAction(e -> {
                CartItem ci = cart.get(item.name);
                if (ci != null && ci.qty > 0) {
                    ci.qty--;
                    qtyLabel.setText(String.valueOf(ci.qty));
                    if (ci.qty == 0) cart.remove(item.name);
                    updateBill();
                }
            });

            row.getChildren().addAll(img, nameLabel, minus, qtyLabel, plus);
            menuVBox.getChildren().add(row);
        }

        updateBill();
    }

    private void updateBill() {
        billVBox.getChildren().clear();

        double subtotal = 0;

        for (CartItem ci : cart.values()) {
            double price = ci.item.price * ci.qty;
            subtotal += price;

            Label line = new Label(ci.item.name + " x " + ci.qty + " = ₹" + price);
            billVBox.getChildren().add(line);
        }

        double gst = subtotal * GST_RATE;
        double tax = subtotal * TAX_RATE;
        double total = subtotal + gst + tax;

        subTotalLabel.setText("Subtotal: ₹" + subtotal);
        gstLabel.setText("GST (5%): ₹" + gst);
        taxLabel.setText("Service Tax (3%): ₹" + tax);
        grandTotalLabel.setText("Grand Total: ₹" + total);
    }

    @FXML
    private void generateBill() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Bill");
        alert.setHeaderText("Payment Method: " + paymentOption.getValue());
        alert.setContentText(grandTotalLabel.getText());
        alert.showAndWait();
    }

    @FXML
    private void saveBill() {
        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName("Restaurant_Bill.txt");
        File file = chooser.showSaveDialog(menuVBox.getScene().getWindow());

        if (file != null) {
            try (FileWriter fw = new FileWriter(file)) {
                fw.write("---- RESTAURANT BILL ----\n");
                for (CartItem ci : cart.values()) {
                    fw.write(ci.item.name + " x " + ci.qty + "\n");
                }
                fw.write(subTotalLabel.getText() + "\n");
                fw.write(gstLabel.getText() + "\n");
                fw.write(taxLabel.getText() + "\n");
                fw.write(grandTotalLabel.getText() + "\n");
                fw.write("Payment Method: " + paymentOption.getValue());
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to save bill", e);
            }
        }
    }

    // --------- INNER CLASSES ---------
    static class MenuItem {
        String name;
        double price;
        String image;
        MenuItem(String n, double p, String i) { name = n; price = p; image = i; }
    }

    static class CartItem {
        MenuItem item;
        int qty = 0;
        CartItem(MenuItem i) { item = i; }
    }
}
