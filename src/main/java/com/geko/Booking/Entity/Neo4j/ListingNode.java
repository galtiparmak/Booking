package com.geko.Booking.Entity.Neo4j;

import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import lombok.*;

import java.util.List;

@Node("Listing")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingNode {
    @Id
    private String id;
    private String title;
    private double lat;
    private double lon;
    private List<String> amenities;
    private String ownerUsername;

    @Relationship(type = "SIMILAR_AMENITIES", direction = Relationship.Direction.OUTGOING)
    private List<ListingNode> similarListings;
    @Relationship(type = "NEARBY", direction = Relationship.Direction.OUTGOING)
    private List<ListingNode> nearbyListings;
}

