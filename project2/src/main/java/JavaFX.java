import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class JavaFX extends Application {
    private BorderPane root;
    
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("I'm a professional Weather App!");
        root = new BorderPane();
        root.setPadding(new Insets(20));
        
        initializeUI();
        setupRefreshButton();

        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initializeUI() {
        ArrayList<weather.Period> forecast = weather.WeatherAPI.getForecast("LOT", 73, 70);
        
        if (forecast == null) {
            throw new RuntimeException("Forecast did not load");
        }

        weather.Period today = forecast.get(0);
        double tempC = (today.temperature - 32) * 5 / 9;

        GridPane calendarPane = createCalendarPane();
        VBox weatherInfo = createWeatherInfoBox(today, tempC, forecast);
        HBox bottomBox = createBottomBox();

        root.setLeft(calendarPane);
        root.setCenter(weatherInfo);
        root.setBottom(bottomBox);
    }

    private void setupRefreshButton() {
        Button refreshButton = new Button("Refresh Data");
        refreshButton.setFont(Font.font("Arial", 14));
        refreshButton.setStyle("-fx-padding: 8 15;");
        refreshButton.setOnAction(e -> {
            root.getChildren().clear();
            initializeUI();
        });

        HBox refreshBox = new HBox(refreshButton);
        refreshBox.setAlignment(Pos.BOTTOM_RIGHT);
        refreshBox.setPadding(new Insets(10));
        root.setBottom(refreshBox);
    }

    private GridPane createCalendarPane() {
        GridPane calendar = new GridPane();
        calendar.setPadding(new Insets(15));
        calendar.setHgap(10);
        calendar.setVgap(10);
        calendar.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 15;");

        LocalDate now = LocalDate.now();
        Month month = now.getMonth();
        int year = now.getYear();
        LocalDate firstDay = LocalDate.of(year, month, 1);
        DayOfWeek startDay = firstDay.getDayOfWeek();
        int daysInMonth = month.length(now.isLeapYear());

        Label monthLabel = new Label(month.toString() + " " + year);
        monthLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        calendar.add(monthLabel, 0, 0, 7, 1);

        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(days[i]);
            dayLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            calendar.add(dayLabel, i, 1);
        }

        int column = startDay.getValue() % 7;
        int row = 2;
        for (int day = 1; day <= daysInMonth; day++) {
            Label dayNumber = new Label(Integer.toString(day));
            dayNumber.setFont(Font.font("Arial", 14));
            if (day == LocalDate.now().getDayOfMonth()) {
                dayNumber.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
            calendar.add(dayNumber, column, row);
            column = (column + 1) % 7;
            if (column == 0) row++;
        }

        calendar.setMinWidth(350);
        return calendar;
    }

    private VBox createWeatherInfoBox(weather.Period today, double tempC, ArrayList<weather.Period> forecast) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE MMMM dd");
        String formattedDate = dateFormat.format(today.startTime);
        
        Label cityLabel = new Label("Chicago, IL");
        cityLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));

        Label dateLabel = new Label(formattedDate);
        dateLabel.setFont(Font.font("Arial", 18));

        Label conditionLabel = new Label(today.shortForecast + " conditions");
        conditionLabel.setFont(Font.font("Arial", 16));

        Label tempLabel = new Label(String.format("%d° F / %.1f° C", (int) today.temperature, tempC));
        tempLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));

        String highLow = calculateHighLow(forecast, today);
        Label highLowLabel = new Label(highLow);
        highLowLabel.setFont(Font.font("Arial", 14));

        String recommendations = getRecommendations(today);
        Label shouldILabel = new Label(recommendations);
        shouldILabel.setFont(Font.font("Arial", 14));

        VBox weatherBox = new VBox(10, cityLabel, dateLabel, conditionLabel, tempLabel, highLowLabel, shouldILabel);
        weatherBox.setAlignment(Pos.CENTER_LEFT);
        weatherBox.setPadding(new Insets(20));
        weatherBox.setStyle("-fx-spacing: 15;");

        return weatherBox;
    }

    private String calculateHighLow(ArrayList<weather.Period> forecast, weather.Period today) {
        LocalDate todayDate = today.startTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int high = Integer.MIN_VALUE;
        int low = Integer.MAX_VALUE;
        
        for (weather.Period p : forecast) {
            LocalDate pDate = p.startTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (pDate.equals(todayDate)) {
                int temp = (int) p.temperature;
                high = Math.max(high, temp);
                low = Math.min(low, temp);
            }
        }
        return String.format("High: %d°F | Low: %d°F", high, low);
    }

    private String getRecommendations(weather.Period today) {
        String wearHat = today.temperature > 80 ? "Yes" : "No";
        String sunglasses = today.shortForecast.toLowerCase().contains("sun") ? "Yes" : "No";
        String jacket = today.temperature < 60 ? "Yes" : "No";
        String umbrella = (today.shortForecast.toLowerCase().matches(".*(rain|shower|snow).*")) ? "Yes" : "No";
        
        return "Should I:\n" +
               "Wear a Hat?: " + wearHat + "\n" +
               "Carry Sunglasses?: " + sunglasses + "\n" +
               "Wear a Jacket?: " + jacket + "\n" +
               "Carry an Umbrella?: " + umbrella;
    }

    private HBox createBottomBox() {
        Button forecastButton = new Button("3 Day Forecast →");
        forecastButton.setFont(Font.font("Arial", 14));
        forecastButton.setStyle("-fx-padding: 8 15;");
        
        HBox bottomBox = new HBox(forecastButton);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(20));
        return bottomBox;
    }
}