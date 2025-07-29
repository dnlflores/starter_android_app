package com.example.starter.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.starter.models.Tool;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<List<Tool>> properties;
    private List<Tool> allProperties;

    public HomeViewModel() {
        properties = new MutableLiveData<>();
        loadSampleProperties();
    }

    public LiveData<List<Tool>> getProperties() {
        return properties;
    }

    private void loadSampleProperties() {
        List<Tool> sampleProperties = new ArrayList<>();

        // Sample Airbnb-style properties with realistic location names
        sampleProperties.add(new Tool(1, "Mountain View, California", "$125", 
            "Stunning mountain cabin with panoramic views and hiking trails nearby.",
            101, "johnsmith", "john@example.com", "John", "Smith", 
            "https://example.com/cabin.jpg", 37.3861, -122.0840));

        sampleProperties.add(new Tool(2, "Brooklyn, New York", "$180", 
            "Modern Brooklyn loft with exposed brick walls and rooftop access.",
            102, "sarahjones", "sarah@example.com", "Sarah", "Jones", 
            "https://example.com/loft.jpg", 40.6782, -73.9442));

        sampleProperties.add(new Tool(3, "Malibu, California", "$350", 
            "Beachfront villa with private beach access and ocean views.",
            103, "mikebrown", "mike@example.com", "Mike", "Brown", 
            "https://example.com/villa.jpg", 34.0259, -118.7798));

        sampleProperties.add(new Tool(4, "Boston, Massachusetts", "$95", 
            "Historic brownstone in Back Bay with original Victorian details.",
            104, "emilydavis", "emily@example.com", "Emily", "Davis", 
            "https://example.com/townhouse.jpg", 42.3601, -71.0589));

        sampleProperties.add(new Tool(5, "Aspen, Colorado", "$220", 
            "Cozy ski cabin with fireplace and mountain trail access.",
            105, "davidwilson", "david@example.com", "David", "Wilson", 
            "https://example.com/forest.jpg", 39.1911, -106.8175));

        sampleProperties.add(new Tool(6, "West Hollywood, California", "$140", 
            "Chic apartment near Sunset Strip with city skyline views.",
            106, "lisagarcia", "lisa@example.com", "Lisa", "Garcia", 
            "https://example.com/apartment.jpg", 34.0900, -118.3617));

        sampleProperties.add(new Tool(7, "Lake Tahoe, California", "$200", 
            "Lakefront cabin with private dock and stunning sunset views.",
            107, "robmiller", "rob@example.com", "Rob", "Miller", 
            "https://example.com/lakehouse.jpg", 39.0968, -120.0324));

        sampleProperties.add(new Tool(8, "Scottsdale, Arizona", "$110", 
            "Desert retreat with pool and panoramic mountain views.",
            108, "amandataylor", "amanda@example.com", "Amanda", "Taylor", 
            "https://example.com/desert.jpg", 33.4942, -111.9261));

        sampleProperties.add(new Tool(9, "Park City, Utah", "$175", 
            "Alpine lodge near ski slopes with hot tub and mountain views.",
            109, "chrislee", "chris@example.com", "Chris", "Lee", 
            "https://example.com/alpine.jpg", 40.6461, -111.4980));

        sampleProperties.add(new Tool(10, "Miami Beach, Florida", "$165", 
            "Art Deco apartment steps from South Beach and nightlife.",
            110, "jessicawhite", "jessica@example.com", "Jessica", "White", 
            "https://example.com/miami.jpg", 25.7907, -80.1300));

        sampleProperties.add(new Tool(11, "Napa Valley, California", "$280", 
            "Vineyard cottage surrounded by wine country and rolling hills.",
            111, "tomgreen", "tom@example.com", "Tom", "Green", 
            "https://example.com/vineyard.jpg", 38.2975, -122.2869));

        sampleProperties.add(new Tool(12, "Portland, Oregon", "$85", 
            "Trendy downtown loft in the Pearl District with urban amenities.",
            112, "rachelblack", "rachel@example.com", "Rachel", "Black", 
            "https://example.com/portland.jpg", 45.5152, -122.6784));

        allProperties = new ArrayList<>(sampleProperties);
        properties.setValue(sampleProperties);
    }

    public void filterProperties(String category) {
        if (allProperties == null) return;

        List<Tool> filteredProperties = new ArrayList<>();

        switch (category.toLowerCase()) {
            case "houses":
                for (Tool property : allProperties) {
                    String name = property.getName().toLowerCase();
                    if (name.contains("cabin") || name.contains("house") || 
                        name.contains("villa") || name.contains("cottage") || 
                        name.contains("retreat")) {
                        filteredProperties.add(property);
                    }
                }
                break;
            case "apartments":
                for (Tool property : allProperties) {
                    String name = property.getName().toLowerCase();
                    if (name.contains("loft") || name.contains("apartment") || 
                        name.contains("brooklyn") || name.contains("downtown")) {
                        filteredProperties.add(property);
                    }
                }
                break;
            case "cabins":
                for (Tool property : allProperties) {
                    String name = property.getName().toLowerCase();
                    String description = property.getDescription().toLowerCase();
                    if (name.contains("cabin") || name.contains("lodge") || 
                        description.contains("cabin") || description.contains("mountain") ||
                        description.contains("ski") || description.contains("alpine")) {
                        filteredProperties.add(property);
                    }
                }
                break;
            case "all":
            default:
                filteredProperties = new ArrayList<>(allProperties);
                break;
        }

        properties.setValue(filteredProperties);
    }

    public void searchProperties(String query) {
        if (allProperties == null) return;

        if (query == null || query.trim().isEmpty()) {
            properties.setValue(allProperties);
            return;
        }

        List<Tool> filteredProperties = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (Tool property : allProperties) {
            if (property.getName().toLowerCase().contains(lowerQuery) ||
                property.getDescription().toLowerCase().contains(lowerQuery) ||
                property.getOwnerFullName().toLowerCase().contains(lowerQuery)) {
                filteredProperties.add(property);
            }
        }

        properties.setValue(filteredProperties);
    }
}