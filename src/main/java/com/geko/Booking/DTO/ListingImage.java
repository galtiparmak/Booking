package com.geko.Booking.DTO;

import com.geko.Booking.Entity.Elasticsearch.ListingIndex;
import com.geko.Booking.Entity.Mongo.Image;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListingImage {
    private ListingIndex listingIndex;
    private List<Image> images;
}
