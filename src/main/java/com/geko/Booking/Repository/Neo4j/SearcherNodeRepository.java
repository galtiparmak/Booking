package com.geko.Booking.Repository.Neo4j;

import com.geko.Booking.Entity.Neo4j.ListingNode;
import com.geko.Booking.Entity.Neo4j.SearcherNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SearcherNodeRepository extends Neo4jRepository<SearcherNode, String> {
    Optional<SearcherNode> findByUsername(String username);

    @Query("MATCH (u:Searcher {id: $userId}), (l:Listing {id: $listingId}) " +
            "MERGE (u)-[:VIEWED]->(l)")
    void logView(String searcherId, String listingId);

    @Query("MATCH (u:Searcher {id: $userId})-[:VIEWED|LIKED|BOOKED]->(l:Listing)-[:SIMILAR_AMENITIES|NEARBY]->(rec:Listing) " +
            "WHERE NOT (u)-[:BOOKED]->(rec) " +
            "RETURN DISTINCT rec.id" +
            "LIMIT 6")
    List<String> findRecommendations(String searcherId);

    @Query("""
            MATCH (s:Searcher {username: $username})
            DETACH DELETE s
            """)
    void deleteByUsername(String username);


    @Query("""
            MATCH (s:Searcher {id: $id})
            DETACH DELETE s
            """)
    void deleteById(String id);

    boolean existsByUsername(String username);
}

