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
import javafx.scene.control.ScrollPane;
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
    private static final String PARTLYSUNNY_GIF = "/images/partlysunny.gif";
    
    private Stage primaryStage;
    private Scene mainScene;
    private Scene forecastScene;
    private Scene sevenDayForecastScene;
    private String detailedForecast;
    private ArrayList<weather.Period> forecast;
    private BorderPane root;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("NTF - Nature's True Forecast");
        primaryStage.setResizable(false);

        
        initializeUI();
        createForecastScene();
        createSevenDayForecastScene();
        
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    private void initializeUI() {
        forecast = weather.WeatherAPI.getForecast("LOT", 76, 73);
        
        if (forecast == null) {
            throw new RuntimeException("Forecast did not load");
        }

    
        root = new BorderPane();
        root.setPadding(new Insets(10, 20, 20, 20));

       
        weather.Period today = forecast.get(0);
        double tempC = (today.temperature - 32) * 5 / 9;
        GridPane calendarPane = createCalendarPane();
        VBox weatherInfo = createWeatherInfoBox(today, tempC, forecast);
        HBox bottomBox = createBottomBox();

  
        root.setLeft(calendarPane);
        root.setCenter(weatherInfo);
        root.setBottom(bottomBox);
        
        mainScene = new Scene(root, 1000, 700);
    }
    
  
    private void createSevenDayForecastScene() {
        VBox rootVBox = new VBox(10);
        rootVBox.setPadding(new Insets(6));
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        VBox forecastContent = new VBox(6);
        forecastContent.setPadding(new Insets(6));
        Label forecastTitle = new Label("7 Day Forecast");
        forecastTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        forecastContent.getChildren().add(forecastTitle);
        LocalDate todayDate = LocalDate.now();

        for (int i = 0; i < 7; i++) {
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
            String windSpeed = "";
            String windDirection = "";
            int precipitation = 0;
            for (weather.Period p : dayPeriods) {
                int temp = (int) p.temperature;
                if (temp > high) high = temp;
                if (temp < low) low = temp;
                if (shortForecast.isEmpty()) {
                    shortForecast = p.shortForecast;
                    windSpeed = p.windSpeed;
                    windDirection = p.windDirection;
                }
                precipitation = Math.max(precipitation, p.probabilityOfPrecipitation.value);
            }

            int cHigh = (high - 32) * 5 / 9;
            int cLow = (low - 32) * 5 / 9;

            String dateStr = dayDate.getMonth().toString() + " " + dayDate.getDayOfMonth() + ", " + dayDate.getYear();
            String gifPath = DEFAULT_GIF;
            String forecastLower = shortForecast.toLowerCase();
            if (forecastLower.contains("storm")) {
                gifPath = STORMY_GIF;
            } else if (forecastLower.contains("partly") && forecastLower.contains("sun")) {
                gifPath = PARTLYSUNNY_GIF;
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
            dayGif.setFitWidth(50);
            dayGif.setFitHeight(50);
            dayGif.setPreserveRatio(true);
            Label dateLabel = new Label(dateStr);
            dateLabel.setFont(Font.font("Arial", FontWeight.BOLD, 15));
            Label tempLabel = new Label(String.format("High: %d°F/%d°C   Low: %d°F/%d°C", high, cHigh, low, cLow));
            tempLabel.setFont(Font.font("Arial", 11));
            Label conditionLabel = new Label("Condition: " + shortForecast);
            conditionLabel.setFont(Font.font("Arial", 11));
            Label windLabel = new Label(String.format("Wind: %s %s", windSpeed, windDirection));
            windLabel.setFont(Font.font("Arial", 11));
            Label precipitationLabel = new Label(String.format("Precipitation: %d%%", precipitation));
            precipitationLabel.setFont(Font.font("Arial", 11));
            VBox textBox = new VBox(3);
            textBox.getChildren().add(dateLabel);
            textBox.getChildren().add(tempLabel);
            textBox.getChildren().add(conditionLabel);
            textBox.getChildren().add(windLabel);
            textBox.getChildren().add(precipitationLabel);
            textBox.setAlignment(Pos.CENTER_LEFT);
            HBox dayBox = new HBox(11);
            dayBox.getChildren().add(dayGif);
            dayBox.getChildren().add(textBox);
            dayBox.setAlignment(Pos.CENTER_LEFT);
            dayBox.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 11;");
            dayBox.setMaxWidth(10000);

            forecastContent.getChildren().add(dayBox);
        }

        scrollPane.setContent(forecastContent);
        Button backButton = new Button("Back to Today's Forecast");
        backButton.setOnAction(e -> primaryStage.setScene(mainScene));
        rootVBox.getChildren().addAll(scrollPane, backButton);
        sevenDayForecastScene = new Scene(rootVBox, 1000, 700);
    }


   
    
    
    private void createForecastScene() {
        BorderPane forecastRoot = new BorderPane();
        forecastRoot.setPadding(new Insets(10, 20, 20, 20));  
        VBox forecastContent = new VBox(20);
        forecastContent.setPadding(new Insets(20)); 
        Label forecastTitle = new Label("3 Day Forecast");
        forecastTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        forecastContent.getChildren().add(forecastTitle);
        LocalDate todayDate = LocalDate.now();
        
        for (int i = 1; i < 4; i++) {
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
            String windSpeed = "";
            String windDirection = "";
            int precipitation = 0;
            
            for (weather.Period p : dayPeriods) {
                int temp = (int)p.temperature;
                if (temp > high) high = temp;
                if (temp < low) low = temp;
                if (shortForecast.isEmpty()) {
                    shortForecast = p.shortForecast;
                    windSpeed = p.windSpeed;
                    windDirection = p.windDirection;
                    precipitation = p.probabilityOfPrecipitation.value;
                    detailedForecast = p.detailedForecast;
                }
            }
            
            int cHigh = (high - 32) * 5 / 9;
            int cLow = (low - 32) * 5 / 9;
            
            String dateStr = dayDate.getMonth().toString() + " " + dayDate.getDayOfMonth() + ", " + dayDate.getYear();
            
            String gifPath = DEFAULT_GIF;
            String forecastLower = shortForecast.toLowerCase();
            if (forecastLower.contains("storm")) {
                gifPath = STORMY_GIF;
            } else if (forecastLower.contains("partly") && forecastLower.contains("sun")) {
                gifPath = PARTLYSUNNY_GIF;
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
            Label windLabel = new Label(String.format("Wind: %s %s", windSpeed, windDirection));
            windLabel.setFont(Font.font("Arial", 14));      
            Label precipitationLabel = new Label(String.format("Precipitation: %d%%", precipitation));
            precipitationLabel.setFont(Font.font("Arial", 14));   
            Label detailedForecastLabel = new Label("Forecast: " + detailedForecast);
            detailedForecastLabel.setFont(Font.font("Arial", 14));
            detailedForecastLabel.setWrapText(true);            
            VBox textBox = new VBox(5);
            textBox.getChildren().add(dateLabel);
            textBox.getChildren().add(tempLabel);
            textBox.getChildren().add(conditionLabel);
            textBox.getChildren().add(windLabel);
            textBox.getChildren().add(precipitationLabel);
            textBox.getChildren().add(detailedForecastLabel);
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
        calendar.setPadding(new Insets(10, 5, 10, 10)); 
        calendar.setHgap(5);
        calendar.setVgap(5);
        calendar.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10;");
        calendar.setMaxHeight(300);

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

        calendar.setMinWidth(280);
        return calendar;
    }

    private VBox createWeatherInfoBox(weather.Period today, double tempC, ArrayList<weather.Period> forecast) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE MMMM dd");
        String formattedDate = dateFormat.format(today.startTime);
        String bgImage = "/images/sunnyclear.jpg";
        boolean isNight = false;
        String forecastLower = today.shortForecast.toLowerCase();
        String gifPath = DEFAULT_GIF;
        if (forecastLower.contains("storm")) {
            gifPath = STORMY_GIF;
            bgImage = "/images/rainysky.jpg";
        } else if (forecastLower.contains("partly") && forecastLower.contains("sun")) {
            gifPath = PARTLYSUNNY_GIF;
        } else if (forecastLower.contains("sun")) {
            gifPath = SUNNY_GIF;
        } else if (forecastLower.contains("cloud")) {
            gifPath = CLOUDY_GIF;
        } else if (forecastLower.contains("rain")) {
            gifPath = RAINY_GIF;
            bgImage = "/images/rainysky.jpg";
        } else if (forecastLower.contains("snow")) {
            gifPath = SNOWY_GIF;
            bgImage = "/images/rainysky.jpg";
        } else if (forecastLower.contains("night") || forecastLower.contains("clear")) {
            gifPath = NIGHT_GIF;
            bgImage = "/images/nightsky.jpg";
            isNight = true;
        }
        root.setStyle("-fx-background-image: url('" + bgImage + "'); -fx-background-size: cover;");
        String textColor = "";
        if (isNight) {
            textColor = "-fx-text-fill: white;";
        }
        Label cityLabel = new Label("Chicago, IL");
        cityLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        cityLabel.setStyle(textColor);
        Label dateLabel = new Label(formattedDate);
        dateLabel.setFont(Font.font("Arial", 18));
        dateLabel.setStyle(textColor);
        Label conditionLabel = new Label(today.shortForecast + " conditions");
        conditionLabel.setFont(Font.font("Arial", 16));
        conditionLabel.setStyle(textColor);
        Label tempLabel = new Label((int) today.temperature + "° F / " + String.format("%.1f", tempC) + "° C");
        tempLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        tempLabel.setStyle(textColor);
        Label detailedForecastLabel = new Label("Forecast: " + today.detailedForecast);
        detailedForecastLabel.setFont(Font.font("Arial", 14));
        detailedForecastLabel.setWrapText(true);
        detailedForecastLabel.setStyle(textColor);
        Label shouldILabel = new Label(getRecommendations(today));
        shouldILabel.setFont(Font.font("Arial", 14));
        shouldILabel.setStyle(textColor);
        ImageView weatherGif = new ImageView(new Image(getClass().getResourceAsStream(gifPath)));
        weatherGif.setFitWidth(150);
        weatherGif.setFitHeight(150);
        weatherGif.setPreserveRatio(true);
        Label windLabel = new Label("Wind: " + today.windSpeed + " " + today.windDirection);
        windLabel.setFont(Font.font("Arial", 14));
        windLabel.setStyle(textColor);
        Label precipitationLabel = new Label("Precipitation: " + today.probabilityOfPrecipitation.value + "%");
        precipitationLabel.setFont(Font.font("Arial", 14));
        precipitationLabel.setStyle(textColor);
        String highLow = calculateHighLow(forecast, today);
        Label highLowLabel = new Label(highLow);
        highLowLabel.setFont(Font.font("Arial", 14));
        highLowLabel.setStyle(textColor);
        VBox leftBox = new VBox(20);
        leftBox.getChildren().addAll(cityLabel, dateLabel, conditionLabel, tempLabel, detailedForecastLabel, shouldILabel);
        leftBox.setAlignment(Pos.TOP_LEFT);
        VBox rightBox = new VBox(20);
        rightBox.getChildren().addAll(weatherGif, windLabel, precipitationLabel, highLowLabel);
        rightBox.setAlignment(Pos.TOP_LEFT);
        HBox topContent = new HBox(60);
        topContent.getChildren().addAll(leftBox, rightBox);
        topContent.setAlignment(Pos.TOP_CENTER);
        VBox weatherBox = new VBox(20);
        weatherBox.setPadding(new Insets(0, 40, 40, 40)); 
        weatherBox.setAlignment(Pos.TOP_CENTER); 
        weatherBox.getChildren().add(topContent);
        leftBox.setMinWidth(350);
        rightBox.setMinWidth(350);
        weatherBox.setPrefSize(1000, 600);
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
        if (today.shortForecast.toLowerCase().contains("rain") || today.shortForecast.toLowerCase().contains("snow")) {
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
        Button forecastButton = new Button("3 Day Forecast ");
        forecastButton.setFont(Font.font("Arial", 14));
        forecastButton.setStyle("-fx-padding: 8 15;");
        forecastButton.setOnAction(e -> primaryStage.setScene(forecastScene));
        
        Button sevenDayButton = new Button("7 Day Forecast ");
        sevenDayButton.setFont(Font.font("Arial", 14));
        sevenDayButton.setStyle("-fx-padding: 8 15;");
        sevenDayButton.setOnAction(e -> primaryStage.setScene(sevenDayForecastScene));
        
        HBox bottomBox = new HBox(10);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(20));
        bottomBox.setPrefWidth(Double.MAX_VALUE);
        bottomBox.getChildren().addAll(forecastButton, sevenDayButton);
        BorderPane.setAlignment(bottomBox, Pos.CENTER);
        
        return bottomBox;
    }


}