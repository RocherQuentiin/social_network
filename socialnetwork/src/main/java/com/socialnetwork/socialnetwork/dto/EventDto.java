package com.socialnetwork.socialnetwork.dto;

import com.socialnetwork.socialnetwork.enums.VisibilityType;

public class EventDto {
	private String eventName;
	private String eventDate;
	private String eventLocation;
	private String eventDescription;
	private VisibilityType eventVisibility;
	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	public String getEventDate() {
		return eventDate;
	}
	public void setEventDate(String eventDate) {
		this.eventDate = eventDate;
	}
	public String getEventLocation() {
		return eventLocation;
	}
	public void setEventLocation(String eventLocation) {
		this.eventLocation = eventLocation;
	}
	public String getEventDescription() {
		return eventDescription;
	}
	public void setEventDescription(String eventDescription) {
		this.eventDescription = eventDescription;
	}
	public VisibilityType getEventVisibility() {
		return eventVisibility;
	}
	public void setEventVisibility(VisibilityType eventVisibility) {
		this.eventVisibility = eventVisibility;
	}

	
}
