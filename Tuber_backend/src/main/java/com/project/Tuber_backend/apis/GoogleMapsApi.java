package com.project.Tuber_backend.apis;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.json.JSONObject;

@Getter
@Setter
@AllArgsConstructor
public class GoogleMapsApi {

    private Integer ETA;
    private Integer distance;
    private String polyline;
    private String startLocation;
    private String endLocation;
    private Integer departureTime;

    @Value("${google.maps.api.key}")
    private String apiKey;  // Google Maps API Key should be stored in application.properties

    private final RestTemplate restTemplate;

    // Constructor to initialize departure time, start, and end locations
    public GoogleMapsApi(int departureTime, String startLocation, String endLocation, RestTemplate restTemplate) {
        this.departureTime = 1742917874;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.restTemplate = restTemplate;
    }

    // Method to fetch ETA, distance, and polyline from the Google Maps Distance Matrix API
    public void getRideDetails() {
        String url = "https://maps.googleapis.com/maps/api/directions/json";
        System.out.println("API Key: " + apiKey);
        // Build the URL with query parameters
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("origin", this.startLocation)
                .queryParam("destination", this.endLocation)
                .queryParam("mode", "driving")
                .queryParam("departure_time", this.departureTime)
                .queryParam("key", apiKey);

        String response = restTemplate.getForObject(uriBuilder.toUriString(), String.class);
        parseResponse(response);
    }

    private void parseResponse(String response) {
        JSONObject jsonResponse = new JSONObject(response);

        if (!"OK".equals(jsonResponse.getString("status"))) {
            throw new RuntimeException("API Error: " + jsonResponse.optString("error_message", "Unknown error"));
        }

        JSONObject route = jsonResponse.getJSONArray("routes").getJSONObject(0);
        JSONObject leg = route.getJSONArray("legs").getJSONObject(0);

        this.distance = (leg.getJSONObject("distance").getInt("value"))/1000; // kms
        this.ETA = leg.getJSONObject("duration_in_traffic").getInt("value") / 60; // minutes
        this.polyline = route.getJSONObject("overview_polyline").getString("points");
    }

}
