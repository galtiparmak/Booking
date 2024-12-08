package com.geko.Booking.Repository.Neo4j;

import com.geko.Booking.Entity.Neo4j.ListingNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListingNodeRepository extends Neo4jRepository<ListingNode, String> {
    List<ListingNode> findByTitleContaining(String keyword);

    @Query("MATCH (l1:Listing {id: $listingId}), (l2:Listing) " +
            "WHERE l1 <> l2 AND any(amenity IN l1.amenities WHERE amenity IN l2.amenities) " +
            "MERGE (l1)-[:SIMILAR_AMENITIES]->(l2) " +
            "MERGE (l2)-[:SIMILAR_AMENITIES]->(l1)")
    void createSimilarAmenitiesRelationships(String listingId);

    @Query("MATCH (l1:Listing {id: $listingId}), (l2:Listing) " +
            "WHERE l1 <> l2 AND distance(point({latitude: l1.lat, longitude: l1.lon}), " +
            "point({latitude: l2.lat, longitude: l2.lon})) < $radius " +
            "MERGE (l1)-[:NEARBY]->(l2) " +
            "MERGE (l2)-[:NEARBY]->(l1)")
    void createNearbyRelationships(String listingId, double radius);

    @Query("MATCH (l:Listing {id: $id})-[:SIMILAR_AMENITIES|NEARBY]->(rec:Listing) " +
            "RETURN DISTINCT rec.id LIMIT 6")
    List<String> getRecommendations(String id);

    @Query("MATCH (l1:Listing)-[r:SIMILAR_AMENITIES]->(l2:Listing) " +
            "DELETE r " +
            "WITH l1, l2 " +
            "WHERE any(amenity IN l1.amenities WHERE amenity IN l2.amenities) " +
            "MERGE (l1)-[:SIMILAR_AMENITIES]->(l2)")
    void refreshSimilarAmenitiesRelationships();

    @Query("MATCH (l:Listing {id: $listingId})-[r]->() DELETE r")
    void deleteAllRelationshipsForListing(String listingId);

    @Query("""
            MATCH (l1:Listing {id: $listingId})
            DETACH DELETE l1
            """)
    void deleteListing(String listingId);

    boolean existsById(String listingId);
}

