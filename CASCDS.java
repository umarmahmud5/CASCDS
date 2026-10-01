//CASCDS
//Developed by Umar Mahmud
//Sep 2025
//code is generated using Gemini and fixed by the author to improve readability
//Requires loading on Raspberry Pi 4 device

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class CASCDSNode {    //Sample code to simulate CASCDS

    // Configuration & Constants
    private static final String WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather?q=London&appid=YOUR_API_KEY";   //API key is deliberately removed
    private static final String STATE_NORMAL = "Normal";
    private static final String STATE_ALERT = "Alert";
    private static final String STATE_EMERGENCY = "Emergency";

    // Sensor Data Structure
    public static class SensorData {
        double cabinTemp;
        double engineTemp;
        boolean smokeDetected;
        List<Double> tirePressures;
        double fuelLevel;
        double coolantLevel;
        boolean sirenDetected;
        boolean roadHazard;
        String weatherState;

        public SensorData(double cabinTemp, double engineTemp, boolean smokeDetected, 
                          List<Double> tirePressures, double fuelLevel, double coolantLevel, 
                          boolean sirenDetected, boolean roadHazard, String weatherState) {
            this.cabinTemp = cabinTemp;
            this.engineTemp = engineTemp;
            this.smokeDetected = smokeDetected;
            this.tirePressures = tirePressures;
            this.fuelLevel = fuelLevel;
            this.coolantLevel = coolantLevel;
            this.sirenDetected = sirenDetected;
            this.roadHazard = roadHazard;
            this.weatherState = weatherState;
        }
    }

    public void initializeHardware() {
        System.out.println("Initializing CASCDS Vehicle System on Raspberry Pi 4 (Java Runtime)...");
        // Note: For physical GPIO/ADC/Camera sensor polling on Pi 4, 
        // integrate libraries like Pi4J or WebCam-Capture here.
    }

    public double[] readTemperatureSensors() {
        // Mocking sensor reading (Cabin Temp, Engine Temp in Celsius)
        return new double[]{22.5, 88.5};
    }

    public boolean readSmokeSensor() {
        // Mocking digital smoke detector pin
        return false;
    }

    public Object[] readVehicleLevelsAndPressure() {
        // Mocking tire pressures (PSI), fuel level (0-1), and coolant level (0-1)
        List<Double> pressures = Arrays.asList(32.0, 32.0, 31.5, 32.0);
        double fuel = 0.70;
        double coolant = 0.85;
        return new Object[]{pressures, fuel, coolant};
    }

    public boolean checkEmergencySiren() {
        // Mocking microphone acoustic processing for emergency sirens
        return false;
    }

    public boolean captureRoadConditions() {
        // Mocking camera computer-vision obstacle/hazard detector
        return false;
    }

    public String fetchWeatherForecast() {
        try {
            URL url = new URL(WEATHER_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                // Basic string parsing for weather condition match
                String resStr = response.toString();
                if (resStr.contains("Thunderstorm") || resStr.contains("Snow")) {
                    return "Severe Weather";
                }
            }
        } catch (Exception e) {
            // Fallback behavior if offline or API key isn't configured yet
            return "Clear";
        }
        return "Clear";
    }

    public String classifyContext(SensorData data) {
        // 1. Emergency Evaluation Conditions
        if (data.smokeDetected || data.engineTemp > 110.0 || data.sirenDetected) {
            return STATE_EMERGENCY;
        }

        // 2. Alert Evaluation Conditions
        boolean lowPressure = data.tirePressures.stream().anyMatch(p -> p < 25.0);
        if (lowPressure || data.fuelLevel < 0.15 || data.coolantLevel < 0.3 || 
            data.roadHazard || data.weatherState.equals("Severe Weather")) {
            return STATE_ALERT;
        }

        // 3. Default Normal State
        return STATE_NORMAL;
    }

    public void runLoop() {
        initializeHardware();
        try {
            while (true) {
                // Gather Sensor Data
                double[] temps = readTemperatureSensors();
                boolean smoke = readSmokeSensor();
                Object[] levels = readVehicleLevelsAndPressure();
                @SuppressWarnings("unchecked")
                List<Double> tires = (List<Double>) levels[0];
                double fuel = (Double) levels[1];
                double coolant = (Double) levels[2];
                boolean siren = checkEmergencySiren();
                boolean hazard = captureRoadConditions();
                String weather = fetchWeatherForecast();

                SensorData currentData = new SensorData(
                    temps[0], temps[1], smoke, tires, fuel, coolant, siren, hazard, weather
                );

                // Process Context State via ML / Rules
                String currentContext = classifyContext(currentData);

                // Output to Embedded Dashboard Screen / Console
                System.out.printf("[Dashboard Display] Weather: %s | Engine Temp: %.1f°C | State: %s%n", 
                                  weather, temps[1], currentContext);

                Thread.sleep(2000); // 2-second sampling interval
            }
        } catch (InterruptedException e) {
            System.out.println("CASCDS System terminated safely.");
        }
    }

    public static void main(String[] args) {
        CASCDSNode node = new CASCDSNode();
        node.runLoop();
    }
}
