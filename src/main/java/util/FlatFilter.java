package util;

import model.FlatListing;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FlatFilter {

    public static List<FlatListing> filterLastHour(List<FlatListing> all) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

        List<FlatListing> fresh = new ArrayList<>();
        for (FlatListing f : all) {
            if (f.getPublishedAt() != null && f.getPublishedAt().isAfter(oneHourAgo)) {
                fresh.add(f);
            }
        }
        return fresh;
    }
}