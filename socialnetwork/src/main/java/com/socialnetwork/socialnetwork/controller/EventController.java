package com.socialnetwork.socialnetwork.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import com.socialnetwork.socialnetwork.business.interfaces.service.IEventPaymentService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IEventService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IUserService;
import com.socialnetwork.socialnetwork.business.utils.Utils;
import com.socialnetwork.socialnetwork.dto.EventDto;
import com.socialnetwork.socialnetwork.entity.Event;
import com.socialnetwork.socialnetwork.entity.User;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/event")
public class EventController {
	private IEventService eventService;
	private IUserService userService;
	private IEventPaymentService eventPaymentService;

	public EventController(IEventService eventService, IUserService userService,
			IEventPaymentService eventPaymentService) {
		this.eventService = eventService;
		this.userService = userService;
		this.eventPaymentService = eventPaymentService;
	}

	@PostMapping("")
	public RedirectView createEvent(HttpServletRequest request, Model model, Event event,
			@RequestParam(value = "returnTo", required = false, defaultValue = "/feed") String returnTo) {
		Object userIsConnect = Utils.validPage(request, true);
		model.addAttribute("isConnect", userIsConnect);
		if (userIsConnect == null) {
			return new RedirectView("/");
		}

		String redirectBase = sanitizeReturnTo(returnTo);

		if (event.getName().trim().equals("") || event.getEventDate() == null || event.getLocation().trim().equals("")
				|| event.getVisibilityType().equals("")) {
			model.addAttribute("errorEvent", "Veuillez remplir l'ensemble des champs");
			model.addAttribute("event", event);
			return new RedirectView(redirectBase);
		}

		if (event.getCapacity() <= 0) {
			model.addAttribute("errorEvent", "Un événement doit avoir obligatoirement plus de 0 participants");
			model.addAttribute("event", event);
			return new RedirectView(redirectBase);
		}

		ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Paris"));

		if (event.getEventDate().isBefore(now.toLocalDateTime())
				|| event.getEventDate().isEqual(now.toLocalDateTime())) {
			model.addAttribute("errorEvent",
					"La date doit être supérieur a celle d'aujourd'hui pour la création de l'événement");
			model.addAttribute("event", event);
			return new RedirectView(redirectBase);
		}

		try {
			ResponseEntity<User> user = this.userService.getUserById(UUID.fromString(userIsConnect.toString()));
			event.setCreator(user.getBody());
			this.eventService.save(event);
			return new RedirectView(redirectBase + (redirectBase.contains("?") ? "&" : "?") + "eventCreated=1");
		} catch (IllegalArgumentException ex) {
			model.addAttribute("errorEvent", ex.getMessage());
			model.addAttribute("event", event);
			return new RedirectView(redirectBase);
		}
	}

	@GetMapping("{id}/payment")
	public String showEventPaymentPage(@PathVariable("id") UUID eventId,
			@RequestParam(value = "returnTo", required = false) String returnTo,
			HttpServletRequest request,
			Model model) {
		Object userIsConnect = Utils.validPage(request, true);
		model.addAttribute("isConnect", userIsConnect);

		if (userIsConnect == null) {
			return "redirect:/login";
		}

		ResponseEntity<Event> eventResponse = this.eventService.getEventByID(eventId);
		Event event = eventResponse.getBody();
		if (eventResponse.getStatusCode().isError() || event == null || !Boolean.TRUE.equals(event.getIsPaid())) {
			return "redirect:/feed?payment=unavailable";
		}

		UUID currentUserId = UUID.fromString(userIsConnect.toString());
		if (event.getCreator() != null && event.getCreator().getId().equals(currentUserId)) {
			return "redirect:/feed?payment=own-event";
		}

		if (eventPaymentService.hasSuccessfulPayment(eventId, currentUserId)) {
			eventPaymentService.ensureAttendanceAfterSuccessfulPayment(eventId, currentUserId);
			return "redirect:" + buildSuccessRedirect(eventId, returnTo);
		}

		model.addAttribute("event", event);
		model.addAttribute("currentUserId", currentUserId.toString());
		model.addAttribute("returnTo", sanitizeReturnTo(returnTo));
		ResponseEntity<User> currentUser = this.userService.getUserById(currentUserId);
		BigDecimal wallet = BigDecimal.ZERO;
		if (currentUser.getStatusCode().is2xxSuccessful() && currentUser.getBody() != null
				&& currentUser.getBody().getWalletBalance() != null) {
			wallet = currentUser.getBody().getWalletBalance();
		}
		model.addAttribute("currentUserWalletBalance", wallet);
		return "eventPayment";
	}

	@PutMapping("{id}")
	public ResponseEntity<?> updateEvent(@PathVariable("id") UUID id, @RequestBody EventDto body,
			HttpServletRequest request) {
		Object userIsConnect = Utils.validPage(request, true);
		if (userIsConnect == null) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authenticated");
		}

		ResponseEntity<Event> event = this.eventService.getEventByID(id);

		if (event.getStatusCode() == HttpStatusCode.valueOf(404)) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
		}

		if (!userIsConnect.toString().equals(event.getBody().getCreator().getId().toString())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only author can edit");
		}

		if (body.getEventName().trim().equals("") || body.getEventDate() == null
				|| body.getEventLocation().trim().equals("") || body.getEventVisibility().equals("")) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Remplir l'ensemble des champs");
		}
		
		if(body.getEventCapacity() < event.getBody().getCapacity()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Capacité minimum : " + event.getBody().getCapacity());
		}

		ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
		LocalDateTime eventDate = LocalDateTime.parse(body.getEventDate());

		if (eventDate.isBefore(now.toLocalDateTime()) || eventDate.isEqual(now.toLocalDateTime())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("La date doit être supérieur a celle actuel");
		}

		event.getBody().setName(body.getEventName());
		event.getBody().setVisibilityType(body.getEventVisibility());
		event.getBody().setCapacity(body.getEventCapacity());
		event.getBody().setEventDate(eventDate);
		event.getBody().setLocation(body.getEventLocation());
		event.getBody().setDescription(body.getEventDescription());

		event = this.eventService.update(event.getBody());

		return ResponseEntity.ok().build();
	}

	@DeleteMapping("{id}")
	public ResponseEntity<?> deleteEvent(@PathVariable("id") UUID id, HttpServletRequest request) {
		Object userIsConnect = Utils.validPage(request, true);
		if (userIsConnect == null) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authenticated");
		}

		ResponseEntity<Event> event = this.eventService.getEventByID(id);
		if (event.getStatusCode() == HttpStatusCode.valueOf(404)) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
		}

		if (!userIsConnect.toString().equals(event.getBody().getCreator().getId().toString())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the creator can delete this event");
		}

		ResponseEntity<Void> deleted = this.eventService.deleteById(id);
		if (deleted.getStatusCode() == HttpStatus.NOT_FOUND) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
		}

		return ResponseEntity.noContent().build();
	}

	private String sanitizeReturnTo(String returnTo) {
		if (returnTo == null || returnTo.isBlank()) {
			return "/feed";
		}
		if (!returnTo.startsWith("/") || returnTo.startsWith("//")) {
			return "/feed";
		}
		return returnTo;
	}

	private String buildSuccessRedirect(UUID eventId, String returnTo) {
		String base = sanitizeReturnTo(returnTo);
		String separator = base.contains("?") ? "&" : "?";
		return base + separator + "payment=success&eventId=" + eventId;
	}
}
