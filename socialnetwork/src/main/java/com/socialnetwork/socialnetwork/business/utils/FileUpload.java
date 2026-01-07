package com.socialnetwork.socialnetwork.business.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

public class FileUpload {
	private static final Path UPLOAD_DIR = Paths.get("src/main/resources/static/upload");
	
	public static String UploadFile(MultipartFile file) {
		try {
			System.out.println("File upload directory : " + UPLOAD_DIR);
            String originalFilename = Utils.generateRandomString(10) + "_" + StringUtils.cleanPath(file.getOriginalFilename());
            System.out.println("File upload originalFilename : " + originalFilename);
            Path targetLocation = UPLOAD_DIR.resolve(originalFilename);
            System.out.println("File upload Path : " + targetLocation);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return "/upload/" + originalFilename;

        } catch (IOException ex) {
            return "error";
        }
	}
}
