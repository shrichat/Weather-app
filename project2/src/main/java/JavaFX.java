import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class JavaFX extends Application {
    private static final String DEFAULT_GIF = "/images/sunny.gif";
    private static final String SUNNY_GIF = "/images/sunny.gif";
    private static final String CLOUDY_GIF = "/images/cloudy.gif";
    private static final String RAINY_GIF = "/images/rainy.gif";
    private static final String SNOWY_GIF = "/images/snowy.gif";
    private static final String STORMY_GIF = "/images/stormy.gif";
    private static final String NIGHT_GIF = "/images/night.gif";
    
    private Stage primaryStage;
    private Scene mainScene;
    private Scene forecastScene;
    private ArrayList<weather.Period> forecast;
    private BorderPane root;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("I'm a professional Weather App!");
        
        initializeUI();
        createForecastScene();
        
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    private void initializeUI() {
        forecast = weather.WeatherAPI.getForecast("LOT", 73, 70);
        
        if (forecast == null) {
            throw new RuntimeException("Forecast did not load");
        }

        weather.Period today = forecast.get(0);
        double tempC = (today.temperature - 32) * 5 / 9;

        GridPane calendarPane = createCalendarPane();
        VBox weatherInfo = createWeatherInfoBox(today, tempC, forecast);
        HBox bottomBox = createBottomBox();

        root = new BorderPane();
        root.setPadding(new Insets(10, 20, 20, 20));
        root.setLeft(calendarPane);
        root.setCenter(weatherInfo);
        root.setBottom(bottomBox);
        
        mainScene = new Scene(root, 1000, 700);
    }

    private void createForecastScene() {
        BorderPane forecastRoot = new BorderPane();
        forecastRoot.setPadding(new Insets(10, 20, 20, 20));
        
        VBox forecastContent = new VBox(20);
        forecastContent.setPadding(new Insets(20));
        
        Label forecastTitle = new Label("3-Day Forecast Display");
        forecastTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        forecastContent.getChildren().add(forecastTitle);

        LocalDate todayDate = LocalDate.now();
        
        for (int i = 0; i < 4; i++) {
            LocalDate dayDate = todayDate.plusDays(i);
            
            ArrayList<weather.Period> dayPeriods = new ArrayList<>();
            for (weather.Period p : forecast) {
                LocalDate pDate = p.startTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                if (pDate.equals(dayDate)) {
                    dayPeriods.add(p);
                }
            }
            
            if (dayPeriods.isEmpty()) continue;
            
            int high = -1000;
            int low = 1000;
            String shortForecast = "";
            for (weather.Period p : dayPeriods) {
                int temp = (int)p.temperature;
                if (temp > high) high = temp;
                if (temp < low) low = temp;
                if (shortForecast.isEmpty()) {
                    shortForecast = p.shortForecast;
                }
            }
            
            int cHigh = (high - 32) * 5 / 9;
            int cLow = (low - 32) * 5 / 9;
            
            String dateStr = dayDate.getMonth().toString() + " " + dayDate.getDayOfMonth() + ", " + dayDate.getYear();
            
            String gifPath = DEFAULT_GIF;
            String forecastLower = shortForecast.toLowerCase();
            if (forecastLower.contains("storm")) {
                gifPath = STORMY_GIF;
            } else if (forecastLower.contains("sun")) {
                gifPath = SUNNY_GIF;
            } else if (forecastLower.contains("cloud")) {
                gifPath = CLOUDY_GIF;
            } else if (forecastLower.contains("rain")) {
                gifPath = RAINY_GIF;
            } else if (forecastLower.contains("snow")) {
                gifPath = SNOWY_GIF;
            } else if (forecastLower.contains("night") && forecastLower.contains("clear")) {
                gifPath = NIGHT_GIF;
            }
            
            ImageView dayGif = new ImageView(new Image(getClass().getResourceAsStream(gifPath)));
            dayGif.setFitWidth(80);
            dayGif.setFitHeight(80);
            dayGif.setPreserveRatio(true);
            
            Label dateLabel = new Label(dateStr);
            dateLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            
            Label tempLabel = new Label(String.format("High: %d°F/%d°C   Low: %d°F/%d°C", high, cHigh, low, cLow));
            tempLabel.setFont(Font.font("Arial", 14));
            
            Label conditionLabel = new Label("Condition: " + shortForecast);
            conditionLabel.setFont(Font.font("Arial", 14));
            
            VBox textBox = new VBox(5);
            textBox.getChildren().add(dateLabel);
            textBox.getChildren().add(tempLabel);
            textBox.getChildren().add(conditionLabel);
            textBox.setAlignment(Pos.CENTER_LEFT);
            
            HBox dayBox = new HBox(20);
            dayBox.getChildren().add(dayGif);
            dayBox.getChildren().add(textBox);
            dayBox.setAlignment(Pos.CENTER_LEFT);
            dayBox.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 15;");
            dayBox.setMaxWidth(10000);
            
            forecastContent.getChildren().add(dayBox);
        }
        
        Button backButton = new Button("Back to Today's Forecast");
        backButton.setOnAction(e -> primaryStage.setScene(mainScene));
        
        HBox forecastBottom = new HBox(backButton);
        forecastBottom.setAlignment(Pos.CENTER);
        forecastBottom.setPadding(new Insets(20));
        
        forecastRoot.setCenter(forecastContent);
        forecastRoot.setBottom(forecastBottom);
        
        forecastScene = new Scene(forecastRoot, 1000, 700);
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

        Label detailedForecastLabel = new Label("Forecast: " + today.detailedForecast);
        detailedForecastLabel.setFont(Font.font("Arial", 14));
        detailedForecastLabel.setWrapText(true);

        ImageView weatherGif = new ImageView();
        String forecastLower = today.shortForecast.toLowerCase();
        String gifPath = DEFAULT_GIF;

        if (forecastLower.contains("storm")) {
            gifPath = STORMY_GIF;
        } else if (forecastLower.contains("sun")) {
            gifPath = SUNNY_GIF;
        } else if (forecastLower.contains("cloud")) {
            gifPath = CLOUDY_GIF;
        } else if (forecastLower.contains("rain")) {
            gifPath = RAINY_GIF;
        } else if (forecastLower.contains("snow")) {
            gifPath = SNOWY_GIF;
        } else if (forecastLower.contains("night") && forecastLower.contains("clear")) {
            gifPath = NIGHT_GIF;
        }

        weatherGif.setImage(new Image(getClass().getResourceAsStream(gifPath)));
        weatherGif.setFitWidth(150);
        weatherGif.setFitHeight(150);
        weatherGif.setPreserveRatio(true);

        HBox topContent = new HBox(20);
        VBox textContent = new VBox(8, cityLabel, dateLabel, conditionLabel, tempLabel);
        topContent.getChildren().addAll(textContent, weatherGif);

        String highLow = calculateHighLow(forecast, today);
        Label highLowLabel = new Label(highLow);
        highLowLabel.setFont(Font.font("Arial", 14));

        String recommendations = getRecommendations(today);
        Label shouldILabel = new Label(recommendations);
        shouldILabel.setFont(Font.font("Arial", 14));

        VBox weatherBox = new VBox(10, topContent, highLowLabel, detailedForecastLabel, shouldILabel);
        weatherBox.setAlignment(Pos.TOP_LEFT);
        weatherBox.setPadding(new Insets(0, 20, 20, 20));
        weatherBox.setStyle("-fx-spacing: 10;");

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
        int c_high = (high - 32) * 5/9;
        int c_low = (low - 32) * 5/9;
        return String.format("High: %d°F / %d°C | Low: %d°F / %d°C", high, c_high, low, c_low);
    }

    private String getRecommendations(weather.Period today) {
        String wearHat;
        if(today.temperature > 70) {
            wearHat = "Yes"; 
        } else {
            wearHat = "No";
        }
        
        String sunglasses;
        if(today.shortForecast.toLowerCase().contains("sun")) {
            sunglasses = "Yes";
        } else {
            sunglasses = "No";
        }
    
        String jacket;
        if(today.temperature < 60) {
            jacket = "Yes";
        } else {
            jacket = "No";
        }
    
        String umbrella;
        if(today.shortForecast.toLowerCase().matches(".*(rain|snow).*")) {
            umbrella = "Yes"; 
        } else {
            umbrella = "No";
        }
        
        return "Would I need to:\n" +
               "Wear a Hat?: " + wearHat + "\n" +
               "Carry Sunglasses?: " + sunglasses + "\n" +
               "Wear a Jacket?: " + jacket + "\n" +
               "Carry an Umbrella?: " + umbrella;
    }

    private HBox createBottomBox() {
        Button forecastButton = new Button("3 Day Forecast →");
        forecastButton.setFont(Font.font("Arial", 14));
        forecastButton.setStyle("-fx-padding: 8 15;");
        forecastButton.setOnAction(e -> primaryStage.setScene(forecastScene));
        
        HBox bottomBox = new HBox(forecastButton);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(20));
        return bottomBox;
    }
}