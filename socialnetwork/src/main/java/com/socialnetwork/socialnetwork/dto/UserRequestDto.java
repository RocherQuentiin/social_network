package com.socialnetwork.socialnetwork.dto;

import java.time.LocalDate;

import org.springframework.web.multipart.MultipartFile;

public class UserRequestDto {

	private MultipartFile profilePictureUrl;
	private MultipartFile coverPictureUrl;
	private String userGender;
	private String lastName;
	private String firstName;
	private String phoneNumber;
	private LocalDate  birthdate;
	private String company;
	private String education;
	private String isepSpecialization;
	private int promoYear;
	private String website;
	private String bio;
	
	public MultipartFile getProfilePictureUrl() {
		return profilePictureUrl;
	}
	public void setProfilePictureUrl(MultipartFile profilePictureUrl) {
		this.profilePictureUrl = profilePictureUrl;
	}
	public MultipartFile getCoverPictureUrl() {
		return coverPictureUrl;
	}
	public void setCoverPictureUrl(MultipartFile coverPictureUrl) {
		this.coverPictureUrl = coverPictureUrl;
	}
	public String getUserGender() {
		return userGender;
	}
	public void setUserGender(String userGender) {
		this.userGender = userGender;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public LocalDate getBirthdate() {
		return birthdate;
	}
	public void setBirthdate(LocalDate birthdate) {
		this.birthdate = birthdate;
	}
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public String getEducation() {
		return education;
	}
	public void setEducation(String education) {
		this.education = education;
	}
	public String getIsepSpecialization() {
		return isepSpecialization;
	}
	public void setIsepSpecialization(String isepSpecialization) {
		this.isepSpecialization = isepSpecialization;
	}
	public int getPromoYear() {
		return promoYear;
	}
	public void setPromoYear(int promoYear) {
		this.promoYear = promoYear;
	}
	public String getWebsite() {
		return website;
	}
	public void setWebsite(String website) {
		this.website = website;
	}
	public String getBio() {
		return bio;
	}
	public void setBio(String bio) {
		this.bio = bio;
	}
	
	
	
}
