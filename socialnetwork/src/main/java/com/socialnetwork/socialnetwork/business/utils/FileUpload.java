package com.socialnetwork.socialnetwork.business.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileUpload {
	private static Path UPLOAD_DIR;
	
	public FileUpload(@Value("${file-directory}") String fileDirectory) {
		UPLOAD_DIR = Paths.get(fileDirectory).toAbsolutePath().normalize();
	}
	
	public static String UploadFile(MultipartFile file) {
		try {
			System.out.println("File upload directory : " + UPLOAD_DIR);
            String originalFilename = Utils.generateRandomString(10) + "_" + StringUtils.cleanPath(file.getOriginalFilename());
            System.out.println("File upload originalFilename : " + originalFilename);
            Path targetLocation = UPLOAD_DIR.resolve(originalFilename);
            System.out.println("File upload Path : " + targetLocation);
            long info = Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File upload Success : " + info);
            return "/upload/" + originalFilename;

        } catch (IOException e) {
        	System.out.println("File upload Error Message : " + e.getLocalizedMessage());
        	System.out.println("File upload Error Message : " + e.getMessage());
            return "error";
        }
	}
}
