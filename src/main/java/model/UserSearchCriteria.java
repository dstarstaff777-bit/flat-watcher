package model;

public class UserSearchCriteria {
    private Integer minPrice;
    private Integer maxPrice;
    private String district;

    public UserSearchCriteria(Integer minPrice, Integer maxPrice, String district) {
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.district = district.toLowerCase();
    }

    public boolean matches(FlatListing listing) {
        if(listing == null) return false;
        boolean priceMatch = true;
        if(minPrice != null) {
            priceMatch = listing.getPrice() >= minPrice;
        }
        if(maxPrice != null) {
            priceMatch = priceMatch && (listing.getPrice() <= maxPrice);
        }
        boolean districtMatch = true;
        if (district != null && !district.isEmpty()) {
            String listingDistrict = listing.getDistrict() != null ?
                    listing.getDistrict().toLowerCase() : "";
            districtMatch = listingDistrict.contains(district);
        }
        return priceMatch && districtMatch;
    }

    public String toSearchUrl() {
        return "https://avito.ru/uzlovaya/kvartiry?" +
                "min=" + minPrice +
                "max=" + maxPrice +
                "district=" + district;
    }
}

