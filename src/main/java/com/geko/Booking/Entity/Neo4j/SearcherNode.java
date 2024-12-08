package com.geko.Booking.Entity.Neo4j;

import com.geko.Booking.Entity.Mysql.Role;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import lombok.*;

import java.util.List;

@Node("Searcher")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearcherNode {
    @Id
    private String id;
    private String username;

    @Relationship(type = "VIEWED", direction = Relationship.Direction.OUTGOING)
    private List<ListingNode> viewedListings;
    @Relationship(type = "LIKED", direction = Relationship.Direction.OUTGOING)
    private List<ListingNode> likedListings;
    @Relationship(type = "BOOKED", direction = Relationship.Direction.OUTGOING)
    private List<ListingNode> bookedListings;
}

