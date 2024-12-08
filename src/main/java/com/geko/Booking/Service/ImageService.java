package com.geko.Booking.Service;

import com.geko.Booking.Entity.Mongo.LogMapper;
import com.geko.Booking.DTO.Response;
import com.geko.Booking.Entity.Mongo.Action;
import com.geko.Booking.Entity.Mongo.Image;
import com.geko.Booking.Entity.Mysql.Homeowner;
import com.geko.Booking.Entity.Mysql.Listing;
import com.geko.Booking.KafkaProducer.LogProducer;
import com.geko.Booking.Repository.Mongodb.ImageRepository;
import com.geko.Booking.Repository.Mysql.HomeownerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ImageService {
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg");
    private final ImageRepository imageRepository;
    private final HomeownerRepository homeownerRepository;
    private final LogProducer logProducer;

    @Autowired
    public ImageService(ImageRepository imageRepository,
                        HomeownerRepository homeownerRepository,
                        LogProducer logProducer) {
        this.imageRepository = imageRepository;
        this.homeownerRepository = homeownerRepository;
        this.logProducer = logProducer;
    }

    public Response uploadImages(String username, String listingId, List<String> paths) {
        HashSet<String> set = new HashSet<>();
        if (paths.size() > 15) {
            return Response
                    .builder()
                    .success(false)
                    .message("You can only upload upto 15 images")
                    .build();
        }

        if (!doesOwnersListing(username, listingId)) {
            return Response
                    .builder()
                    .success(false)
                    .message("Homeowner has no such listing")
                    .build();
        }

        for (String path : paths) {
            String extension = getFileExtension(path);
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                System.out.println("Skipping invalid file: " + path);
                continue;
            }
            if (set.contains(path)) {
                System.out.println("Image is already uploaded");
                continue;
            }
            Image image = Image
                    .builder()
                    .filePath(path)
                    .fileSize(0)
                    .listingId(listingId)
                    .build();
            imageRepository.save(image);
            set.add(path);
        }

        logProducer.create(LogMapper.createLog(username, Action.IMAGE_UPLOADED, listingId));

        return Response
                .builder()
                .success(true)
                .message("Images uploaded")
                .build();
    }

    public List<Image> getImagesForListing(String listingId) {
        return imageRepository.findByListingId(listingId);
    }

    public Image getSingleImage(String listingId) {
        Optional<Image> optionalImage =imageRepository.findFirstByListingId(listingId);
        return optionalImage.isPresent() ? optionalImage.orElseGet(() -> imageRepository.findFirstByListingId("default").get()) : null;
    }

    private boolean doesOwnersListing(String username, String listingId) {
        Homeowner homeowner = homeownerRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No such homeowner"));

        for (Listing l : homeowner.getListings()) {
            if (l.getId().equals(listingId)) {
                return true;
            }
        }

        return false;
    }

    private String getFileExtension(String filePath) {
        if (filePath == null || !filePath.contains(".")) {
            throw new IllegalArgumentException("Invalid file path: No file extension found.");
        }
        return filePath.substring(filePath.lastIndexOf(".") + 1).toLowerCase();
    }

}
