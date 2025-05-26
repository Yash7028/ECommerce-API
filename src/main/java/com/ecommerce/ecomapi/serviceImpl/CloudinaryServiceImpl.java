package com.ecommerce.ecomapi.serviceImpl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ecommerce.ecomapi.exception.FileUploadException;
import com.ecommerce.ecomapi.service.CloudinaryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    /* upload image in cloudinary */
    public String uploadImage(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        String publicId = uploadResult.get("public_id").toString();
        String secureId = uploadResult.get("secure_url").toString();
        log.info("publicId : {} /n publicId : {}  "  , secureId , publicId);
        return secureId;  // this is the public image URL
    }

    /* delete image in cloudinary */
    public boolean deleteImageThroughPublicId(String url) throws IOException {
        String publicId = extractPublicIdFromUrl(url);
        if (publicId.equals("o0ag2cjhel5pxb6znyxn") || publicId.equals("juiukjquurhjbf0uckf7") || publicId.equals("oomejiu2rv0dm2pruae8")){
            return true;
        }
        Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        return "ok".equals(result.get("result"));  // "ok" or "not found"
    }

    /* extract publicId through url */
    public String extractPublicIdFromUrl(String secureUrl){
        int lastSlashIndex = secureUrl.lastIndexOf('/');
        int lastDotIndex = secureUrl.lastIndexOf('.');

        if (lastSlashIndex == -1 || lastDotIndex == -1 || lastDotIndex <= lastSlashIndex) {
            throw new IllegalArgumentException("Invalid Cloudinary URL format");
        }
        // Extract the filename without extension
        return secureUrl.substring(lastSlashIndex + 1, lastDotIndex);
    }

}
